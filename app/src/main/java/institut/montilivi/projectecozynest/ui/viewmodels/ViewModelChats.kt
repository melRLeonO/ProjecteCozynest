package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import institut.montilivi.projectecozynest.dades.DAOFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelChats : ViewModel() {
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    var rolActual: String? = null

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val correuActual = FirebaseAuth.getInstance().currentUser?.email
    var usuariIdActual: String? = null

    init {
        obtenirUsuariActualId()
    }

    private fun obtenirUsuariActualId() {
        correuActual?.let { correu ->
            viewModelScope.launch {
                try {
                    val resultat = usuarisDAO.obtenUsuariPerCorreu(correu)
                    when (resultat) {
                        is Resposta.Exit -> {
                            usuariIdActual = resultat.dades.id
                            rolActual = resultat.dades.rol
                            usuariIdActual?.let { carregarChats(it) }
                        }
                        is Resposta.Fracas -> {
                            // Manejar el error si es necesario
                        }
                    }
                } catch (e: Exception) {
                    // Manejar la excepción si es necesario
                }
            }
        }
    }

    suspend fun obtenirNomUsuariPerId(id: String): String {
        return try {
            val resultat = usuarisDAO.obtenUsuari(id)
            when (resultat) {
                is Resposta.Exit -> {
                    "${resultat.dades.nom} ${resultat.dades.cognoms}".trim()
                }
                is Resposta.Fracas -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun obtenirFotoUsuariPerId(id: String): String {
        return try {
            val resultat = usuarisDAO.obtenUsuari(id)
            when (resultat) {
                is Resposta.Exit -> resultat.dades.fotoPerfil
                is Resposta.Fracas -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun carregarChats(usuariId: String) {
        viewModelScope.launch {
            try {
                chatsDAO.obtenChats(usuariId).collect { resposta ->
                    when (resposta) {
                        is Resposta.Exit -> _chats.value = resposta.dades
                        is Resposta.Fracas -> {
                            // Podrías manejar el error mostrando un mensaje, etc.
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar la excepción si es necesario
            }
        }
    }
}