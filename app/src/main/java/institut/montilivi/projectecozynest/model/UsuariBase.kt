package institut.montilivi.projectecozynest.model

open class UsuariBase(
    val id: String = "",
    val correu: String = "",
    val nom: String = "",
    val cognoms: String = "",
    val dataNaixement: String = "",
    val genere: String = "",
    val fotoPerfil: String = "",
    val rol: String = "" // "Estudiant" o "Persona Gran"
)
