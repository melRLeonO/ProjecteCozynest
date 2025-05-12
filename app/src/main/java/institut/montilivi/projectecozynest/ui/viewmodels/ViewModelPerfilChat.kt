package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewModelPerfilChat : ViewModel() {
    private val _usuariActual = MutableStateFlow<UsuariBase?>(null)
    val usuariActual: StateFlow<UsuariBase?> = _usuariActual.asStateFlow()

    private val _estaBloquejat = MutableStateFlow(false)
    val estaBloquejat: StateFlow<Boolean> = _estaBloquejat.asStateFlow()

    private val _carregantBloqueig = MutableStateFlow(true)
    val carregantBloqueig: StateFlow<Boolean> = _carregantBloqueig.asStateFlow()

    private val repositori = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    init {
        obtenirUsuariActual()
    }
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val missatgesDAO = DAOFactory.obtenMissatgesDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val firestore = FirebaseFirestore.getInstance()

    // Eliminar chat entre dos usuarios
    private suspend fun eliminaChatEntre(usuari1: String, usuari2: String) {
        val chatsRef = firestore.collection("Chats")
        val chats = chatsRef
            .whereArrayContains("participants", usuari1)
            .get()
            .await()
            .documents

        for (chat in chats) {
            val participants = chat.get("participants") as? List<*>
            if (participants != null && participants.contains(usuari2)) {
                chat.reference.delete().await()
            }
        }
    }

    // Eliminar mensajes entre dos usuarios
    private suspend fun eliminaMissatgesEntre(usuari1: String, usuari2: String) {
        val missatgesRef = firestore.collection("Missatges")
        val missatges = missatgesRef
            .whereIn("remitent", listOf(usuari1, usuari2))
            .get()
            .await()
            .documents

        for (missatge in missatges) {
            val remitent = missatge.getString("remitent")
            val destinatari = missatge.getString("destinatari")
            if ((remitent == usuari1 && destinatari == usuari2) ||
                (remitent == usuari2 && destinatari == usuari1)) {
                missatge.reference.delete().await()
            }
        }
    }

    // Eliminar match entre dos usuarios
    private suspend fun eliminaMatchEntre(usuari1: String, usuari2: String) {
        val matchRef = firestore.collection("match")
        val matches = matchRef
            .whereIn("usuari1", listOf(usuari1, usuari2))
            .get()
            .await()
            .documents

        for (match in matches) {
            val u1 = match.getString("usuari1")
            val u2 = match.getString("usuari2")
            if ((u1 == usuari1 && u2 == usuari2) ||
                (u1 == usuari2 && u2 == usuari1)) {
                match.reference.delete().await()
            }
        }
    }
    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            FirebaseFirestore.getInstance().collection("Usuaris")
                .whereEqualTo("correu", currentUserEmail)
                .get()
                .addOnSuccessListener { documents ->
                    _usuariActual.value = documents.firstOrNull()?.toObject(UsuariBase::class.java)
                }
        }
    }

    fun comprovaBloqueig(idUsuariAComprovar: String) {
        viewModelScope.launch {
            _carregantBloqueig.value = true
            val idUsuariActual = _usuariActual.value?.id
            if (idUsuariActual != null) {
                when (val resposta = repositori.estaBloquejat(idUsuariActual, idUsuariAComprovar)) {
                    is institut.montilivi.projectecozynest.model.Resposta.Exit -> {
                        _estaBloquejat.value = resposta.dades
                    }
                    is institut.montilivi.projectecozynest.model.Resposta.Fracas -> {
                        _estaBloquejat.value = false
                    }
                }
            }
            _carregantBloqueig.value = false
        }
    }

   fun bloquejaUsuari(idUsuariBloquejat: String) {
    viewModelScope.launch {
        val idUsuariActual = _usuariActual.value?.id
        if (idUsuariActual != null) {
            when (val resposta = repositori.bloquejaUsuari(idUsuariActual, idUsuariBloquejat)) {
                is institut.montilivi.projectecozynest.model.Resposta.Exit -> {
                    if (resposta.dades) {
                        _estaBloquejat.value = true
                        // Elimina chat, mensajes y match
                        eliminaChatEntre(idUsuariActual, idUsuariBloquejat)
                        eliminaMissatgesEntre(idUsuariActual, idUsuariBloquejat)
                        eliminaMatchEntre(idUsuariActual, idUsuariBloquejat)
                    }
                }
                is institut.montilivi.projectecozynest.model.Resposta.Fracas -> {
                    // Manejar error si quieres
                }
            }
        }
    }
}
} 