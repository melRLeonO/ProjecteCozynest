package institut.montilivi.projectecozynest.model

import com.google.firebase.Timestamp

data class Chat(
    var idChat: String = "",
    var idMatch: String = "",
    var idEstudiant: String = "",
    var idPersonaGran: String = "",
    var lastMessage: String = "",
    val lastMessageSenderId: String? = null,
    var timestamp: Timestamp? = null,
    var participants: List<String> = listOf()
)
