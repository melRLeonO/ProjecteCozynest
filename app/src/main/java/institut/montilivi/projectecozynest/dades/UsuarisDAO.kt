package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase

interface UsuarisDAO {
    suspend fun obtenUsuaris(): Resposta<List<UsuariBase>>
    suspend fun afegeixUsuari(usuari: UsuariBase): Resposta<Boolean>
    suspend fun modificaUsuari(usuari: UsuariBase): Resposta<Boolean>
    suspend fun existeixUsuari(correu: String): Resposta<Boolean>
    suspend fun obtenUsuari(id: String): Resposta<UsuariBase>
    suspend fun obtenUsuariPerCorreu(correu: String): Resposta<UsuariBase>
    suspend fun bloquejaUsuari(idUsuariQueBloqueja: String, idUsuariBloquejat: String): Resposta<Boolean>
    suspend fun desbloquejaUsuari(idUsuariQueDesbloqueja: String, idUsuariDesbloquejat: String): Resposta<Boolean>
    suspend fun estaBloquejat(idUsuariQueComprova: String, idUsuariAComprovar: String): Resposta<Boolean>
    suspend fun obtenirUsuarisPerNom(nom: String, correuActual: String): Resposta<List<UsuariBase>>
}