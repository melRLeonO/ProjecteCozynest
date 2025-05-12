package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.ContracteDigital
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.Flow


interface ContractesDAO {
    suspend fun afegeixContracte(contracte: ContracteDigital): Resposta<Boolean>
    suspend fun modificaContracte(contracte: ContracteDigital): Resposta<Boolean>
    suspend fun existeixContracte(id: String): Resposta<Boolean>
    suspend fun obtenContracte(id: String): Resposta<ContracteDigital>
    suspend fun obtenContractes(usuariId: String): Flow<Resposta<List<ContracteDigital>>>
}