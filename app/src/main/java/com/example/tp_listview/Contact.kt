package com.example.tp_listview

import java.io.Serializable

/**
 * Modèle Contact étendu pour la V3 avec support des coordonnées GPS (latitude et longitude).
 */
data class Contact(
    var id: Long = System.currentTimeMillis(),
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photoUri: String? = null,
    var photoDrawableRes: Int? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) : Serializable {

    val nomComplet: String
        get() = "$nom $prenom".trim()

    /**
     * Vérifie si le contact possède des coordonnées valides :
     * Latitude comprise entre -90.0 et 90.0
     * Longitude comprise entre -180.0 et 180.0
     */
    val hasValidCoordinates: Boolean
        get() = latitude != null && longitude != null &&
                latitude!! >= -90.0 && latitude!! <= 90.0 &&
                longitude!! >= -180.0 && longitude!! <= 180.0

    fun formattedCoordinates(): String {
        return if (hasValidCoordinates) {
            "Lat: %.4f, Lon: %.4f".format(latitude, longitude)
        } else {
            "Aucune position définie"
        }
    }
}
