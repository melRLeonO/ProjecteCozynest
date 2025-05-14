package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.Resposta

interface MatchDAO {
    suspend fun crearMatch(estudiantId: String, personaGranId: String, esEstudiant: Boolean): Boolean
    suspend fun actualitzarMatch(matchId: String, esEstudiant: Boolean): Boolean
    suspend fun obtenirMatch(matchId: String): Map<String, Any>?
    suspend fun esMatch(matchId: String): Boolean
    suspend fun eliminarMatch(matchId: String): Resposta<Boolean>
} 