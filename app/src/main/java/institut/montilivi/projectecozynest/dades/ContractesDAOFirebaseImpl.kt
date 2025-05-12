package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.ContracteDigital
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ContractesDAOFirebaseImpl(private val db: ManegadorFirestore) : ContractesDAO {
    override suspend fun afegeixContracte(contracte: ContracteDigital): Resposta<Boolean> {
        var existeix = false
        try {
            runBlocking {
                val resposta = existeixContracte(contracte.idContracte)
                if (resposta is Resposta.Exit)
                    existeix = resposta.dades
                else if (resposta is Resposta.Fracas)
                    throw Exception(resposta.missatgeError)
            }
            if (!existeix) {
                val refEstats = db.firestoreDB.collection(db.CONTRACTES)
                val refEstatNou = refEstats.document()
                contracte.idContracte = refEstatNou.id
                refEstatNou.set(contracte).await()
                existeix = true
            }


        }catch (e: Exception) {
            return Resposta.Fracas( missatgeError =  e.message?: "Error afegint contracte")
        }
        return Resposta.Exit(existeix)
    }

    override suspend fun modificaContracte(contracte: ContracteDigital): Resposta<Boolean> {
        var modificat = false
        try {
            val refContractes = db.firestoreDB.collection(db.CONTRACTES)
            val refChat = refContractes.document(contracte.idContracte)
            refChat.set(contracte).await()
            modificat = true
        }catch (e: Exception) {
            return Resposta.Fracas(e.message?:"Error cercant el contracte")
        }
        return Resposta.Exit(modificat)
    }

    override suspend fun existeixContracte(id: String): Resposta<Boolean> {
        var consultaBuida = false
        try {
            val refChats = db.firestoreDB.collection(db.CONTRACTES)
            val consulta = refChats.whereEqualTo("idContracte", id)
            consultaBuida = consulta.get().await().documents.isEmpty()
        }catch (e: Exception) {
            return Resposta.Fracas(e.message?:"Error cercant el contracte")
        }
        return Resposta.Exit(!consultaBuida)
    }


    override suspend fun obtenContractes(usuariId: String): Flow<Resposta<List<ContracteDigital>>> = callbackFlow {
            val colRef = db.firestoreDB.collection(db.CONTRACTES)
            val listener = colRef
                .whereEqualTo("idUsuariReceptor", usuariId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        trySend(Resposta.Fracas(error.message ?: "Error desconegut"))
                        return@addSnapshotListener
                    }

                    val contractes = snapshots?.documents?.mapNotNull {
                        it.toObject(ContracteDigital::class.java)
                    } ?: emptyList()

                    trySend(Resposta.Exit(contractes)).isSuccess
                }

            awaitClose {
                listener.remove()
            }
        }

        // Aquí podries afegir altres mètodes com afegeixContracte, modificaContracte, existeixContracte, obtenContracte, etc.


    override suspend fun obtenContracte(id: String): Resposta<ContracteDigital> {
        var contracte: ContracteDigital = ContracteDigital()
        try {
            val refContractes = db.firestoreDB.collection(db.CONTRACTES)
            val refContracte = refContractes.document(id)
            val document = refContracte.get().await()
            contracte = document.toObject(ContracteDigital::class.java)?: ContracteDigital()

        } catch (e: Exception) {
            return Resposta.Fracas(e.message ?: "Error cercant Contracte Digital")
        }
        return Resposta.Exit(contracte)
    }

}
