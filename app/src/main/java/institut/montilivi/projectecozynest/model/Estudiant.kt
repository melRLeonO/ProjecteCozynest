package institut.montilivi.projectecozynest.model

class Estudiant(
    id: String = "",
    correu: String = "",
    nom: String = "",
    cognoms: String = "",
    dataNaixement: String = "",
    genere: String = "",
    fotoPerfil: String = "",
    rol: String = "Estudiant",
    val disponibilitat: Map<String, List<Boolean>> = emptyMap(),
    val tipusHabitacio: String = "",
    val preferenciaTipusAjuda: String = "",
    val duradaEstada: String = "",
    val habilitatsLlar: List<String> = emptyList(),
    val preferenciesConvivencia: List<String> = emptyList()
) : UsuariBase(id, correu, nom, cognoms, dataNaixement, genere,fotoPerfil, rol)
