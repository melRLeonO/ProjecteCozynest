package institut.montilivi.projectecozynest.autentificacio

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.tasks.await

class ManegadorAutentificacio(private val context: Context) {
    private val autentificacio: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val idClientWeb =
        "913039890809-o2d4h2okqai1dpepokrc9rmdlj3ts878.apps.googleusercontent.com"
    private val tag = "MANEGADOR_AUTENTIFICACIO"
    private val manegadorDeCredencials = CredentialManager.create(context)

    fun obtenUsuariActual(): FirebaseUser? {
        return autentificacio.currentUser
    }
    fun hiHaUsuariIniciat() = obtenUsuariActual() != null
    suspend fun registraUsuariAmbCorreuIContrasenya(email: String, password: String): Resposta<FirebaseUser> {
        return try {
            val resultat = autentificacio.createUserWithEmailAndPassword(email, password).await()
            Resposta.Exit(resultat.user ?: throw Exception("No s'ha pogut crear l'usuari"))
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error registrant l'usuari")
        }
    }
    suspend fun tancaSessio() {
        manegadorDeCredencials.clearCredentialState(ClearCredentialStateRequest())
        autentificacio.signOut()
    }

    suspend fun iniciarSessioAmbCorreuIContrasenya(email: String, password: String): Resposta<FirebaseUser> {
        return try {
            val resultat = autentificacio.signInWithEmailAndPassword(email, password).await()
            Resposta.Exit(resultat.user ?: throw Exception("Usuari no trobat"))
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error iniciant sessió")
        }
    }
    suspend fun enviaCorreuDeRestablimentContrasenya(email: String): Resposta<Unit> {
        return try {
            autentificacio.sendPasswordResetEmail(email).await()
            Resposta.Exit(Unit)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error enviant correu de restabliment")
        }
    }
    suspend fun actualitzaContrasenya(novaContrasenya: String): Resposta<Unit> {
        val usuari = autentificacio.currentUser
        return if (usuari != null) {
            try {
                usuari.updatePassword(novaContrasenya).await()
                Resposta.Exit(Unit)
            } catch (e: Exception) {
                Resposta.Fracas(e.message ?: "Error actualitzant la contrasenya")
            }
        } else {
            Resposta.Fracas("Cap usuari iniciat")
        }
    }
    suspend fun reautenticaUsuari(correu: String, contrasenya: String): Resposta<Unit> {
        val usuari = autentificacio.currentUser
        return if (usuari != null) {
            if (usuari.email != correu) {
                return Resposta.Fracas("El correu no coincideix amb el de l'usuari actual")
            }
            val credencial = EmailAuthProvider.getCredential(correu, contrasenya)
            try {
                usuari.reauthenticate(credencial).await()
                Resposta.Exit(Unit)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Resposta.Fracas("CONTRASENYA_INCORRECTA")
            } catch (e: Exception) {
                Resposta.Fracas(e.message ?: "Error durant la reautenticació")
            }
        } else {
            Resposta.Fracas("Cap usuari iniciat")
        }
    }

}