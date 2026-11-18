package com.example.tp_listview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File

/**
 * Fragment permettant la consultation, modification, actions et géolocalisation d'un contact.
 */
class DetailContactFragment : Fragment() {

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(contactId: Long?): DetailContactFragment {
            return DetailContactFragment().apply {
                arguments = Bundle().apply {
                    if (contactId != null) {
                        putLong(ARG_CONTACT_ID, contactId)
                    }
                }
            }
        }
    }

    private val viewModel: ContactViewModel by activityViewModels()

    private var contactId: Long? = null
    private var contact: Contact? = null

    private lateinit var toolbarDetail: MaterialToolbar
    private lateinit var ivDetailPhoto: ImageView
    private lateinit var ivCameraBadge: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var btnAppeler: ImageButton
    private lateinit var etEmail: EditText
    private lateinit var btnEmail: ImageButton
    private lateinit var btnChoisirPhoto: MaterialButton
    private lateinit var btnPrendrePhoto: MaterialButton
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var btnVoirSurCarte: MaterialButton
    private lateinit var btnEnregistrer: MaterialButton
    private lateinit var btnAnnuler: MaterialButton

    private var currentPhotoUri: String? = null
    private var currentDrawableRes: Int? = null

    // Launcher pour la sélection d'une image depuis la galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val savedUri = copyUriToInternal(uri)
                currentPhotoUri = savedUri
                currentDrawableRes = null
                ivDetailPhoto.setImageURI(Uri.parse(savedUri))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Impossible de charger l'image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), R.string.msg_photo_annulee, Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher pour la capture directe avec l'appareil photo
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val savedUri = saveBitmapToInternal(bitmap)
                currentPhotoUri = savedUri
                currentDrawableRes = null
                ivDetailPhoto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), R.string.msg_camera_annulee, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey(ARG_CONTACT_ID) == true) {
            contactId = arguments?.getLong(ARG_CONTACT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Liaison des composants de l'interface
        toolbarDetail = view.findViewById(R.id.toolbarDetail)
        ivDetailPhoto = view.findViewById(R.id.ivDetailPhoto)
        ivCameraBadge = view.findViewById(R.id.ivCameraBadge)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        btnAppeler = view.findViewById(R.id.btnAppeler)
        etEmail = view.findViewById(R.id.etEmail)
        btnEmail = view.findViewById(R.id.btnEmail)
        btnChoisirPhoto = view.findViewById(R.id.btnChoisirPhoto)
        btnPrendrePhoto = view.findViewById(R.id.btnPrendrePhoto)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)
        btnVoirSurCarte = view.findViewById(R.id.btnVoirSurCarte)
        btnEnregistrer = view.findViewById(R.id.btnEnregistrer)
        btnAnnuler = view.findViewById(R.id.btnAnnuler)

        // Récupération du contact depuis le ViewModel
        contact = contactId?.let { viewModel.getContactById(it) }

        setupToolbar()
        populateFields()
        setupListeners()
    }

    private fun setupToolbar() {
        // Retour vers la liste sans enregistrer
        toolbarDetail.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val deleteItem = toolbarDetail.menu.findItem(R.id.action_delete)
        if (contact == null) {
            deleteItem?.isVisible = false
            toolbarDetail.title = getString(R.string.title_nouveau_contact)
        } else {
            toolbarDetail.title = getString(R.string.title_detail)
        }

        toolbarDetail.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    confirmerSuppression()
                    true
                }
                else -> false
            }
        }
    }

    private fun populateFields() {
        contact?.let { c ->
            etNom.setText(c.nom)
            etPrenom.setText(c.prenom)
            etTelephone.setText(c.telephone)
            etEmail.setText(c.email)

            if (c.latitude != null) {
                etLatitude.setText(c.latitude.toString())
            }
            if (c.longitude != null) {
                etLongitude.setText(c.longitude.toString())
            }

            currentPhotoUri = c.photoUri
            currentDrawableRes = c.photoDrawableRes

            when {
                !c.photoUri.isNullOrEmpty() -> {
                    try {
                        ivDetailPhoto.setImageURI(Uri.parse(c.photoUri))
                    } catch (e: Exception) {
                        ivDetailPhoto.setImageResource(c.photoDrawableRes ?: R.drawable.ic_default_avatar)
                    }
                }
                c.photoDrawableRes != null -> {
                    ivDetailPhoto.setImageResource(c.photoDrawableRes!!)
                }
                else -> {
                    ivDetailPhoto.setImageResource(R.drawable.ic_default_avatar)
                }
            }
        } ?: run {
            // Nouveau contact : icône par défaut
            ivDetailPhoto.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun setupListeners() {
        // 1. Bouton Appeler (Intent implicite vers le composeur)
        btnAppeler.setOnClickListener {
            val tel = etTelephone.text.toString().trim()
            if (tel.isEmpty()) {
                etTelephone.error = getString(R.string.err_telephone_vide)
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$tel")
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.msg_aucune_app_tel, Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Bouton Envoyer un e-mail (Intent implicite)
        btnEmail.setOnClickListener {
            val mail = etEmail.text.toString().trim()
            if (mail.isEmpty()) {
                etEmail.error = "Veuillez saisir une adresse e-mail"
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$mail")
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.msg_aucune_app_mail, Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Galerie photo
        btnChoisirPhoto.setOnClickListener {
            try {
                pickImageLauncher.launch("image/*")
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Impossible d'ouvrir la galerie", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Appareil photo
        val launchCamera = {
            try {
                takePhotoLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Appareil photo indisponible", Toast.LENGTH_SHORT).show()
            }
        }
        btnPrendrePhoto.setOnClickListener { launchCamera() }
        ivCameraBadge.setOnClickListener { launchCamera() }

        // 5. Bouton Voir sur la carte Google Maps
        btnVoirSurCarte.setOnClickListener {
            val (lat, lon, isValid) = getValidatedCoordinates()
            val nomAffiche = "${etNom.text.toString().trim()} ${etPrenom.text.toString().trim()}".trim()

            if (isValid && lat != null && lon != null) {
                val carteFragment = CarteContactFragment.newInstance(
                    contactId = contact?.id,
                    contactNom = nomAffiche.ifEmpty { "Contact" },
                    latitude = lat,
                    longitude = lon
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, carteFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), R.string.msg_coordonnees_absentes, Toast.LENGTH_LONG).show()
            }
        }

        // 6. Bouton Enregistrer
        btnEnregistrer.setOnClickListener { enregistrerContact() }

        // 7. Bouton Annuler
        btnAnnuler.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    /**
     * Valide et extrait les coordonnées GPS saisies dans les champs.
     * Retourne (latitude, longitude, estValide).
     */
    private fun getValidatedCoordinates(): Triple<Double?, Double?, Boolean> {
        val latStr = etLatitude.text.toString().trim()
        val lonStr = etLongitude.text.toString().trim()

        if (latStr.isEmpty() && lonStr.isEmpty()) {
            return Triple(null, null, false)
        }

        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat == null || lon == null) {
            return Triple(null, null, false)
        }

        val isLatValid = lat in -90.0..90.0
        val isLonValid = lon in -180.0..180.0

        return if (isLatValid && isLonValid) {
            Triple(lat, lon, true)
        } else {
            Triple(lat, lon, false)
        }
    }

    private fun enregistrerContact() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val latStr = etLatitude.text.toString().trim()
        val lonStr = etLongitude.text.toString().trim()

        var hasError = false

        if (nom.isEmpty()) {
            etNom.error = getString(R.string.err_nom_vide)
            hasError = true
        }
        if (prenom.isEmpty()) {
            etPrenom.error = getString(R.string.err_prenom_vide)
            hasError = true
        }
        if (telephone.isEmpty()) {
            etTelephone.error = getString(R.string.err_telephone_vide)
            hasError = true
        }

        // Validation des coordonnées GPS
        var finalLatitude: Double? = null
        var finalLongitude: Double? = null

        if (latStr.isNotEmpty() || lonStr.isNotEmpty()) {
            val parsedLat = latStr.toDoubleOrNull()
            val parsedLon = lonStr.toDoubleOrNull()

            if (parsedLat == null || parsedLat !in -90.0..90.0) {
                etLatitude.error = getString(R.string.err_latitude_invalide)
                hasError = true
            } else {
                finalLatitude = parsedLat
            }

            if (parsedLon == null || parsedLon !in -180.0..180.0) {
                etLongitude.error = getString(R.string.err_longitude_invalide)
                hasError = true
            } else {
                finalLongitude = parsedLon
            }
        }

        if (hasError) return

        val updatedContact = Contact(
            id = contact?.id ?: System.currentTimeMillis(),
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = currentPhotoUri,
            photoDrawableRes = currentDrawableRes,
            latitude = finalLatitude,
            longitude = finalLongitude
        )

        // Sauvegarde dans le ViewModel partagé (met à jour la liste immédiatement)
        viewModel.saveContact(updatedContact)

        val message = if (contact == null) {
            getString(R.string.msg_contact_ajoute, updatedContact.nomComplet)
        } else {
            getString(R.string.msg_contact_modifie, updatedContact.nomComplet)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun confirmerSuppression() {
        val targetContact = contact ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dlg_supprimer_titre)
            .setMessage(getString(R.string.dlg_supprimer_msg, targetContact.nomComplet))
            .setPositiveButton(R.string.dlg_oui) { _, _ ->
                viewModel.deleteContact(targetContact.id)
                Toast.makeText(requireContext(), R.string.msg_contact_supprime, Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .setNegativeButton(R.string.dlg_non, null)
            .show()
    }

    private fun copyUriToInternal(sourceUri: Uri): String {
        val file = File(requireContext().filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file).toString()
    }

    private fun saveBitmapToInternal(bitmap: Bitmap): String {
        val file = File(requireContext().filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        return Uri.fromFile(file).toString()
    }
}
