package com.example.tp_listview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

/**
 * Fragment affichant la liste des contacts et le champ de recherche dynamique.
 */
class ListeContactsFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    private lateinit var toolbarListe: MaterialToolbar
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageView
    private lateinit var rvContacts: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liste_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Liaison des composants de l'interface
        toolbarListe = view.findViewById(R.id.toolbarListe)
        etSearch = view.findViewById(R.id.etSearch)
        btnClearSearch = view.findViewById(R.id.btnClearSearch)
        rvContacts = view.findViewById(R.id.rvContacts)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupToolbar() {
        // Bouton "+" pour ajouter un nouveau contact
        toolbarListe.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    ouvrirDetailContact(contactId = null)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter { contact ->
            // Clic sur un contact : passage de son ID unique via Bundle pour éviter toute erreur d'indice
            ouvrirDetailContact(contactId = contact.id)
        }
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter
    }

    /**
     * 5. Filtrage des contacts :
     * - Filtrage instantané au fur et à mesure de la saisie (TextWatcher sans bouton de validation).
     * - Insensible à la casse, portant sur nom, prénom et téléphone.
     * - Affichage/masquage de l'icône pour effacer la recherche.
     */
    private fun setupSearch() {
        // Restauration de la valeur de recherche existante
        etSearch.setText(viewModel.searchQuery.value.orEmpty())

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                viewModel.setSearchQuery(query)
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Bouton pour vider le champ de recherche
        btnClearSearch.setOnClickListener {
            etSearch.text?.clear()
            viewModel.setSearchQuery("")
        }
    }

    /**
     * Observation de la liste filtrée depuis le ViewModel partagé.
     */
    private fun observeViewModel() {
        viewModel.filteredContacts.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            if (contacts.isEmpty()) {
                layoutEmptyState.visibility = View.VISIBLE
                rvContacts.visibility = View.GONE
            } else {
                layoutEmptyState.visibility = View.GONE
                rvContacts.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Navigation vers DetailContactFragment avec transmission de l'identifiant via Bundle.
     */
    private fun ouvrirDetailContact(contactId: Long?) {
        val detailFragment = DetailContactFragment.newInstance(contactId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }
}
