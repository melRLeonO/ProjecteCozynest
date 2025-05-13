package institut.montilivi.projectecozynest.firestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ManegadorFirestore {
    val USUARIS = "Usuaris"
    val MATCH = "match"
    val VALORACIONS = "Valoracions"
    val CHATS = "Chats"
    val MISSATGES = "Missatges"
    val BLOQUEJOS = "Bloquejos"
    val firestoreDB = FirebaseFirestore.getInstance()

    suspend fun haCompletatRegistre(correu: String): Boolean {
        return try {
            val consulta = firestoreDB.collection(USUARIS)
                .whereEqualTo("correu", correu)
                .get()
                .await()
            
            !consulta.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
