package com.nanoorbit.groundcontrol.data.models

import java.time.LocalDate
import java.time.LocalDateTime

enum class StatutSatellite {
    OPERATIONNEL,
    EN_VEILLE,
    DEFAILLANT,
    DESORBITE
}

enum class FormatCubeSat {
    U1,
    U3,
    U6,
    U12
}

enum class StatutFenetre {
    PLANIFIEE,
    REALISEE,
    ANNULEE
}

enum class TypeOrbite {
    SSO,
    LEO,
    MEO,
    GEO
}

enum class StatutStation {
    OPERATIONNELLE,
    EN_MAINTENANCE,
    HORS_SERVICE
}

// Correspond à la table Oracle SATELLITE (id_satellite, nom_satellite, statut,
// format_cubesat, id_orbite, date_lancement, masse).
data class Satellite(
    val idSatellite: String,
    val nomSatellite: String,
    val statut: StatutSatellite,
    val formatCubesat: FormatCubeSat,
    val idOrbite: String,
    val dateLancement: LocalDate? = null,
    val masse: Double? = null
)

// Correspond à la table Oracle ORBITE (id_orbite, type_orbite).
data class Orbite(
    val idOrbite: String,
    val typeOrbite: TypeOrbite,
    val altitude: Double,
    val inclinaison: Double,
    val zoneCouverture: String? = null
)

// Correspond à la table Oracle INSTRUMENT (ref_instrument, type_instrument, modele, resolution).
data class Instrument(
    val refInstrument: String,
    val typeInstrument: String,
    val modele: String,
    val resolution: Double? = null,
    val consommation: Double? = null
)

// Correspond à la table Oracle FENETRE_COM (id_fenetre, id_satellite, code_station,
// datetime_debut, duree, statut, volume_donnees).
data class FenetreCom(
    val idFenetre: String,
    val idSatellite: String,
    val codeStation: String,
    val datetimeDebut: LocalDateTime,
    val duree: Int,
    val statut: StatutFenetre,
    val volumeDonnees: Double? = null
) {
    init {
        require(duree in 1..900) { "La duree doit etre comprise entre 1 et 900 secondes." }
    }
}

// Correspond à la table Oracle STATION_SOL (code_station, nom_station, latitude, longitude, statut).
data class StationSol(
    val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val statut: StatutStation
)

// Correspond à la table Oracle MISSION (id_mission, nom_mission, objectif, date_debut, date_fin, statut_mission).
data class Mission(
    val idMission: String,
    val nomMission: String,
    val objectif: String,
    val dateDebut: LocalDate,
    val dateFin: LocalDate? = null,
    val statutMission: String
)
