# Compte Rendu — TP Android Contacts (Version 3)
**Module : Développement Mobile Android • Kotlin**  
**Auteur : Mehdi Benomrane	 
**Projet : Contacts V3 (Fragments, RecyclerView, Filtrage, Google Maps)**  

---

## 1. Introduction et Objectifs
Le présent TP fait évoluer l'application de carnet d'adresses (version 2) vers une architecture moderne d'entreprise. Les objectifs principaux réalisés sont :
1. Transition d'une architecture multi-activités vers une **architecture à activité unique (Single-Activity)** hébergeant des **Fragments dynamiques**.
2. Remplacement de la `ListView` par une **`RecyclerView` personnalisée** avec motif **ViewHolder** et calcul de différentiel (`DiffUtil`).
3. Implémentation d'un **système de filtrage en temps réel** insensible à la casse sur le nom, prénom et téléphone.
4. Extension du modèle métier avec des **coordonnées GPS** (latitude et longitude) avec validation stricte.
5. Intégration interactive du **SDK Google Maps** pour localiser le contact avec un marqueur personnalisé et centrage automatique de la caméra.
6. Sécurisation rigoureuse de la **clé d'API Google Maps** exclue de tout contrôle de version.

---

## 2. Architecture Logicielle et Navigation

### 2.1 Single-Activity Architecture
L'activité unique `MainActivity` sert de conteneur d'hébergement via un `FragmentContainerView` (`R.id.fragment_container`).
* **Gestion du cycle de vie et de la rotation** : Afin de respecter la contrainte technique évitant l'empilement des fragments en double lors d'un changement d'orientation (rotation portrait/paysage), `MainActivity` conditionne l'ajout du premier fragment au test `savedInstanceState == null`. En cas de recréation d'activité, le `FragmentManager` restaure automatiquement l'arborescence et l'état des fragments.

```kotlin
if (savedInstanceState == null) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, ListeContactsFragment())
        .commit()
}
```

### 2.2 Navigation et Pile Arrière (BackStack)
Les transitions entre fragments s'effectuent par transactions dynamiques :
* `ListeContactsFragment` ➔ `DetailContactFragment` : Remplacement avec `addToBackStack(null)` et passage de l'identifiant `contact.id` via un `Bundle`.
* `DetailContactFragment` ➔ `CarteContactFragment` : Remplacement avec `addToBackStack(null)` et passage des coordonnées et du nom.
* Retours arrières : Appel à `parentFragmentManager.popBackStack()` assurant un retour immédiat sans recréer inutilement les instances précédentes.

### 2.3 Choix du mode de communication : Shared ViewModel
Nous avons opté pour un **Shared ViewModel** (`ContactViewModel` hébergé au niveau de l'activité via `activityViewModels()`) plutôt qu'uniquement le `Fragment Result API`.
* **Justification** :
  1. Le `ContactViewModel` constitue une **source unique de vérité** en mémoire partagée entre tous les fragments.
  2. Il résiste intrinsèquement aux changements de configuration (rotation d'écran).
  3. Il offre une réactivité immédiate grâce aux `LiveData` : toute modification, ajout ou suppression enregistrée dans `DetailContactFragment` se répercute instantanément dans `ListeContactsFragment`.
  4. Le filtrage de la liste s'exécute directement dans le ViewModel via un `MediatorLiveData`, assurant une séparation claire entre logique de présentation et logique métier.

---

## 3. RecyclerView et Filtrage Dynamique

### 3.1 Rôle du RecyclerView.Adapter et du ViewHolder
* **`ContactAdapter`** : Gère l'inflation des vues XML (`item_contact.xml`), adapte les données du modèle et gère les événements de clic.
* **`ContactViewHolder`** : Maintient en cache les pointeurs de vues (`findViewById`) pour `ivPhoto`, `tvNomPrenom`, `tvTelephone` et le badge GPS. Ce recyclage évite les saccades lors du défilement.

### 3.2 Stratégie de Filtrage et Prévention des Erreurs d'Indice
* Un `TextWatcher` est branché sur le champ de recherche `etSearch`. À chaque modification du texte, la méthode `viewModel.setSearchQuery(query)` est invoquée.
* Le filtrage teste si la chaîne de recherche (en minuscules et sans espaces superflus) est contenue dans :
  * Le nom (`contact.nom.lowercase()`)
  * Le prénom (`contact.prenom.lowercase()`)
  * Le numéro de téléphone (`contact.telephone`)
* **Résolution de l'erreur d'indice** :
  Lorsqu'une liste est filtrée, l'index visuel ne correspond plus à l'index de la liste originale. Pour éliminer tout risque d'erreur ou d'écrasement du mauvais contact, le clic sur la ligne transmet l'identifiant immuable `contact.id`. Le fragment détail charge le contact correspondant à cet ID unique.

---

## 4. Coordonnées GPS et Intégration Google Maps

### 4.1 Modèle de données étendu
Le modèle `Contact` intègre deux propriétés optionnelles :
```kotlin
var latitude: Double? = null,
var longitude: Double? = null
```
Une propriété calculée `hasValidCoordinates` valide que :
* $\text{Latitude} \in [-90.0, +90.0]$
* $\text{Longitude} \in [-180.0, +180.0]$

### 4.2 Saisie et Validation dans `DetailContactFragment`
* Les champs `etLatitude` et `etLongitude` autorisent la saisie de nombres décimaux signés.
* Lors du clic sur **Enregistrer** ou sur **Voir sur la carte**, un contrôle strict est appliqué. En cas de format invalide ou de valeur hors bornes, un message d'erreur précis est affiché.

### 4.3 Affichage Cartographique dans `CarteContactFragment`
* Implémente `OnMapReadyCallback` via `SupportMapFragment`.
* Positionne un marqueur (`MarkerOptions`) à la position GPS du contact et anime la caméra avec `CameraUpdateFactory.newLatLngZoom(latLng, 14f)`.
* En cas de contact sans position GPS (ex: Lucas Petit), une bannière jaune d'avertissement explicite est affichée, évitant tout plantage.

---

## 5. Sécurité et Gestion des Secrets GitHub
Pour éviter d'exposer la clé API Google Maps :
1. La clé est inscrite dans `local.properties` (ou `secrets.properties`), fichier formellement ignoré dans `.gitignore`.
2. Le fichier `app/build.gradle.kts` charge cette clé dynamiquement :
   ```kotlin
   manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
   ```
3. Le fichier `AndroidManifest.xml` utilise la variable d'environnement `${MAPS_API_KEY}`.

---

## 6. Réponses aux Questions de Compréhension

1. **Différence entre Activity et Fragment :**  
   Une Activity est un composant système représentant une fenêtre d'application complète avec son propre cycle de vie système. Un Fragment est un composant visuel modulaire hébergé dans une Activity, permettant un découpage flexible d'écran et une composition dynamique.

2. **Intérêt de FragmentContainerView et addToBackStack() :**  
   `FragmentContainerView` est le conteneur optimisé AndroidX pour héberger les fragments en traitant correctement les transitions et les insets système. `addToBackStack()` insère la transaction dans la pile d'historique du `FragmentManager`, autorisant le retour arrière naturel sans quitter l'application.

3. **Rôle de RecyclerView.Adapter et de ViewHolder :**  
   L'Adapter associe le jeu de données aux vues graphiques (`onCreateViewHolder` et `onBindViewHolder`). Le ViewHolder met en cache les références des composants d'une ligne pour éviter les appels coûteux à `findViewById()` pendant le défilement.

4. **Éviter une erreur d'indice après filtrage :**  
   Ne jamais utiliser la position numérique de la ligne pour manipuler la liste globale. Transmettre toujours l'identifiant unique `contact.id` et rechercher l'objet correspondant dans le ViewModel via son ID.

5. **Avantage de transmettre l'ID plutôt que la position :**  
   La position est variable et volatile (altérée par les filtres ou les tris). L'ID est invariable, garantissant l'intégrité de la donnée consultée même après filtrage ou réorientation d'écran.

6. **Protection de la clé Google Maps sur GitHub :**  
   En isolant la clé dans le fichier local non versionné `local.properties` (inscrit dans `.gitignore`) et en l'injectant au build via les `manifestPlaceholders`.

---

## 7. Bilan et Validation des Tests
* **Tests unitaires automatisés** : Validés avec succès (`BUILD SUCCESSFUL in 1m 2s`).
* **Validation fonctionnelle** : Recherche temps réel, ajout/modification/suppression réactifs, intents d'appel et d'email, appareil photo, galerie, carte Google Maps avec marqueur et navigation fluide.
