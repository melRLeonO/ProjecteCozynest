package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.Valoracio
import kotlinx.coroutines.flow.Flow

interface ValoracionsDAO {
    suspend fun obtenValoracions(): Flow<Resposta<List<Valoracio>>>
    suspend fun afegeixValoracio(valoracio: Valoracio): Resposta<Boolean>
    suspend fun obtenValoracio(id:String): Resposta<Valoracio>
    suspend fun existeixValoracio(id: String): Resposta<Boolean>
}