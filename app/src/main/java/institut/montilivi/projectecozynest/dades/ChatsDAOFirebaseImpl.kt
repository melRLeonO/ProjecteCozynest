package institut.montilivi.projectecozynest.dades

import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ChatsDAOFirebaseImpl (private val db: ManegadorFirestore) : ChatsDAO {
    override suspend fun obtenChats(usuariId: String): Flow<Resposta<List<Chat>>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("Chats")
            .whereArrayContains("participants", usuariId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resposta.Fracas(error.message ?: "Error desconegut"))
                    return@addSnapshotListener
                }

                val llistaChats = snapshots?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
                trySend(Resposta.Exit(llistaChats)).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun afegeixChat(chat: Chat): Resposta<Boolean> {
        var existeix = false
        try {
            runBlocking {
                val resposta = existeixChat(chat.idChat)
                if (resposta is Resposta.Exit)
                    existeix = resposta.dades
                else if (resposta is Resposta.Fracas)
                    throw Exception(resposta.missatgeError)
            }
            if (!existeix) {
                val refEstats = db.firestoreDB.collection(db.CHATS)
                val refEstatNou = refEstats.document()
                chat.idChat = refEstatNou.id
                refEstatNou.set(chat).await()
                existeix = true
            }


        }catch (e: Exception) {
            return Resposta.Fracas( missatgeError =  e.message?: "Error afegint chat")
        }
        return Resposta.Exit(existeix)
    }

    override suspend fun modificaChat(chat: Chat): Resposta<Boolean> {
        var modificat = false
        try {
            val refChats = db.firestoreDB.collection(db.CHATS)
            val refChat = refChats.document(chat.idChat)
            refChat.set(chat).await()
            modificat = true
        }catch (e: Exception) {
            return Resposta.Fracas(e.message?:"Error cercant el chat")
        }
        return Resposta.Exit(modificat)
    }

    override suspend fun existeixChat(id: String): Resposta<Boolean> {
        var consultaBuida = false
        try {
            val refChats = db.firestoreDB.collection(db.CHATS)
            val consulta = refChats.whereEqualTo("idChat", id)
            consultaBuida = consulta.get().await().documents.isEmpty()
        }catch (e: Exception) {
            return Resposta.Fracas(e.message?:"Error cercant el chat")
        }
        return Resposta.Exit(!consultaBuida)
    }

    override suspend fun obtenChat(id: String): Resposta<Chat> {
        var chat: Chat = Chat()
        try {
            val refChats = db.firestoreDB.collection(db.CHATS)
            val refChat = refChats.document(id)
            val document = refChat.get().await()
            chat = document.toObject(Chat::class.java)?: Chat()

        } catch (e: Exception) {
            return Resposta.Fracas(e.message ?: "Error cercant Chat")
        }
        return Resposta.Exit(chat)
    }

    override suspend fun crearChat(estudiantId: String, personaGranId: String): Resposta<Boolean> {
        return try {
            val matchId = listOf(estudiantId, personaGranId).sorted().joinToString("_")
            val chatsRef = db.firestoreDB.collection(db.CHATS)
            
            // Verificar si ya existe el chat
            val consulta = chatsRef.whereEqualTo("idMatch", matchId).get().await()
            if (!consulta.isEmpty) {
                return Resposta.Exit(true) // El chat ya existe
            }

            // Crear nuevo chat
            val docRef = chatsRef.document()
            val chatData = hashMapOf(
                "idChat" to docRef.id,
                "idMatch" to matchId,
                "idEstudiant" to estudiantId,
                "idPersonaGran" to personaGranId,
                "lastMessage" to "",
                "lastMessageSenderId" to "",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "participants" to listOf(estudiantId, personaGranId)
            )
            docRef.set(chatData).await()
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error creant el chat")
        }
    }

    override suspend fun eliminarChat(idChat: String): Resposta<Boolean> {
        return try {
            db.firestoreDB.collection(db.CHATS)
                .document(idChat)
                .delete()
                .await()
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error eliminant el chat")
        }
    }
}