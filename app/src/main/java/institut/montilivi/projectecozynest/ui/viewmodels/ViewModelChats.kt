package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import institut.montilivi.projectecozynest.dades.ChatsDAO
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewModelChats : ViewModel() {
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    var rolActual: String? = null

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val correuActual = FirebaseAuth.getInstance().currentUser?.email
    var usuariIdActual: String? = null

    init {
        obtenirUsuariActualId()
    }

    private fun obtenirUsuariActualId() {
        val firestore = FirebaseFirestore.getInstance()
        correuActual?.let { correu ->
            firestore.collection("Usuaris")
                .whereEqualTo("correu", correu)
                .get()
                .addOnSuccessListener { documents ->
                    val usuari = documents.firstOrNull()?.toObject(UsuariBase::class.java)
                    usuariIdActual = usuari?.id
                    rolActual = usuari?.rol
                    usuari?.id?.let { carregarChats(it) }
                }
        }
    }
    suspend fun obtenirNomUsuariPerId(id: String): String {
        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.collection("Usuaris").document(id).get().await()
        val usuari = doc.toObject(UsuariBase::class.java)
        return "${usuari?.nom ?: ""} ${usuari?.cognoms ?: ""}".trim()
    }
    suspend fun obtenirFotoUsuariPerId(id: String): String {
        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.collection("Usuaris").document(id).get().await()
        val usuari = doc.toObject(UsuariBase::class.java)
        return usuari?.fotoPerfil ?: ""
    }

    private fun carregarChats(usuariId: String) {
        viewModelScope.launch {
            chatsDAO.obtenChats(usuariId).collect { resposta ->
                when (resposta) {
                    is Resposta.Exit -> _chats.value = resposta.dades
                    is Resposta.Fracas -> {
                        // Podrías manejar el error mostrando un mensaje, etc.
                    }
                }
            }
        }
    }
}

