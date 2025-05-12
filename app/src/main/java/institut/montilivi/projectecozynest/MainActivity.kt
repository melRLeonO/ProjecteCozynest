package institut.montilivi.projectecozynest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.threetenabp.AndroidThreeTen
import institut.montilivi.projectecozynest.navegacio.GrafDeNavegacio
import institut.montilivi.projectecozynest.ui.theme.ProjecteCozynestTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        // Activa edge-to-edge en dispositivos modernos
        enableEdgeToEdge()

        // Inicializa Google Places solo una vez
        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                "AIzaSyCWnl96pIyDpbzlLO75bad9KrhomOAfPvg",
                Locale("ca") // Catalán
            )
        }

        // Solo verificamos si el usuario ha completado el registro si es un usuario nuevo
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("Usuaris")
                .whereEqualTo("correu", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    // Si no hay documentos y el usuario es nuevo (creado hace menos de 5 minutos)
                    if (documents.isEmpty && currentUser.metadata?.creationTimestamp?.let {
                        System.currentTimeMillis() - it < 5 * 60 * 1000 
                    } == true) {
                        // Solo eliminamos si es un usuario nuevo que no ha completado el registro
                        currentUser.delete()
                    }
                }
        }

        setContent {
            ProjecteCozynestTheme {
                // UI principal de tu app
                GrafDeNavegacio()
            }
        }
    }
}
