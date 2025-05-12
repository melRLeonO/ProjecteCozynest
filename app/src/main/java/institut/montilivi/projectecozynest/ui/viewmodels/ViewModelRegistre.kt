package institut.montilivi.projectecozynest.ui.viewmodels
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class ViewModelRegistre : ViewModel() {
    var step = mutableStateOf(1)
    var email = mutableStateOf("")
    var contrasenya = mutableStateOf("")
    var nom = mutableStateOf("")
    var cognom = mutableStateOf("")
    var dataNaixement = mutableStateOf("")
    var selectedGender = mutableStateOf<String?>(null)
    var rol = mutableStateOf("")
    var errorMessage = mutableStateOf("")

    var tipusHabitacio = mutableStateOf("")
    var preferenciaPersona = mutableStateOf("")
    var duradaEstada = mutableStateOf("")

    val downloadUrl = mutableStateOf<String?>(null)
    val allotjamentDownloadUrl = mutableStateOf<String?>(null)
    val superficieAllotjament = mutableStateOf("")
    val descripcioAllotjament = mutableStateOf("")
    val preuAllotjament = mutableStateOf(0)
    val ubicacioAllotjament = mutableStateOf("")

    val disponibilitat = mutableStateMapOf<String, MutableState<BooleanArray>>(
        "Dilluns" to mutableStateOf(BooleanArray(3) { false }),
        "Dimarts" to mutableStateOf(BooleanArray(3) { false }),
        "Dimecres" to mutableStateOf(BooleanArray(3) { false }),
        "Dijous" to mutableStateOf(BooleanArray(3) { false }),
        "Divendres" to mutableStateOf(BooleanArray(3) { false }),
        "Dissabte" to mutableStateOf(BooleanArray(3) { false }),
        "Diumenge" to mutableStateOf(BooleanArray(3) { false })
    )
    val tipusAjudaSolicitada = mutableStateMapOf(
        "Ajuda amb la compra" to mutableStateOf(false),
        "Neteja bàsica" to mutableStateOf(false),
        "Cuinar o ajudar a preparar menjars" to mutableStateOf(false),
        "Acompanyament en passejades" to mutableStateOf(false),
        "Gestió de tràmits senzills" to mutableStateOf(false),
        "Ajuda amb tecnologia" to mutableStateOf(false)
    )

    val preferenciesConvivencia = mutableStateMapOf(
        "No vull conviure amb una persona fumadora" to mutableStateOf(false),
        "M'agrada la idea de conviure amb mascotes" to mutableStateOf(false),
        "No m'agrada el soroll" to mutableStateOf(false),
        "No vull visites a casa" to mutableStateOf(false),
        "M'agradaria menjar junts amb la persona gran" to mutableStateOf(false)
    )
    val habilitatsLlar = mutableStateMapOf(
        "Netejar" to mutableStateOf(false),
        "Cuinar" to mutableStateOf(false),
        "Fer la compra" to mutableStateOf(false),
        "Acompanyar a cites mèdiques" to mutableStateOf(false)
    )

    val preferenciesConvivenciaEstudiant = mutableStateMapOf(
        "Prefereixo un ambient tranquil" to mutableStateOf(false),
        "Estic d'acord amb mascotes" to mutableStateOf(false),
        "M'agrada menjar en companyia" to mutableStateOf(false),
        "No m'agrada fumar a casa" to mutableStateOf(false)
    )


    fun validarStepActual(): Boolean {
        errorMessage.value = ""
        return when (step.value) {
            1 -> {
                if (!email.value.contains("@") || !email.value.contains(".")) {
                    errorMessage.value = "El correu electrònic no és vàlid."
                    return false
                }
                if (contrasenya.value.length < 6) {
                    errorMessage.value = "La contrasenya ha de tenir almenys 6 caràcters."
                    return false
                }
                true
            }
            2 -> {
                if (nom.value.isBlank() || cognom.value.isBlank()) {
                    errorMessage.value = "Has d'introduir nom i cognom."
                    return false
                }
                true
            }
            3 -> {
                val regex = Regex("""\d{2}/\d{2}/\d{4}""")
                if (!regex.matches(dataNaixement.value)) {
                    errorMessage.value = "Format de data incorrecte (ha de ser DD/MM/YYYY)."
                    return false
                }
                try {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val date = LocalDate.parse(dataNaixement.value, formatter)
                    if (date.isAfter(LocalDate.now())) {
                        errorMessage.value = "La data no pot ser en el futur."
                        return false
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Data invàlida."
                    return false
                }
                true
            }
            4 -> {
                if (selectedGender.value == null) {
                    errorMessage.value = "Has de seleccionar un gènere."
                    return false
                }
                true
            }
            6 -> {
                if (rol.value.isBlank()) {
                    errorMessage.value = "Has de seleccionar un rol."
                    return false
                }
                true
            }
            7 -> {
                when (rol.value) {
                    "Persona Gran" -> {
                        if (superficieAllotjament.value.isBlank()) {
                            errorMessage.value = "Has d'indicar la superfície de l'allotjament."
                            return false
                        }
                        if (descripcioAllotjament.value.isBlank()) {
                            errorMessage.value = "Has d'afegir una descripció de l'allotjament."
                            return false
                        }
                        if (ubicacioAllotjament.value.isBlank()) {
                            errorMessage.value = "Has d'especificar la ubicació de l'allotjament."
                            return false
                        }
                    }
                    "Estudiant" -> {
                        if (!hiHaDisponibilitatSeleccionada()) {
                            errorMessage.value = "Has de seleccionar almenys una franja horària disponible."
                            return false
                        }
                    }
                    else -> {
                        errorMessage.value = "Rol desconegut."
                        return false
                    }
                }
                true
            }
            8 -> {
                when (rol.value) {
                    "Persona Gran" -> {
                        val ajudesSeleccionades = tipusAjudaSolicitada.filter { it.value.value }.map { it.key }
                        if (ajudesSeleccionades.isEmpty()) {
                            errorMessage.value = "Has de seleccionar almenys un tipus d'ajuda sol·licitada."
                            return false
                        }
                        val preferenciesSeleccionades = preferenciesConvivencia.filter { it.value.value }.map { it.key }
                        if (preferenciesSeleccionades.isEmpty()) {
                            errorMessage.value = "Has de seleccionar almenys una preferència de convivència."
                            return false
                        }

                    }
                    "Estudiant" -> {
                        if (tipusHabitacio.value.isBlank()) {
                            errorMessage.value = "Has de seleccionar un tipus d’habitació."
                            return false
                        }
                        if (preferenciaPersona.value.isBlank()) {
                            errorMessage.value = "Has de seleccionar una preferència sobre la persona major."
                            return false
                        }
                        if (duradaEstada.value.isBlank()) {
                            errorMessage.value = "Has de seleccionar la durada de l’estada."
                            return false
                        }
                    }
                }
                true
            }
            9 -> {
                if (rol.value == "Estudiant") {
                    val habilitatsSeleccionades = habilitatsLlar.filter { it.value.value }.map { it.key }
                    val preferenciesSeleccionades = preferenciesConvivenciaEstudiant.filter { it.value.value }.map { it.key }

                    if (habilitatsSeleccionades.isEmpty()) {
                        errorMessage.value = "Has de seleccionar almenys una habilitat per a la llar."
                        return false
                    }
                    if (preferenciesSeleccionades.isEmpty()) {
                        errorMessage.value = "Has de seleccionar almenys una preferència de convivència."
                        return false
                    }
                }
                true
            }

            else -> true
        }
    }
    fun hiHaDisponibilitatSeleccionada(): Boolean {
        return disponibilitat.any { (_, stat) ->
            stat.value.any { it }
        }
    }
    fun obtenirDisponibilitat(): Map<String, List<Boolean>> {
        return disponibilitat.mapValues { it.value.value.toList() }
    }
}

