package com.example.tp_listview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel partagé entre les Fragments de l'application (hébergé au niveau de MainActivity).
 *
 * Justification architecturale :
 * 1. Source de vérité unique pour la liste des contacts.
 * 2. Survie aux changements de configuration (rotation d'écran) sans perte de données.
 * 3. Découplage complet entre fragments : ListeContactsFragment, DetailContactFragment
 *    et CarteContactFragment communiquent via ce ViewModel partagé.
 * 4. Filtrage dynamique réactif via LiveData.
 */
class ContactViewModel : ViewModel() {

    // Exemples réels avec coordonnées géographiques précises de lieux emblématiques
    private val initialContacts = mutableListOf(
        Contact(
            id = 1L,
            nom = "Dupont",
            prenom = "Claire",
            telephone = "06 12 34 56 78",
            email = "claire.dupont@example.com",
            photoDrawableRes = R.drawable.avatar_claire,
            latitude = 48.8584,
            longitude = 2.2945 // Paris (Tour Eiffel)
        ),
        Contact(
            id = 2L,
            nom = "Martin",
            prenom = "Thomas",
            telephone = "07 23 45 67 89",
            email = "thomas.martin@example.com",
            photoDrawableRes = R.drawable.avatar_thomas,
            latitude = 45.7578,
            longitude = 4.8320 // Lyon (Place Bellecour)
        ),
        Contact(
            id = 3L,
            nom = "Bernard",
            prenom = "Sophie",
            telephone = "06 98 76 54 32",
            email = "sophie.bernard@example.com",
            photoDrawableRes = R.drawable.avatar_sophie,
            latitude = 43.2951,
            longitude = 5.3744 // Marseille (Vieux-Port)
        ),
        Contact(
            id = 4L,
            nom = "Petit",
            prenom = "Lucas",
            telephone = "07 11 22 33 44",
            email = "lucas.petit@example.com",
            photoDrawableRes = R.drawable.avatar_lucas,
            latitude = null,
            longitude = null // Aucune position définie (cas test requis par le TP)
        ),
        Contact(
            id = 5L,
            nom = "Moreau",
            prenom = "Julien",
            telephone = "06 55 44 33 22",
            email = "julien.moreau@example.com",
            photoDrawableRes = R.drawable.avatar_julien,
            latitude = 44.8416,
            longitude = -0.5696 // Bordeaux (Place de la Bourse)
        ),
        Contact(
            id = 6L,
            nom = "Leroy",
            prenom = "Camille",
            telephone = "06 88 99 00 11",
            email = "camille.leroy@example.com",
            photoDrawableRes = R.drawable.avatar_claire,
            latitude = 43.6045,
            longitude = 1.4442 // Toulouse (Place du Capitole)
        ),
        Contact(
            id = 7L,
            nom = "Dubois",
            prenom = "Antoine",
            telephone = "07 44 55 66 77",
            email = "antoine.dubois@example.com",
            photoDrawableRes = R.drawable.avatar_thomas,
            latitude = 50.6370,
            longitude = 3.0633 // Lille (Grand Place)
        )
    )

    private val _contacts = MutableLiveData<List<Contact>>(initialContacts.toList())
    private val _searchQuery = MutableLiveData<String>("")

    val searchQuery: LiveData<String> = _searchQuery

    /**
     * LiveData filtré combinant la liste complète des contacts et la requête de recherche.
     */
    val filteredContacts = MediatorLiveData<List<Contact>>().apply {
        addSource(_contacts) { contacts ->
            value = filterList(contacts, _searchQuery.value)
        }
        addSource(_searchQuery) { query ->
            value = filterList(_contacts.value ?: emptyList(), query)
        }
    }

    /**
     * Applique le filtre insensible à la casse sur le nom, prénom ou téléphone.
     */
    private fun filterList(contacts: List<Contact>, query: String?): List<Contact> {
        val cleanQuery = query?.trim()?.lowercase().orEmpty()
        if (cleanQuery.isEmpty()) {
            return contacts
        }
        return contacts.filter { contact ->
            contact.nom.lowercase().contains(cleanQuery) ||
                    contact.prenom.lowercase().contains(cleanQuery) ||
                    contact.telephone.replace(" ", "").contains(cleanQuery.replace(" ", "")) ||
                    contact.nomComplet.lowercase().contains(cleanQuery)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Recherche un contact par son identifiant unique ID.
     */
    fun getContactById(id: Long): Contact? {
        return _contacts.value?.find { it.id == id }?.copy()
    }

    /**
     * Enregistre un contact : mise à jour s'il existe déjà, ou ajout s'il est nouveau.
     */
    fun saveContact(contact: Contact) {
        val currentList = _contacts.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == contact.id }

        if (index >= 0) {
            currentList[index] = contact
        } else {
            currentList.add(contact)
        }
        _contacts.value = currentList
    }

    /**
     * Supprime un contact par son identifiant ID.
     */
    fun deleteContact(id: Long) {
        val currentList = _contacts.value?.toMutableList() ?: return
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            _contacts.value = currentList
        }
    }
}
