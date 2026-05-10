package com.nanoorbit.groundcontrol.data.mock

import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.FormatCubeSat
import com.nanoorbit.groundcontrol.data.models.Instrument
import com.nanoorbit.groundcontrol.data.models.Mission
import com.nanoorbit.groundcontrol.data.models.Orbite
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.models.StatutFenetre
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.data.models.StatutStation
import com.nanoorbit.groundcontrol.data.models.StationSol
import com.nanoorbit.groundcontrol.data.models.TypeOrbite
import java.time.LocalDate
import java.time.LocalDateTime

data class MissionParticipationRef(
    val idMission: String,
    val roleSatellite: String
)

object MockData {
    /** Clé = [Satellite.idSatellite], valeurs = [Instrument.refInstrument] embarqués. */
    val instrumentRefsBySatelliteId: Map<String, List<String>> = mapOf(
        "SAT-001" to listOf("INS-CAM-01", "INS-IR-01"),
        "SAT-002" to listOf("INS-CAM-01"),
        "SAT-003" to listOf("INS-CAM-01", "INS-SPEC-01"),
        "SAT-004" to listOf("INS-IR-01"),
        "SAT-005" to listOf("INS-AIS-01")
    )

    /** Clé = [Satellite.idSatellite] ; correspond à la table PARTICIPATION. */
    val missionParticipationBySatelliteId: Map<String, List<MissionParticipationRef>> = mapOf(
        "SAT-001" to listOf(
            MissionParticipationRef("MSN-ARC-2023", "Imageur principal"),
            MissionParticipationRef("MSN-DEF-2022", "Imageur principal")
        ),
        "SAT-002" to listOf(MissionParticipationRef("MSN-ARC-2023", "Imageur secondaire")),
        "SAT-003" to listOf(
            MissionParticipationRef("MSN-ARC-2023", "Satellite de relais"),
            MissionParticipationRef("MSN-COAST-2024", "Imageur principal")
        ),
        "SAT-004" to listOf(MissionParticipationRef("MSN-COAST-2024", "Satellite de secours")),
        "SAT-005" to listOf(MissionParticipationRef("MSN-DEF-2022", "Imageur secondaire"))
    )

    val orbites = listOf(
        Orbite(
            idOrbite = "ORB-001",
            typeOrbite = TypeOrbite.SSO,
            altitude = 550.0,
            inclinaison = 97.6,
            zoneCouverture = "Polaire globale — Europe / Arctique"
        ),
        Orbite(
            idOrbite = "ORB-002",
            typeOrbite = TypeOrbite.SSO,
            altitude = 700.0,
            inclinaison = 98.2,
            zoneCouverture = "Polaire globale — haute latitude"
        ),
        Orbite(
            idOrbite = "ORB-003",
            typeOrbite = TypeOrbite.LEO,
            altitude = 400.0,
            inclinaison = 51.6,
            zoneCouverture = "Équatoriale — zone tropicale"
        )
    )

    val satellites = listOf(
        Satellite(
            idSatellite = "SAT-001",
            nomSatellite = "NanoOrbit-Alpha",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U3,
            idOrbite = "ORB-001",
            dateLancement = LocalDate.parse("2022-03-15"),
            masse = 1.30
        ),
        Satellite(
            idSatellite = "SAT-002",
            nomSatellite = "NanoOrbit-Beta",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U3,
            idOrbite = "ORB-001",
            dateLancement = LocalDate.parse("2022-03-15"),
            masse = 1.30
        ),
        Satellite(
            idSatellite = "SAT-003",
            nomSatellite = "NanoOrbit-Gamma",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U6,
            idOrbite = "ORB-002",
            dateLancement = LocalDate.parse("2023-06-10"),
            masse = 2.00
        ),
        Satellite(
            idSatellite = "SAT-004",
            nomSatellite = "NanoOrbit-Delta",
            statut = StatutSatellite.EN_VEILLE,
            formatCubesat = FormatCubeSat.U6,
            idOrbite = "ORB-002",
            dateLancement = LocalDate.parse("2023-06-10"),
            masse = 2.00
        ),
        Satellite(
            idSatellite = "SAT-005",
            nomSatellite = "NanoOrbit-Epsilon",
            statut = StatutSatellite.DESORBITE,
            formatCubesat = FormatCubeSat.U12,
            idOrbite = "ORB-003",
            dateLancement = LocalDate.parse("2021-11-20"),
            masse = 4.50
        )
    )

    val instruments = listOf(
        Instrument(
            refInstrument = "INS-CAM-01",
            typeInstrument = "Caméra optique",
            modele = "PlanetScope-Mini",
            resolution = 3.0,
            consommation = 2.5
        ),
        Instrument(
            refInstrument = "INS-IR-01",
            typeInstrument = "Infrarouge",
            modele = "FLIR-Lepton-3",
            resolution = 160.0,
            consommation = 1.2
        ),
        Instrument(
            refInstrument = "INS-AIS-01",
            typeInstrument = "Récepteur AIS",
            modele = "ShipTrack-V2",
            resolution = null,
            consommation = 0.8
        ),
        Instrument(
            refInstrument = "INS-SPEC-01",
            typeInstrument = "Spectromètre",
            modele = "HyperSpec-Nano",
            resolution = 30.0,
            consommation = 3.1
        )
    )

    val stations = listOf(
        StationSol(
            codeStation = "GS-TLS-01",
            nomStation = "Toulouse Ground Station",
            latitude = 43.6047,
            longitude = 1.4442,
            statut = StatutStation.OPERATIONNELLE
        ),
        StationSol(
            codeStation = "GS-KIR-01",
            nomStation = "Kiruna Arctic Station",
            latitude = 67.8557,
            longitude = 20.2253,
            statut = StatutStation.OPERATIONNELLE
        ),
        StationSol(
            codeStation = "GS-SGP-01",
            nomStation = "Singapore Station",
            latitude = 1.3521,
            longitude = 103.8198,
            statut = StatutStation.EN_MAINTENANCE
        )
    )

    val missions = listOf(
        Mission(
            idMission = "MSN-ARC-2023",
            nomMission = "ArcticWatch 2023",
            objectif = "Surveillance fonte des glaces et dynamique des banquises",
            dateDebut = LocalDate.parse("2023-01-01"),
            dateFin = null,
            statutMission = "Active"
        ),
        Mission(
            idMission = "MSN-DEF-2022",
            nomMission = "DeforestAlert",
            objectif = "Détection et cartographie de la déforestation en temps quasi-réel",
            dateDebut = LocalDate.parse("2022-06-01"),
            dateFin = LocalDate.parse("2023-05-31"),
            statutMission = "Terminée"
        ),
        Mission(
            idMission = "MSN-COAST-2024",
            nomMission = "CoastGuard 2024",
            objectif = "Surveillance évolution du trait de côte et détection d'érosion",
            dateDebut = LocalDate.parse("2024-03-01"),
            dateFin = null,
            statutMission = "Active"
        )
    )

    val fenetres = listOf(
        FenetreCom(
            idFenetre = "1",
            idSatellite = "SAT-001",
            codeStation = "GS-KIR-01",
            datetimeDebut = LocalDateTime.parse("2024-01-15T09:14:00"),
            duree = 420,
            statut = StatutFenetre.REALISEE,
            volumeDonnees = 1250.0
        ),
        FenetreCom(
            idFenetre = "2",
            idSatellite = "SAT-002",
            codeStation = "GS-TLS-01",
            datetimeDebut = LocalDateTime.parse("2024-01-15T11:52:00"),
            duree = 310,
            statut = StatutFenetre.REALISEE,
            volumeDonnees = 890.0
        ),
        FenetreCom(
            idFenetre = "3",
            idSatellite = "SAT-003",
            codeStation = "GS-KIR-01",
            datetimeDebut = LocalDateTime.parse("2024-01-16T08:30:00"),
            duree = 540,
            statut = StatutFenetre.REALISEE,
            volumeDonnees = 1680.0
        ),
        // Date future utilisée pour démontrer les notifications locales.
        FenetreCom(
            idFenetre = "4",
            idSatellite = "SAT-001",
            codeStation = "GS-TLS-01",
            datetimeDebut = LocalDateTime.now().plusMinutes(20),
            duree = 380,
            statut = StatutFenetre.PLANIFIEE,
            volumeDonnees = null
        ),
        // Date future utilisée pour démontrer les notifications locales.
        FenetreCom(
            idFenetre = "5",
            idSatellite = "SAT-003",
            codeStation = "GS-TLS-01",
            datetimeDebut = LocalDateTime.now().plusMinutes(30),
            duree = 290,
            statut = StatutFenetre.PLANIFIEE,
            volumeDonnees = null
        )
    )
}
