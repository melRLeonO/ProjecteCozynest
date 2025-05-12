package institut.montilivi.projectecozynest.model

data class Valoracio(
    var idValoracio: String = "",
    var idUsuariQueValora: String = "",
    var nomUsuariQueValora: String = "",
    var correuUsuariQueValora: String = "",
    var idUsuariValorat: String = "",
    var puntuacio: Int = 0,
    var comentari: String = "",
    var dataValoracio: String = ""
)
