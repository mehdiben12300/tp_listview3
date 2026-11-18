package com.example.tp_listview

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitaires vérifiant la logique métier du modèle Contact :
 * - Nom complet
 * - Validation des coordonnées GPS (limites latitude -90..90 et longitude -180..180)
 * - Gestion des coordonnées nulles ou hors limites
 */
class ContactTest {

    @Test
    fun testNomComplet() {
        val contact = Contact(
            nom = "Dupont",
            prenom = "Claire",
            telephone = "06 12 34 56 78",
            email = "claire.dupont@example.com"
        )
        assertEquals("Dupont Claire", contact.nomComplet)
    }

    @Test
    fun testCoordonneesValides() {
        val contactParis = Contact(
            nom = "Dupont",
            prenom = "Claire",
            telephone = "06 12 34 56 78",
            email = "claire.dupont@example.com",
            latitude = 48.8566,
            longitude = 2.3522
        )
        assertTrue(contactParis.hasValidCoordinates)
    }

    @Test
    fun testCoordonneesNulles() {
        val contactSansCoords = Contact(
            nom = "Petit",
            prenom = "Lucas",
            telephone = "07 11 22 33 44",
            email = "lucas.petit@example.com",
            latitude = null,
            longitude = null
        )
        assertFalse(contactSansCoords.hasValidCoordinates)
    }

    @Test
    fun testLatitudeHorsLimites() {
        val contactInvalide = Contact(
            nom = "Test",
            prenom = "Invalide",
            telephone = "01 23 45 67 89",
            email = "test@example.com",
            latitude = 95.0, // Invalide : > 90
            longitude = 2.0
        )
        assertFalse(contactInvalide.hasValidCoordinates)
    }

    @Test
    fun testLongitudeHorsLimites() {
        val contactInvalide = Contact(
            nom = "Test",
            prenom = "Invalide",
            telephone = "01 23 45 67 89",
            email = "test@example.com",
            latitude = 45.0,
            longitude = 195.0 // Invalide : > 180
        )
        assertFalse(contactInvalide.hasValidCoordinates)
    }

    @Test
    fun testCoordonneesLimitesExactes() {
        val contactBords = Contact(
            nom = "Pole",
            prenom = "Nord",
            telephone = "00 00 00 00 00",
            email = "nord@example.com",
            latitude = 90.0,
            longitude = 180.0
        )
        assertTrue(contactBords.hasValidCoordinates)
    }
}
