package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelPerfilChat : ViewModel() {
    private val _usuariActual = MutableStateFlow<UsuariBase?>(null)

    private val _estaBloquejat = MutableStateFlow(false)
    val estaBloquejat: StateFlow<Boolean> = _estaBloquejat.asStateFlow()
    private val _carregantBloqueig = MutableStateFlow(true)

    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val missatgesDAO = DAOFactory.obtenMissatgesDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val matchDAO = DAOFactory.obtenMatchsDAO(null, DAOFactory.TipusBBDD.FIREBASE)

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

    fun comprovaBloqueig(idUsuariAComprovar: String) {
        viewModelScope.launch {
            _carregantBloqueig.value = true
            val idUsuariActual = _usuariActual.value?.id
            if (idUsuariActual != null) {
                when (val resposta = usuarisDAO.estaBloquejat(idUsuariActual, idUsuariAComprovar)) {
                    is Resposta.Exit -> {
                        _estaBloquejat.value = resposta.dades
                    }
                    is Resposta.Fracas -> {
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
                when (val resposta = usuarisDAO.bloquejaUsuari(idUsuariActual, idUsuariBloquejat)) {
                    is Resposta.Exit -> {
                        if (resposta.dades) {
                            _estaBloquejat.value = true
                            eliminarChatYRelacionats(idUsuariActual, idUsuariBloquejat)
                        }
                    }
                    is Resposta.Fracas -> {
                        resposta.missatgeError
                    }
                }
            }
        }
    }

    private suspend fun eliminarChatYRelacionats(usuari1: String, usuari2: String) {
        try {
            val matchId = listOf(usuari1, usuari2).sorted().joinToString("_")
            when (matchDAO.eliminarMatch(matchId)) {
                is Resposta.Exit<*> -> {
                    val chatsRef = chatsDAO.obtenChats(usuari1)
                    chatsRef.collect { resposta ->
                        when (resposta) {
                            is Resposta.Exit<*> -> {
                                val llistaChats = resposta.dades as? List<Chat> ?: emptyList()
                                val chat = llistaChats.find { it.idMatch == matchId }
                                chat?.let {
                                    viewModelScope.launch {
                                        when (missatgesDAO.eliminarMissatgesChat(it.idChat)) {
                                            is Resposta.Exit<*> -> {
                                                when (chatsDAO.eliminarChat(it.idChat)) {
                                                    is Resposta.Exit<*> -> {}
                                                    is Resposta.Fracas -> {}
                                                }
                                            }
                                            is Resposta.Fracas -> {}
                                        }
                                    }
                                }
                            }
                            is Resposta.Fracas -> {}
                        }
                    }
                }
                is Resposta.Fracas -> {}
            }
        } catch (e: Exception) {
            // Log.e("ViewModelPerfilChat", "Error eliminando chat y relacionados", e)
        }
    }

} 
