package institut.montilivi.projectecozynest.dades

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Missatge
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MissatgesDAOFirebaseImpl(private val db: ManegadorFirestore) : MissatgesDAO {

    override suspend fun afegirMissatge(missatge: Missatge): Resposta<Boolean> {
        return try {
            val ref = db.firestoreDB.collection(db.MISSATGES)
            ref.add(missatge).await()
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error afegint missatge")
        }
    }

    override suspend fun obtenMissatges(idChat: String): Flow<Resposta<List<Missatge>>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val ref = db.firestoreDB.collection(db.MISSATGES)
                .whereEqualTo("idChat", idChat)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            listener = ref.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resposta.Fracas(error.message ?: "Error obtenint missatges"))
                    return@addSnapshotListener
                }

                val missatges = snapshot?.documents?.mapNotNull {
                    it.toObject(Missatge::class.java)
                } ?: emptyList()
                trySend(Resposta.Exit(missatges)).isSuccess
            }
        } catch (e: Exception) {
            trySend(Resposta.Fracas(e.message ?: "Error obtenint missatges"))
        }

        awaitClose { listener?.remove() }
    }

    override suspend fun eliminarMissatgesChat(idChat: String): Resposta<Boolean> {
        return try {
            val missatges = db.firestoreDB.collection(db.MISSATGES)
                .whereEqualTo("idChat", idChat)
                .get()
                .await()
                .documents

            for (missatge in missatges) {
                missatge.reference.delete().await()
            }
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error eliminant els missatges del chat")
        }
    }

}
