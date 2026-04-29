package com.nanoorbit.groundcontrol.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.StatutFenetre
import java.time.LocalDateTime

@Entity(tableName = "fenetres_com")
data class FenetreEntity(
    @PrimaryKey val idFenetre: String,
    val idSatellite: String,
    val codeStation: String,
    val datetimeDebut: String,
    val duree: Int,
    val statut: String,
    val volumeDonnees: Double?,
    val cachedAt: Long
)

fun FenetreCom.toEntity(cachedAt: Long): FenetreEntity = FenetreEntity(
    idFenetre = idFenetre,
    idSatellite = idSatellite,
    codeStation = codeStation,
    datetimeDebut = datetimeDebut.toString(),
    duree = duree,
    statut = statut.name,
    volumeDonnees = volumeDonnees,
    cachedAt = cachedAt
)

fun FenetreEntity.toDomain(): FenetreCom = FenetreCom(
    idFenetre = idFenetre,
    idSatellite = idSatellite,
    codeStation = codeStation,
    datetimeDebut = LocalDateTime.parse(datetimeDebut),
    duree = duree,
    statut = StatutFenetre.valueOf(statut),
    volumeDonnees = volumeDonnees
)
