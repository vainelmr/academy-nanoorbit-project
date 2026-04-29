# NanoOrbit GroundControl (Jetpack Compose)

## Présentation du projet

NanoOrbit GroundControl est une application Android de supervision opérationnelle.
Elle permet de visualiser les satellites, les stations sol et les fenêtres de communication, avec une interface orientée exploitation.

L'objectif principal est de proposer un outil mobile cohérent pour suivre l'état du système spatial, planifier les communications et consulter la cartographie des stations.

## Architecture générale

L'application suit une architecture en couches simple et maintenable :

- **Jetpack Compose (UI)** : écrans déclaratifs, état lisible, composants réutilisables.
- **Navigation Compose** : gestion claire des routes entre Dashboard, détails, planning et carte.
- **ViewModel** : conservation de l'état UI et orchestration des actions utilisateur.
- **Repository** : point central d'accès aux données, isolant l'UI des sources de données.
- **Room (base locale)** : persistance embarquée pour le fonctionnement hors-ligne et la continuité de service.

Cette structure facilite les tests, limite le couplage entre couches et clarifie les responsabilités.

## Synergies

### 3.1 Modèles de données

Les modèles Android reprennent la structure métier du sujet :
`Satellite`, `StationSol`, `Fenetre`.

La même logique de domaine est conservée entre :

- les données de démonstration (`MockData`),
- les entités persistées (Room Entities),
- les objets manipulés par l'UI via le repository.

Cette cohérence de modèle garantit une compréhension unique du métier dans toutes les couches (UI, data, repository), sans divergence de structure.

### 3.2 Règles métier RG-F04

Les validations métier sont appliquées côté client dans `PlanningScreen` avant toute action utilisateur.

Exemples de règles RG-F04 respectées :

- durée de fenêtre comprise entre **1 et 900 secondes**,
- interdiction de planifier une communication avec un **satellite désorbité**.

Ces contrôles en amont empêchent la création d'états incohérents et assurent la conformité métier directement dans le flux UI.

### 3.3 Mode hors-ligne (Q3 continuité de service)

La stratégie choisie est **cache-first** :

1. lecture locale prioritaire via Room,
2. tentative de rafraîchissement distant ensuite,
3. si échec réseau/API, bascule en mode hors-ligne.

Les données locales restent disponibles, l'application continue de fonctionner, et un message utilisateur informe explicitement de l'état hors-ligne.

## Choix techniques

- **Jetpack Compose** : UI moderne, moins de code impératif, meilleure lisibilité des états.
- **Navigation Compose** : navigation typée et intégrée à l'écosystème Compose.
- **Room** : persistance robuste, requêtes locales fiables, support natif Kotlin.
- **Repository** : séparation claire entre présentation et accès données.
- **osmdroid (OpenStreetMap)** : cartographie sans dépendance Google, adaptée à la contrainte académique imposant l'absence de Google Play Services pour la carte.

Ces choix privilégient la clarté, la robustesse et l'alignement avec les contraintes pédagogiques du projet.

## Justification du fallback GPS

La géolocalisation de la carte repose sur `LocationManager` avec `getLastKnownLocation`.
Cette approche est volontairement simple et conforme au sujet (sans `FusedLocationProviderClient` ni dépendance Play Services).

Limite connue : `getLastKnownLocation` peut retourner `null` (GPS désactivé, pas de fix récent, appareil sans signal).

Solution mise en place :

- fallback vers une position approximative par défaut (Paris),
- centrage de la carte même en absence de position réelle,
- affichage d'une Snackbar : **"Position approximative utilisée"**.

Cette stratégie renforce la robustesse (pas de crash, comportement déterministe) et garantit un fonctionnement fluide  même sans conditions GPS idéales.

## Explication cache-first

Le flux de données est simple :

1. l'application lit d'abord les données locales,
2. elle tente ensuite une mise à jour distante,
3. en cas d'échec, elle conserve les données locales.

Bénéfices :

- continuité de service,
- fonctionnement hors-ligne,
- meilleure expérience utilisateur (contenu immédiatement disponible).

## Fonctionnalités principales

- **Dashboard** : liste des satellites avec recherche.
- **DetailScreen** : affichage détaillé d'un satellite.
- **Planning** : gestion des fenêtres et validation des règles métier.
- **Map** : visualisation des stations sol et géolocalisation utilisateur.

## Lancement du projet

1. Compiler :

   `./gradlew assembleDebug`

   (Sous Windows PowerShell : `.\gradlew.bat assembleDebug`)

2. Exécuter :

   - ouvrir le projet dans Android Studio,
   - lancer l'application via **Run** sur émulateur ou appareil physique.

## Conclusion

Le projet met en oeuvre une architecture Android moderne, respecte les contraintes techniques du sujet NanoOrbit et démontre une attention particulière à la robustesse (fallback GPS, mode hors-ligne, validations métier).
Il répond ainsi aux attentes en combinant cohérence technique, justification des choix et qualité d'expérience utilisateur.
