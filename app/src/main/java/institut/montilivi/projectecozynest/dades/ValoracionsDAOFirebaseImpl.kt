package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.Valoracio
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class ValoracionsDAOFirebaseImpl (private val db: ManegadorFirestore) : ValoracionsDAO {
    override suspend fun obtenValoracions(): Flow<Resposta<List<Valoracio>>> = callbackFlow{
       try{
           val refValoracions = db.firestoreDB.collection(db.VALORACIONS)
           val subscripcio = refValoracions.addSnapshotListener{
                   snapshot, _ ->
               snapshot?.let { querySnapshot ->
                   val valoracions = mutableListOf<Valoracio>()
                   for (document in querySnapshot.documents) {
                       val valoracio = document.toObject(Valoracio::class.java)
                       valoracio?.let { valoracions.add(it) }
                   }

                   trySend(Resposta.Exit(valoracions)).isSuccess
               }
           }
           awaitClose { subscripcio.remove() }
       }
       catch(e: Exception){
           trySend(Resposta.Fracas(e.message ?: "Error obtenint Valoracions"))
       }
    }

    override suspend fun afegeixValoracio(valoracio: Valoracio): Resposta<Boolean> {
        var existeix = false
        try {
            runBlocking {
                val resposta = existeixValoracio(valoracio.idValoracio)
                if (resposta is Resposta.Exit)
                    existeix = resposta.dades
                else if (resposta is Resposta.Fracas)
                    throw Exception(resposta.missatgeError)
            }
            if (!existeix) {
                val refEstats = db.firestoreDB.collection(db.VALORACIONS)
                val refEstatNou = refEstats.document()
                valoracio.idValoracio = refEstatNou.id
                refEstatNou.set(valoracio)
                existeix = true
            }

        }catch (e: Exception) {
            return Resposta.Fracas( missatgeError =  e.message?: "Error afegint valoracio")
        }
        return Resposta.Exit(existeix)
    }

    override suspend fun obtenValoracio(id: String): Resposta<Valoracio> {
        var valoracio = Valoracio()
        try {
            val refValoracions = db.firestoreDB.collection(db.VALORACIONS)
            val refValoracio = refValoracions.document(id)
            val document = refValoracio.get().await()
            valoracio = document.toObject(Valoracio::class.java)?: Valoracio()
        }
        catch (e: Exception){
            return Resposta.Fracas(e.message?: "Error cercant la valoració")
        }
        return Resposta.Exit(valoracio)
    }

    override suspend fun existeixValoracio(id: String): Resposta<Boolean> {
        var consultaBuida = false
        try {
            val refValoracions = db.firestoreDB.collection(db.VALORACIONS)
            val consulta = refValoracions.whereEqualTo("id", id)
            consultaBuida = consulta.get().await().documents.isEmpty()
        }catch (e: Exception) {
            return Resposta.Fracas(e.message?:"Error cercant la valoracio $id")
        }
        return Resposta.Exit(!consultaBuida)
    }
}
