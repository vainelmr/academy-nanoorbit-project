package com.nanoorbit.groundcontrol.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle.State
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.StatutStation
import com.nanoorbit.groundcontrol.data.models.StationSol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val stations = remember { MockData.stations }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(3.5)
            controller.setCenter(averageGeoPoint(stations))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(State.RESUMED)) {
            mapView.post { mapView.onResume() }
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            applyLastKnownLocationOrNotify(
                context = context,
                mapView = mapView,
                scope = scope,
                snackbarHostState = snackbarHostState,
                onLocation = { location -> userLocation = location }
            )
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permission localisation refusée") }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Carte des stations au sol") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        applyLastKnownLocationOrNotify(
                            context = context,
                            mapView = mapView,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onLocation = { location -> userLocation = location }
                        )
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            ) {
                Text("Me localiser")
            }
        }
    ) { innerPadding ->
        if (stations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Aucune station au sol disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                factory = {
                    mapView.post { mapReady = true }
                    mapView
                },
                update = { view ->
                    if (mapReady) {
                        view.post {
                            renderStationMarkers(view, context, stations, userLocation)
                        }
                    }
                }
            )
        }
    }
}

private fun averageGeoPoint(stations: List<StationSol>): GeoPoint {
    if (stations.isEmpty()) return GeoPoint(0.0, 0.0)
    val avgLat = stations.sumOf { it.latitude } / stations.size
    val avgLon = stations.sumOf { it.longitude } / stations.size
    return GeoPoint(avgLat, avgLon)
}

private fun renderStationMarkers(
    mapView: MapView,
    context: Context,
    stations: List<StationSol>,
    userLocation: Location?
) {
    if (mapView.windowToken == null) return

    mapView.overlays.removeAll { it is Marker }
    stations.forEach { station ->
        runCatching {
            val marker = Marker(mapView).apply {
                position = GeoPoint(station.latitude, station.longitude)
                title = station.nomStation
                snippet = buildSnippet(station, userLocation)
                icon = buildMarkerIcon(context, station)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }
    }
    runCatching { mapView.invalidate() }
}

private fun buildSnippet(station: StationSol, userLocation: Location?): String {
    val distanceText = userLocation?.let { location ->
        val result = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            station.latitude,
            station.longitude,
            result
        )
        "Distance : ${"%.1f".format(result[0] / 1000f)} km"
    } ?: "Distance : localisation inactive"

    return buildString {
        appendLine("Code : ${station.codeStation}")
        appendLine("Statut : ${station.statut.name}")
        appendLine("Bande fréquence : indisponible")
        appendLine("Débit max : indisponible")
        appendLine("Diamètre antenne : indisponible")
        append(distanceText)
    }
}

private fun buildMarkerIcon(context: Context, station: StationSol) =
    ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.mutate()?.also { drawable ->
        val tintColor = when (station.statut) {
            StatutStation.OPERATIONNELLE -> Color.parseColor("#2E7D32")
            StatutStation.EN_MAINTENANCE -> Color.parseColor("#EF6C00")
            StatutStation.HORS_SERVICE -> Color.GRAY
        }
        DrawableCompat.setTint(drawable, tintColor)
    }

private fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(context: Context): Location? {
    return try {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        providers.asSequence()
            .filter { manager.isProviderEnabled(it) }
            .mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
    } catch (_: Throwable) {
        null
    }
}

private fun fallbackLocation(): Location =
    Location("fallback").apply {
        latitude = 48.8566
        longitude = 2.3522
    }

private fun applyLastKnownLocationOrNotify(
    context: Context,
    mapView: MapView,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onLocation: (Location) -> Unit
) {
    val realLocation = getLastKnownLocation(context)
    val location = realLocation ?: fallbackLocation()
    onLocation(location)
    centerOnUserLocation(mapView, location)
    if (realLocation == null) {
        scope.launch { snackbarHostState.showSnackbar("Position approximative utilisée") }
    }
}

private fun centerOnUserLocation(mapView: MapView, location: Location) {
    mapView.post {
        try {
            mapView.controller.setZoom(6.5)
            mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))
        } catch (_: Throwable) {
            // Carte non prête ou état invalide : ignorer sans planter
        }
    }
}
