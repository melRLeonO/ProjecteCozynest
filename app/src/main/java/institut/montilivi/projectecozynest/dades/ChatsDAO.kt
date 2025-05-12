package institut.montilivi.projectecozynest.dades

import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.Flow


interface ChatsDAO {
    suspend fun obtenChats(usuariId : String): Flow<Resposta<List<Chat>>>
    suspend fun afegeixChat(chat: Chat): Resposta<Boolean>
    suspend fun modificaChat(chat: Chat): Resposta<Boolean>
    suspend fun existeixChat(id: String): Resposta<Boolean>
    suspend fun obtenChat(id:String): Resposta<Chat>
}