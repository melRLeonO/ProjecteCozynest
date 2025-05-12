package institut.montilivi.projectecozynest.autentificacio

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class ManegadorAutentificacio(private val context: Context) {
    private val autentificacio: FirebaseAuth by lazy {
        Firebase.auth
    }
    //region Inici de sessió amb Google
    private val idClientWeb =
        "913039890809-o2d4h2okqai1dpepokrc9rmdlj3ts878.apps.googleusercontent.com"
    private val tag = "MANEGADOR_AUTENTIFICACIO"
    private val manegadorDeCredencials = CredentialManager.create(context)
    //endregion

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
    private suspend fun creaPeticioDeCredencials(): GetCredentialResponse {
        val peticio = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(idClientWeb)
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return manegadorDeCredencials.getCredential(
            request = peticio,
            context = context
        )
    }
    private suspend fun manegaIniciDeSessio(resultat: GetCredentialResponse): Boolean {
        val credencial = resultat.credential
        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val token = GoogleIdTokenCredential.createFrom(credencial.data)
                Log.i(tag, "Token: ${token.displayName}")
                Log.i(tag, "Token: ${token.familyName}")
                Log.i(tag, "Token: ${token.phoneNumber}")
                Log.i(tag, "Token: ${token.profilePictureUri}")

                val credencialDeGoogle = GoogleAuthProvider.getCredential(token.idToken, null)
                val resultatDeAutenticacio =
                    autentificacio.signInWithCredential(credencialDeGoogle).await()

                return resultatDeAutenticacio.user != null
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(tag, "Error al parsejar el token: ${e.message}")
                return false
            }

        }
        return false;
    }
    suspend fun iniciDeSessioAmbGoogle(): Boolean {
        if (hiHaUsuariIniciat()) {
            Log.d(tag, "Ja hi ha un usuari iniciat")
            return true;
        } else {
            try {
                val resultat = creaPeticioDeCredencials()
                manegaIniciDeSessio(resultat)
                return hiHaUsuariIniciat()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(tag, "Error al iniciar sessió amb Google: ${e.message}")
                return false
            }
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
                Log.d(tag, "Iniciant reautenticació...")
                usuari.reauthenticate(credencial).await()
                Log.d(tag, "Reautenticació completada amb èxit")
                Resposta.Exit(Unit)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(tag, "Contrasenya incorrecta: ${e.message}")
                Resposta.Fracas("CONTRASENYA_INCORRECTA")
            } catch (e: Exception) {
                Log.e(tag, "Error reautenticant usuari: ${e.message}")
                Resposta.Fracas(e.message ?: "Error durant la reautenticació")
            }
        } else {
            Resposta.Fracas("Cap usuari iniciat")
        }
    }

}