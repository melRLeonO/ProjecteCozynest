package institut.montilivi.projectecozynest.model

import com.google.firebase.Timestamp

data class ContracteDigital(
    var idContracte: String = "",
    var idUsuariCreador: String = "",
    var idUsuariReceptor: String = "",
    var horesAsistenciaSetmanal: String = "",
    var horarisConvivencia: String = "",
    var normesDeConvivencia: String = "",
    var duracioMinima: String = "",
    var preuAllotjament: Int = 0,
    var dniCreador: String = "",
    var dniReceptor: String = "",
    var dataCreacio: Timestamp? = null,
    var dataAcceptacioReceptor: Timestamp? = null,
    var estat: String = "pendent", // "pendent", "acceptat", "rebutjat"
)
