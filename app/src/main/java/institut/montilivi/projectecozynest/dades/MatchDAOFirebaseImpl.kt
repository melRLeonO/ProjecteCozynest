package institut.montilivi.projectecozynest.dades

import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.tasks.await

class MatchDAOFirebaseImpl(private val manegadorFirestore: ManegadorFirestore) : MatchDAO {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun crearMatch(estudiantId: String, personaGranId: String, esEstudiant: Boolean): Boolean {
        return try {
            val matchId = listOf(estudiantId, personaGranId).sorted().joinToString("_")
            val nouMatch = hashMapOf(
                "estudiantId" to estudiantId,
                "personaGranId" to personaGranId,
                "accepta_estudiant" to esEstudiant,
                "accepta_persona_gran" to !esEstudiant
            )
            firestore.collection(manegadorFirestore.MATCH).document(matchId).set(nouMatch).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun actualitzarMatch(matchId: String, esEstudiant: Boolean): Boolean {
        return try {
            val actualitzacio = if (esEstudiant) {
                mapOf("accepta_estudiant" to true)
            } else {
                mapOf("accepta_persona_gran" to true)
            }
            firestore.collection(manegadorFirestore.MATCH).document(matchId).update(actualitzacio).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun obtenirMatch(matchId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection(manegadorFirestore.MATCH).document(matchId).get().await()
            doc.data
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun esMatch(matchId: String): Boolean {
        val dades = obtenirMatch(matchId) ?: return false
        val accEst = dades["accepta_estudiant"] as? Boolean ?: false
        val accGran = dades["accepta_persona_gran"] as? Boolean ?: false
        return accEst && accGran
    }

    override suspend fun eliminarMatch(matchId: String): Resposta<Boolean> {
        return try {
            firestore.collection(manegadorFirestore.MATCH)
                .document(matchId)
                .delete()
                .await()
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error eliminant el match")
        }
    }
} 