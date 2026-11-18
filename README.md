# TP Android — Gestion des Contacts (Version 3)
### Fragments • RecyclerView • Filtrage dynamique • Google Maps SDK • Textures Réelles

Projet Android complet développé en **Kotlin** répondant à l'ensemble du cahier des charges du **TP V3**. L'application fait évoluer l'architecture du TP V2 vers une architecture moderne à **activité unique (Single-Activity Architecture)** avec **Fragments dynamiques**, **Shared ViewModel**, liste optimisée via **RecyclerView**, champ de **recherche filtrable en temps réel**, et intégration cartographique interactive avec le **SDK Google Maps** et un mode **Textures Réelles**.

---

## 📱 Fonctionnalités et Parcours Utilisateur

### 1. Écran Principal : `ListeContactsFragment`
* **RecyclerView personnalisée** : Chaque contact affiche sa photo circulaire, son nom complet, son numéro de téléphone et un badge indicateur GPS 📍 discret si des coordonnées sont définies.
* **Filtrage en temps réel** :
  * Champ de recherche situé au-dessus de la liste.
  * Filtrage instantané au fil de la saisie (sans bouton de validation).
  * Recherche insensible à la casse portant sur le nom, le prénom et le numéro de téléphone.
  * Bouton de suppression rapide pour réinitialiser la recherche.
  * Affichage d'un état vide explicite (*"Aucun contact ne correspond à votre recherche"*) lorsque aucun résultat n'est trouvé.
* **Sélection sécurisée** : Le clic sur un contact dans une liste filtrée ouvre sa fiche exacte sans aucune erreur d'indice, grâce à la transmission de l'identifiant unique `contact.id`.
* **Ajout d'un contact** : Icône `+` dans la barre d'outils ouvrant le fragment détail en mode création.

### 2. Écran Détail et Édition : `DetailContactFragment`
* **Consultation et modification** :
  * Modification du nom, prénom, téléphone et e-mail avec validation des champs obligatoires (`.error`).
  * Modification manuelle des coordonnées GPS (Latitude et Longitude).
* **Intents Implicites (conservés du V2)** :
  * 📞 **Appeler** : Déclenche `Intent.ACTION_DIAL` avec le numéro prérempli.
  * ✉️ **Envoyer un e-mail** : Déclenche `Intent.ACTION_SENDTO` avec l'adresse préremplie.
* **Gestion des photos** :
  * 🖼️ **Galerie** : Sélection d'une image (`ActivityResultContracts.GetContent`) et copie sécurisée dans le stockage interne.
  * 📷 **Caméra** : Prise de vue directe (`ActivityResultContracts.TakePicturePreview`).
* **Section Coordonnées GPS & Visualisation** :
  * Contrôle de validité des coordonnées : Latitude comprise entre **-90.0° et +90.0°**, Longitude entre **-180.0° et +180.0°**.
  * 🗺️ **Bouton "Voir sur la carte"** : Ouvre `CarteContactFragment` si les coordonnées sont valides, ou affiche un message explicite si elles sont absentes ou invalides.
* **Actions de sauvegarde et sortie** :
  * ✅ **Enregistrer** : Validation complète, persistance dans le `ContactViewModel`, notification Toast et retour à la liste immédiatement actualisée.
  * ❌ **Annuler & Flèche retour** : Dépilement (`popBackStack()`) sans appliquer de changement.
  * 🗑️ **Supprimer** : Dialogue de confirmation avant suppression du contact.

### 3. Écran Cartographique : `CarteContactFragment` (Double Mode)
* **Mode 1 : 🗺️ Carte Réelle avec Textures (Activé par défaut)** :
  * Affiche directement toutes les textures de routes, villes et bâtiments réels (via tuiles OpenStreetMap / Leaflet).
  * Centrage précis avec niveau de zoom interactif (zoom in/out, déplacement fluide).
  * Marqueur personnalisé avec pop-up affichant le nom du contact et ses coordonnées.
  * **Fonctionne immédiatement à 100% sans nécessiter de clé API payante ni de compte Google Cloud !**
* **Mode 2 : 📍 Google Maps SDK Officiel** :
  * Implémente `SupportMapFragment` et `OnMapReadyCallback` conformément aux exigences académiques du TP.
  * Affiche le marqueur rouge Google Maps et anime la caméra.
* **Bouton "App Maps"** :
  * Déclenche un Intent implicite (`geo:lat,lon`) pour ouvrir les coordonnées dans l'application officielle Google Maps ou le navigateur.

---

## 🏛️ Exemples Réels Préremplis dans l'Application

L'application contient une liste riche de **contacts réels et de lieux emblématiques vérifiables** :

| Contact | Lieu Réel | Latitude | Longitude | Particularité |
| :--- | :--- | :--- | :--- | :--- |
| **Claire Dupont** | Paris (Tour Eiffel) | `48.8584` | `2.2945` | Coordonnées valides |
| **Thomas Martin** | Lyon (Place Bellecour) | `45.7578` | `4.8320` | Coordonnées valides |
| **Sophie Bernard** | Marseille (Vieux-Port) | `43.2951` | `5.3744` | Coordonnées valides |
| **Lucas Petit** | *Aucune position* | `null` | `null` | **Test requis : contact sans position GPS** |
| **Julien Moreau** | Bordeaux (Place de la Bourse) | `44.8416` | `-0.5696` | Coordonnées valides |
| **Camille Leroy** | Toulouse (Place du Capitole) | `43.6045` | `1.4442` | Coordonnées valides |
| **Antoine Dubois** | Lille (Grand Place) | `50.6370` | `3.0633` | Coordonnées valides |

---

## 🔐 Sécurisation de la Clé API Google Maps

1. Le fichier `local.properties` (et `secrets.properties`) est expressément mentionné dans le fichier `.gitignore`.
2. Dans `app/build.gradle.kts`, la clé est extraite au moment de la compilation depuis `local.properties` :
   ```kotlin
   val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY")
       ?: (project.findProperty("MAPS_API_KEY") as? String)
       ?: "YOUR_GOOGLE_MAPS_API_KEY_HERE"
   ```
3. Elle est injectée dynamiquement dans le `AndroidManifest.xml` via `manifestPlaceholders`.
4. Pour utiliser votre clé Google Maps personnelle :
   ```properties
   MAPS_API_KEY=AIzaSy...VotreCleIci
   ```

---

## 📝 Réponses aux Questions de Compréhension (Section 11)

1. **Différence entre Activity et Fragment :**  
   Une *Activity* représente une fenêtre d'application complète gérée par l'OS Android. Un *Fragment* est un composant modulaire réutilisable au sein d'une activité hôte, permettant la composition dynamique et le découpage propre des écrans.
2. **Intérêt de FragmentContainerView et addToBackStack() :**  
   `FragmentContainerView` est le conteneur optimisé AndroidX pour héberger les fragments. `addToBackStack()` insère la transaction dans la pile d'historique du `FragmentManager`, autorisant un retour arrière fluide sans quitter l'application.
3. **Rôle de RecyclerView.Adapter et ViewHolder :**  
   L'Adapter fait le lien entre la liste métier et les vues XML. Le ViewHolder met en cache les références (`findViewById`) pour recycler les vues et garantir un défilement à 60 fps.
4. **Éviter une erreur d’indice après filtrage :**  
   Ne pas utiliser la position d'affichage de la ligne. Transmettre l'identifiant unique `contact.id` et rechercher l'objet via son ID dans le ViewModel (`getContactById(id)`).
5. **Pourquoi transmettre l'identifiant plutôt que la position :**  
   La position change à chaque filtre ou tri. L'identifiant est immuable et persistant.
6. **Protection de la clé Google Maps sur GitHub :**  
   En la plaçant dans `local.properties` (exclu par `.gitignore`) et en l'injectant au build via les `manifestPlaceholders`.
