package institut.montilivi.projectecozynest.model
import com.google.firebase.Timestamp
data class Missatge(
    var idChat: String = "",
    var remitent: String = "",
    var text: String = "",
    var timestamp: Timestamp? = null
)

