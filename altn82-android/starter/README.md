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

### 3.2 Règles métier RG-F04 / RG-S06

Les validations métier sont appliquées côté client dans `PlanningScreen` (affichage des incohérences par fenêtre).

- **RG-F04** : durée de fenêtre comprise entre **1 et 900 secondes** (contrôle UI + `require` sur le modèle `FenetreCom`).
- **RG-S06** : message explicite lorsqu’un satellite est **DESORBITE** (nouvelle communication interdite côté métier, aligné avec le trigger Oracle).

Ces contrôles en amont illustrent la cohérence avec le fil rouge NanoOrbit sans envoi incohérent.

### 3.3 Mode hors-ligne (Q3 continuité de service)

La stratégie choisie est **cache-first** :

1. lecture locale prioritaire via Room,
2. tentative de rafraîchissement distant ensuite,
3. si échec réseau/API, bascule en mode hors-ligne.

Les données locales restent disponibles, l'application continue de fonctionner, et un message utilisateur informe explicitement de l'état hors-ligne.

Cette logique est reliée à la **synergie entre la partie BDD et la partie Android** (voir commentaire dans `NanoOrbitRepository`) : continuité si le serveur central est indisponible.

**Démarrage de l’application** : ouverture Room et refresh initial sont effectués **hors thread principal** (`Dispatchers.IO`), avec un léger décalage, pour limiter l’iowait et les ANR sur émulateur.

### 3.4 Checklist synthèse Phase 3 (fonctionnel)

| Thème | Détail |
|---|---|
| **Synergie des modèles** | Données satellites / fenêtres persistées en Room et alignées avec le domaine métier ; jeu de démonstration `MockData` cohérent avec le fil rouge NanoOrbit |
| **RG-F04 / RG-S06** | Durée fenêtre `[1, 900]` ; message si satellite `DESORBITE` (planning) ; validation côté modèle `FenetreCom` |
| **Hors-ligne & cache** | `cachedAt` sur entités Room, refresh simulé puis écriture locale, bannière hors-ligne et âge du cache sur le dashboard |
| **Planning** | Liste des fenêtres issue du **flux Room** (ViewModel) ; stations de référence via `MockData` |
| **Carte** | **osmdroid** : marqueurs stations, couleurs selon statut, infobulle ; **fallback GPS** (position par défaut + Snackbar si pas de position réelle) |
| **Bonus** | Pull-to-refresh (dashboard) ; favoris (DataStore Preferences) ; rappels fenêtres **PLANIFIÉE** par **WorkManager** (15 min avant si la date est dans le futur) |

### Notifications locales (bonus)

Les notifications locales sont implémentées avec **WorkManager** (`PlanningWindowNotificationWorker`, canal « Planning communications »).

**Démonstration en soutenance**

Afin de pouvoir montrer des rappels quelques minutes après le lancement, certaines fenêtres **PLANIFIEE** dans `MockData.kt` utilisent volontairement des dates futures :

`LocalDateTime.now().plusMinutes(...)`

Le jeu reste compatible avec des **dates métier réelles** (il suffit de rétablir des `LocalDateTime` fixes).

**Planification automatique au démarrage**

Le comportement peut être activé ou désactivé via la constante **`AUTO_SCHEDULE_NOTIFICATIONS`** dans **`NanoOrbitViewModel.kt`** :

- **`true`** → replanification automatique des notifications lors des mises à jour du flux Room combiné (satellites + fenêtres).
- **`false`** (valeur actuelle pour le rendu) → **pas** de replanification automatique, afin d’éviter une surcharge I/O concurrente (Room + refresh + WorkManager) au lancement sur émulateur.

**Déclenchement manuel**

La méthode **`scheduleNotificationsForDemo()`** sur le `NanoOrbitViewModel` permet de lancer la planification **à la demande** (après chargement des données), par exemple depuis le débogueur ou un futur bouton dédié.

### Difficultés rencontrées

Le principal problème rencontré concernait les **performances au démarrage** sur émulateur Android.

L’application enchaînait en parallèle ou presque :

- l’ouverture **Room** et requêtes **SQLite**,
- la planification **WorkManager** (annulation + files d’attente),
- les **refresh** de données (simulation réseau + écriture Room),
- les accès **DataStore** (favoris).

Cette accumulation d’opérations **I/O** au lancement provoquait :

- des ralentissements importants,
- des **frames sautées**,
- des **ANR** (« failed to complete startup ») avec fort **iowait**.

**Correctifs appliqués (sans changer l’architecture fonctionnelle)** :

- accès **Room / construction du repository** déplacés sur **`Dispatchers.IO`** ;
- **refresh initial** différé (`delay` court) puis exécuté sur **IO** ;
- planification automatique des notifications rendue **optionnelle** (`AUTO_SCHEDULE_NOTIFICATIONS`) ;
- mises à jour **`StateFlow`** conditionnées (`!=`) et flux combiné avec **`distinctUntilChanged`** pour limiter les recompositions inutiles ;
- **MapScreen (osmdroid)** : marqueurs créés après attache de la vue (`post`, garde `windowToken`, `runCatching`).

Ces ajustements ont permis un **démarrage stable**, un **dashboard affiché rapidement** et la **disparition des ANR** constatés sur la configuration de test.

### État des bonus (au rendu)

| Bonus | Statut |
|------|--------|
| **Pull-to-refresh** (dashboard) | Fonctionnel — déclenche `pullRefreshDashboard()` avec indicateur Material. |
| **Favoris** (DataStore Preferences) | Fonctionnel — toggle sur les cartes et écran détail, filtre « Favoris ». |
| **Notifications locales** (WorkManager) | Fonctionnel — planifier via `scheduleNotificationsForDemo()` ou activer `AUTO_SCHEDULE_NOTIFICATIONS`. |
| **Mode hors-ligne** (cache-first) | Fonctionnel — bannière + message repository ; test possible via `SIMULATE_NETWORK_FAILURE` dans `NanoOrbitRepository` (debug). |

## Choix techniques

- **Jetpack Compose** : UI moderne, moins de code impératif, meilleure lisibilité des états.
- **Navigation Compose** : navigation typée et intégrée à l'écosystème Compose.
- **Room** : persistance robuste, requêtes locales fiables, support natif Kotlin.
- **Repository** : séparation claire entre présentation et accès données.
- **DataStore Preferences** : stockage léger des satellites favoris (clé utilisateur sans surcharger Room).
- **WorkManager** : planification d’une notification locale avant les fenêtres planifiées (robuste sous contraintes d’optimisation batterie ; les dates du jeu peuvent être passées selon l’horloge appareil).
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

- **Dashboard** : liste des satellites, recherche, indicateur de chargement, **pull-to-refresh**, **favoris** et filtre associé.
- **DetailScreen** : détail satellite, instruments / missions (jeu ALTN83), signalement d’anomalie (dialogue), favori.
- **Planning** : fenêtres depuis **Room** (ViewModel), filtre station, totaux, badges de statut, contrôles RG-F04 / satellite désorbité.
- **Map** : stations **osmdroid**, marqueurs colorés, infobulle, **Me localiser** avec **fallback GPS** (sans Play Services).

## Lancement du projet

1. Compiler :

   `./gradlew assembleDebug`

   (Sous Windows PowerShell : `.\gradlew.bat assembleDebug`)

2. Exécuter :

   - ouvrir le projet dans Android Studio,
   - lancer l'application via **Run** sur émulateur ou appareil physique.

## Conclusion

Le projet met en œuvre une architecture Android moderne (Compose, Room, repository cache-first), respecte les contraintes du sujet NanoOrbit et intègre des correctifs de **stabilité au démarrage** adaptés à l’émulateur (I/O différé, notifications optionnelles au boot).
Les bonus (pull-to-refresh, favoris, WorkManager) restent activables et documentés ci-dessus.
