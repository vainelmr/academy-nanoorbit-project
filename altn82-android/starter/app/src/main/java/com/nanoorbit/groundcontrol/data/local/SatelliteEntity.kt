package com.nanoorbit.groundcontrol.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nanoorbit.groundcontrol.data.models.FormatCubeSat
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import java.time.LocalDate

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey val idSatellite: String,
    val nomSatellite: String,
    val statut: String,
    val formatCubesat: String,
    val idOrbite: String,
    val dateLancement: String?,
    val masse: Double?,
    val cachedAt: Long
)

fun Satellite.toEntity(cachedAt: Long): SatelliteEntity = SatelliteEntity(
    idSatellite = idSatellite,
    nomSatellite = nomSatellite,
    statut = statut.name,
    formatCubesat = formatCubesat.name,
    idOrbite = idOrbite,
    dateLancement = dateLancement?.toString(),
    masse = masse,
    cachedAt = cachedAt
)

fun SatelliteEntity.toDomain(): Satellite = Satellite(
    idSatellite = idSatellite,
    nomSatellite = nomSatellite,
    statut = StatutSatellite.valueOf(statut),
    formatCubesat = FormatCubeSat.valueOf(formatCubesat),
    idOrbite = idOrbite,
    dateLancement = dateLancement?.let(LocalDate::parse),
    masse = masse
)
