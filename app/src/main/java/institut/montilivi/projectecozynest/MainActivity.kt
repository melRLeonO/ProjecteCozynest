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
        enableEdgeToEdge()

        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                "AIzaSyCWnl96pIyDpbzlLO75bad9KrhomOAfPvg",
                Locale("ca")
            )
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("Usuaris")
                .whereEqualTo("correu", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty && currentUser.metadata?.creationTimestamp?.let {
                        System.currentTimeMillis() - it < 5 * 60 * 1000 
                    } == true) {
                        currentUser.delete()
                    }
                }
        }

        setContent {
            ProjecteCozynestTheme {
                GrafDeNavegacio()
            }
        }
    }
}
