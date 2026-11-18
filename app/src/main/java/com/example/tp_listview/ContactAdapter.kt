package com.example.tp_listview

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * 4. Adaptateur RecyclerView personnalisé pour la liste des contacts.
 * Rôle de RecyclerView.Adapter :
 * - Instancier les vues d'éléments (onCreateViewHolder).
 * - Lier les données du modèle Contact aux composants graphiques (onBindViewHolder).
 * - Optimiser le recyclage des vues en mémoire.
 */
class ContactAdapter(
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts: List<Contact> = emptyList()

    /**
     * Rôle du ViewHolder :
     * Mettre en cache les références des composants graphiques (findViewById)
     * pour éviter des appels répétitifs et coûteux lors du défilement.
     */
    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView
        val contentLayout: View? = itemView.findViewById(R.id.layoutContactContent)
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivItemPhoto)
        val tvNomPrenom: TextView = itemView.findViewById(R.id.tvItemNomPrenom)
        val tvTelephone: TextView = itemView.findViewById(R.id.tvItemTelephone)
        val tvGpsBadge: TextView = itemView.findViewById(R.id.tvItemGpsBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        val holder = ContactViewHolder(view)

        // Gestion du clic robuste sur l'élément entier et son conteneur
        val clickListener = View.OnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION && position in contacts.indices) {
                onContactClick(contacts[position])
            }
        }

        holder.root.setOnClickListener(clickListener)
        holder.contentLayout?.setOnClickListener(clickListener)

        return holder
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        holder.tvNomPrenom.text = contact.nomComplet
        holder.tvTelephone.text = contact.telephone

        // Affichage du badge GPS si les coordonnées sont présentes et valides
        if (contact.hasValidCoordinates) {
            holder.tvGpsBadge.visibility = View.VISIBLE
        } else {
            holder.tvGpsBadge.visibility = View.GONE
        }

        // Gestion de la photo de profil (URI, ressource Drawable ou icône par défaut)
        when {
            !contact.photoUri.isNullOrEmpty() -> {
                try {
                    holder.ivPhoto.setImageURI(Uri.parse(contact.photoUri))
                } catch (e: Exception) {
                    holder.ivPhoto.setImageResource(contact.photoDrawableRes ?: R.drawable.ic_default_avatar)
                }
            }
            contact.photoDrawableRes != null -> {
                holder.ivPhoto.setImageResource(contact.photoDrawableRes!!)
            }
            else -> {
                holder.ivPhoto.setImageResource(R.drawable.ic_default_avatar)
            }
        }
    }

    override fun getItemCount(): Int = contacts.size

    /**
     * Met à jour la liste avec calcul de différence (DiffUtil) pour une mise à jour fluide.
     */
    fun submitList(newList: List<Contact>) {
        val diffCallback = ContactDiffCallback(this.contacts, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.contacts = newList.toList()
        diffResult.dispatchUpdatesTo(this)
    }

    private class ContactDiffCallback(
        private val oldList: List<Contact>,
        private val newList: List<Contact>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
