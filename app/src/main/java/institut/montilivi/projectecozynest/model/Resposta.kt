package institut.montilivi.projectecozynest.model

sealed class Resposta<out T>{
    data class Exit<T> (val dades:T):Resposta<T>()
    data class Fracas(val missatgeError:String): Resposta<Nothing>()
}