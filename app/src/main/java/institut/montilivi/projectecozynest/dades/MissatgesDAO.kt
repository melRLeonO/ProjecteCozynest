package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.Missatge
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.Flow

interface MissatgesDAO {
    suspend fun afegirMissatge(missatge: Missatge): Resposta<Boolean>
    suspend fun obtenMissatges(idChat: String): Flow<Resposta<List<Missatge>>>
}
