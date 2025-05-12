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
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.model.UsuariBase
import android.util.Log
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran

class ViewModelConversa : ViewModel() {
    private val _altreUsuari = MutableStateFlow<UsuariBase?>(null)
    val altreUsuari: StateFlow<UsuariBase?> = _altreUsuari.asStateFlow()
    private val _usuariActual = MutableStateFlow<UsuariBase?>(null)
    val usuariActual: StateFlow<UsuariBase?> = _usuariActual.asStateFlow()

    private val missatgesDAO = DAOFactory.obtenMissatgesDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    private val _missatges = MutableStateFlow<List<Missatge>>(emptyList())
    val missatges: StateFlow<List<Missatge>> = _missatges.asStateFlow()

    init {
        obtenirUsuariActual()
    }

    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            FirebaseFirestore.getInstance().collection("Usuaris")
                .whereEqualTo("correu", currentUserEmail)
                .get()
                .addOnSuccessListener { documents ->
                    val document = documents.firstOrNull()
                    if (document != null) {
                        val rol = document.getString("rol")
                        val usuari = when (rol) {
                            "Estudiant" -> document.toObject(Estudiant::class.java)
                            "Persona Gran" -> document.toObject(PersonaGran::class.java)
                            else -> document.toObject(UsuariBase::class.java)
                        }
                        _usuariActual.value = usuari
                    } else {
                        _usuariActual.value = null
                    }
                }
                .addOnFailureListener {
                    _usuariActual.value = null
                }
        }
    }
    fun carregarMissatges(idChat: String) {
        Log.d("DEBUG", "Iniciant càrrega de missatges per al chat $idChat")
        viewModelScope.launch {
            missatgesDAO.obtenMissatges(idChat).collect { resposta ->
                if (resposta is Resposta.Exit) {
                    Log.d("DEBUG", "Missatges rebuts: ${resposta.dades.size}")
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
                } else if (resposta is Resposta.Fracas) {
                    Log.e("DEBUG", "Error rebent missatges: ${resposta.missatgeError}")
                }
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
            val resultat = missatgesDAO.afegirMissatge(missatge)
            if (resultat is Resposta.Exit) {
                // Actualizar el chat con el nuevo lastMessage y timestamp
                val chatRef = FirebaseFirestore.getInstance().collection("Chats").document(idChat)
                chatRef.update(
                    mapOf(
                        "lastMessage" to missatge.text,
                        "lastMessageSenderId" to usuari.id, // 👈 Añadir esta línea
                        "timestamp" to missatge.timestamp
                    )
                )

            }
        }
    }

    fun carregarAltreUsuari(id: String) {
        FirebaseFirestore.getInstance().collection("Usuaris").document(id)
            .get()
            .addOnSuccessListener { document ->
                val rol = document.getString("rol")
                val usuari = when (rol) {
                    "Estudiant" -> document.toObject(Estudiant::class.java)
                    "Persona Gran" -> document.toObject(PersonaGran::class.java)
                    else -> document.toObject(UsuariBase::class.java)
                }
                _altreUsuari.value = usuari
            }
    }
    fun carregarChat(idChat: String, onChatCarregat: (Chat?) -> Unit) {
        FirebaseFirestore.getInstance().collection("Chats")  // Asegúrate del nombre correcto
            .document(idChat)
            .get()
            .addOnSuccessListener { document ->
                val chat = document.toObject(Chat::class.java)
                onChatCarregat(chat)
            }
            .addOnFailureListener {
                onChatCarregat(null)
            }
    }

}
