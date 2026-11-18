package com.example.tp_listview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 3. Activité principale unique (Single-Activity Architecture).
 *
 * Rôles :
 * - Héberger les fragments dynamiques dans son FragmentContainerView.
 * - Partager l'instance unique du ContactViewModel entre tous les fragments.
 * - Gérer la rotation et la recréation sans empiler les fragments en double (vérification de savedInstanceState == null).
 */
class MainActivity : AppCompatActivity() {

    // Instanciation du ViewModel partagé au niveau du cycle de vie de l'activité
    val contactViewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 7. Gestion de la rotation sans duplication :
        // Le FragmentManager restaure automatiquement les fragments existants après une rotation.
        // On n'injecte le fragment initial que lors de la première création de l'activité.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }
    }
}
