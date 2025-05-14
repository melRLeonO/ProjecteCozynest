package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.ChatSeleccionat
import institut.montilivi.projectecozynest.model.Missatge
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.model.Chat

class ViewModelConversa : ViewModel() {
    private val _altreUsuari = MutableStateFlow<UsuariBase?>(null)
    val altreUsuari: StateFlow<UsuariBase?> = _altreUsuari.asStateFlow()
    private val _usuariActual = MutableStateFlow<UsuariBase?>(null)
    val usuariActual: StateFlow<UsuariBase?> = _usuariActual.asStateFlow()

    private val missatgesDAO = DAOFactory.obtenMissatgesDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    private val _missatges = MutableStateFlow<List<Missatge>>(emptyList())
    val missatges: StateFlow<List<Missatge>> = _missatges.asStateFlow()

    init {
        obtenirUsuariActual()
    }

    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            viewModelScope.launch {
                try {
                    val resultat = usuarisDAO.obtenUsuariPerCorreu(currentUserEmail)
                    when (resultat) {
                        is Resposta.Exit -> {
                            _usuariActual.value = resultat.dades
                        }
                        is Resposta.Fracas -> {
                            _usuariActual.value = null
                        }
                    }
                } catch (e: Exception) {
                    _usuariActual.value = null
                }
            }
        }
    }

    fun carregarMissatges(idChat: String) {
        viewModelScope.launch {
            try {
                missatgesDAO.obtenMissatges(idChat).collect { resposta ->
                    when (resposta) {
                        is Resposta.Exit -> {
                            _missatges.value = resposta.dades

                            val usuariActualId = _usuariActual.value?.id
                            if (usuariActualId != null) {
                                carregarChat(idChat) { chat ->
                                    chat?.let {
                                        val altreId = when (usuariActualId) {
                                            it.idEstudiant -> it.idPersonaGran
                                            it.idPersonaGran -> it.idEstudiant
                                            else -> null
                                        }

                                        if (!altreId.isNullOrEmpty()) {
                                            carregarAltreUsuari(altreId)
                                        }
                                    }
                                }
                            }
                        }
                        is Resposta.Fracas -> {
                            // Manejar el error si es necesario
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar la excepción si es necesario
            }
        }
    }

    fun enviarMissatge(text: String) {
        val idChat = ChatSeleccionat.idChatSeleccionat ?: return
        val usuari = _usuariActual.value ?: return

        val missatge = Missatge(
            idChat = idChat,
            remitent = usuari.id,
            text = text,
            timestamp = Timestamp.now()
        )

        viewModelScope.launch {
            try {
                val resultat = missatgesDAO.afegirMissatge(missatge)
                if (resultat is Resposta.Exit) {
                    // Obtener el chat actual primero
                    val chatResultat = chatsDAO.obtenChat(idChat)
                    if (chatResultat is Resposta.Exit) {
                        val chatActual = chatResultat.dades
                        // Actualizar solo los campos necesarios
                        chatActual.lastMessage = missatge.text
                        chatActual.lastMessageSenderId = usuari.id
                        chatActual.timestamp = missatge.timestamp
                        // Mantener los demás campos intactos
                        chatsDAO.modificaChat(chatActual)
                    }
                }
            } catch (e: Exception) {
                // Manejar la excepción si es necesario
            }
        }
    }

    fun carregarAltreUsuari(id: String) {
        viewModelScope.launch {
            try {
                val resultat = usuarisDAO.obtenUsuari(id)
                when (resultat) {
                    is Resposta.Exit -> {
                        _altreUsuari.value = resultat.dades
                    }
                    is Resposta.Fracas -> {

                    }
                }
            } catch (e: Exception) {
                // Manejar la excepción si es necesario
            }
        }
    }

    fun carregarChat(idChat: String, onChatCarregat: (Chat?) -> Unit) {
        viewModelScope.launch {
            try {
                val resultat = chatsDAO.obtenChat(idChat)
                when (resultat) {
                    is Resposta.Exit -> {
                        onChatCarregat(resultat.dades)
                    }
                    is Resposta.Fracas -> {
                        onChatCarregat(null)
                    }
                }
            } catch (e: Exception) {
                onChatCarregat(null)
            }
        }
    }
}
