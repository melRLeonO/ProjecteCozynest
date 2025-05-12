package institut.montilivi.projectecozynest.model

class PersonaGran(
    id: String = "",
    correu: String = "",
    nom: String = "",
    cognoms: String = "",
    dataNaixement: String = "",
    genere: String = "",
    fotoPerfil: String = "",
    rol: String = "Persona Gran",
    val imatgesAllotjament: String = "",
    val ubicacioAllotjament: String = "",
    val preuAllotjament: Int = 0,  // Cambiar de Float a Int
    val superficieAllotjament: String = "",
    val descripcioAllotjament: String = "",
    val tipusAjudaSolicitada: List<String> = emptyList(),
    val preferenciesConvivencia: List<String> = emptyList(),
    val latitud: Double? = null,
    val longitud: Double? = null
) : UsuariBase(id, correu, nom, cognoms, dataNaixement, genere,fotoPerfil, rol)
