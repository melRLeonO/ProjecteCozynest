package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import institut.montilivi.projectecozynest.model.Valoracio
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.update
import kotlin.math.*
import kotlinx.coroutines.launch
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore

class ViewModelLike : ViewModel() {
    val _usuariActual = MutableStateFlow<UsuariBase?>(null)
    private var usuarisExcluits: List<String> = listOf()
    private var genereFiltratActual: String? = null
    private var distanciaMaximaActual: Int = 0
    private var ciutat: String? = null
    private var edatMinActual: Int = 0
    private var edatMaxActual: Int = 99
    private var _estat = MutableStateFlow(Estat())
    val estat: StateFlow<Estat> = _estat.asStateFlow()
    private var numEstrellesMinActual: Int = 0
    private var numValoracionsMinActual: Int = 0
    private var usuariActual: UsuariBase? = null
    private val correuActual = FirebaseAuth.getInstance().currentUser?.email
    private var selectedLatLngActual: LatLng? = null
    private val matchDAO = DAOFactory.obtenMatchsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val chatsDAO = DAOFactory.obtenChatsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val manegadorFirestore = ManegadorFirestore()

    init {    obtenirUsuariActual()
        obtenirUsuariAleatoriFiltrat(
            genereFiltratActual, edatMinActual, edatMaxActual,
            numEstrellesMinActual, numValoracionsMinActual,
            ciutat, distanciaMaximaActual, selectedLatLngActual
        )
    }
    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            viewModelScope.launch {
                try {
                    val resultat = usuarisDAO.obtenUsuariPerCorreu(currentUserEmail)
                    when (resultat) {
                        is Resposta.Exit -> {
                            _usuariActual.value = resultat.dades
                        }
                        is Resposta.Fracas -> {
                            _usuariActual.value = null
                        }
                    }
                } catch (e: Exception) {
                    _usuariActual.value = null
                }
            }
        }
    }
    fun obtenirUsuariAleatoriFiltrat(
        genere: String?,
        edatMin: Int,
        edatMax: Int,
        numEstrellesMin: Int,
        numValoracionsMin: Int,
        ciutat: String?,
        distanciaMaxima: Int,
        selectedLatLng: LatLng?
    ) {
        genereFiltratActual = genere
        val col = manegadorFirestore.firestoreDB.collection(manegadorFirestore.USUARIS)
        val matchRef = manegadorFirestore.firestoreDB.collection(manegadorFirestore.MATCH)
        val valoracionsRef = manegadorFirestore.firestoreDB.collection(manegadorFirestore.VALORACIONS)

        correuActual?.let { correu ->
            col.whereEqualTo("correu", correu).get().addOnSuccessListener { docs ->
                val actual = docs.firstOrNull()?.toObject(UsuariBase::class.java)
                usuariActual = actual

                if (actual != null) {
                    val bloquejosRef = manegadorFirestore.firestoreDB.collection(manegadorFirestore.BLOQUEJOS)
                    val idsExclososPerBloqueig = mutableSetOf<String>()

                    val tascaBloquejosFa = bloquejosRef
                        .whereEqualTo("idUsuariQueBloqueja", actual.id).get()
                    val tascaBloquejosRep = bloquejosRef
                        .whereEqualTo("idUsuariBloquejat", actual.id).get()

                    Tasks.whenAllSuccess<QuerySnapshot>(tascaBloquejosFa, tascaBloquejosRep)
                        .addOnSuccessListener { resultatsBloquejos ->
                            val bloquejosFa = resultatsBloquejos[0].documents
                            val bloquejosRep = resultatsBloquejos[1].documents

                            bloquejosFa.forEach {
                                it.getString("idUsuariBloquejat")?.let { id -> idsExclososPerBloqueig.add(id) }
                            }
                            bloquejosRep.forEach {
                                it.getString("idUsuariQueBloqueja")?.let { id -> idsExclososPerBloqueig.add(id) }
                            }

                    val rolOposat = if (actual.rol == "Estudiant") "Persona Gran" else "Estudiant"
                    val idsLikeDonats = mutableSetOf<String>()

                    matchRef.whereEqualTo("estudiantId", actual.id).get().addOnSuccessListener { matchDocsEstudiant ->
                    matchRef.whereEqualTo("personaGranId", actual.id).get().addOnSuccessListener { matchDocsPersonaGran ->
                            (matchDocsEstudiant + matchDocsPersonaGran).forEach { doc ->
                                val data = doc.data
                                val accEst = data["accepta_estudiant"] as? Boolean ?: false
                                val accGran = data["accepta_persona_gran"] as? Boolean ?: false
                                val idEstudiant = data["estudiantId"] as? String
                                val idPersonaGran = data["personaGranId"] as? String

                                if (actual.rol == "Estudiant" && accEst) {
                                    idsLikeDonats.add(idPersonaGran ?: "")
                                }
                                if (actual.rol != "Estudiant" && accGran) {
                                    idsLikeDonats.add(idEstudiant ?: "")
                                }
                            }

                            usuarisExcluits = idsLikeDonats.toList()
                            col.whereEqualTo("rol", rolOposat).get().addOnSuccessListener { resultats ->
                                val candidats = resultats.mapNotNull {
                                    val rol = it.getString("rol")
                                    when (rol) {
                                        "Estudiant" -> it.toObject(Estudiant::class.java)
                                        "Persona Gran" -> it.toObject(PersonaGran::class.java)
                                        else -> null
                                    }
                                }
                                    .filter { it.correu != correu }
                                    .filter { !usuarisExcluits.contains(it.id) }
                                    .filter {
                                        genere.isNullOrBlank() || it.genere.trim()
                                            .equals(genere, ignoreCase = false)
                                    }
                                    .filter { calcularEdat(it.dataNaixement).also { edat -> } in edatMin..edatMax }
                                    .filter {
                                        if (usuariActual?.rol == "Estudiant" && it is PersonaGran) {
                                            if (distanciaMaxima > 0 && selectedLatLng != null && it.latitud != null && it.longitud != null) {
                                                val distancia = calcularDistancia(
                                                    selectedLatLng.latitude,
                                                    selectedLatLng.longitude,
                                                    it.latitud,
                                                    it.longitud
                                                )
                                                distancia <= distanciaMaxima
                                            } else {
                                                val resultat =
                                                    ciutat.isNullOrBlank() || it.ubicacioAllotjament.equals(
                                                        ciutat,
                                                        ignoreCase = true
                                                    )
                                                resultat
                                            }
                                        } else true
                                    }

                                    .filter {
                                        val selectedLat = selectedLatLng?.latitude
                                        val selectedLon = selectedLatLng?.longitude

                                        if (it is PersonaGran && selectedLat != null && selectedLon != null && it.latitud != null && it.longitud != null) {
                                            val distancia = calcularDistancia(
                                                selectedLat,
                                                selectedLon,
                                                it.latitud,
                                                it.longitud
                                            )
                                            if (distanciaMaxima > 0) {
                                                distancia <= distanciaMaxima
                                            } else {
                                                true
                                            }
                                        } else {
                                            true
                                        }
                                    }
                                val candidatsFiltrats = mutableListOf<UsuariBase>()
                                if (candidats.isEmpty()) {
                                    _estat.update {
                                        it.copy(
                                            usuaris = emptyList(),
                                            error = "No hi ha usuaris disponibles amb aquests filtres"
                                        )
                                    }
                                    return@addOnSuccessListener
                                }

                                var procesats = 0

                                candidats.forEach { usuari ->
                                    valoracionsRef
                                        .whereEqualTo("idUsuariValorat", usuari.id)
                                        .get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val valDocs = task.result
                                                val valoracions =
                                                    valDocs.mapNotNull { it.toObject(Valoracio::class.java) }
                                                val mitjana = if (valoracions.isNotEmpty()) {
                                                    valoracions.map { it.puntuacio }.average()
                                                } else {
                                                    0.0
                                                }
                                                val numValoracions = valoracions.size
                                                if (mitjana >= numEstrellesMin && numValoracions >= numValoracionsMin) {
                                                    candidatsFiltrats.add(usuari)
                                                }
                                            }
                                            procesats++
                                            if (procesats == candidats.size) {
                                                if (candidatsFiltrats.isNotEmpty()) {
                                                    val usuariAleatori = candidatsFiltrats.random()
                                                    _estat.update {
                                                        it.copy(
                                                            usuaris = listOf(usuariAleatori),
                                                            error = ""
                                                        )
                                                    }
                                                } else {
                                                    _estat.update {
                                                        it.copy(
                                                            usuaris = emptyList(),
                                                            error = "No hi ha usuaris amb ${numEstrellesMin} estrelles o més"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                            }
                        }
                    }
                } else {
                    _estat.update {
                        it.copy(error = "Usuari actual no trobat")
                    }
                }
            }.addOnFailureListener {

            }
        }
    }

    fun ferLike(callback: (Boolean) -> Unit) {
        val usuariMostrat = _estat.value.usuaris.firstOrNull()
        val actual = usuariActual

        if (usuariMostrat != null && actual != null) {
            val (estudiantId, personaGranId, esEstudiant) = if (actual.rol == "Estudiant") {
                Triple(actual.id, usuariMostrat.id, true)
            } else {
                Triple(usuariMostrat.id, actual.id, false)
            }

            val matchId = listOf(estudiantId, personaGranId).sorted().joinToString("_")

            viewModelScope.launch {
                val matchExistent = matchDAO.obtenirMatch(matchId)
                if (matchExistent != null) {
                    val actualitzat = matchDAO.actualitzarMatch(matchId, esEstudiant)
                    if (actualitzat) {
                        val esMatch = matchDAO.esMatch(matchId)
                        if (esMatch) {
                            crearChat(estudiantId, personaGranId)
                        }
                        callback(esMatch)
                    }
                } else {
                    // Crear nuevo match
                    val creat = matchDAO.crearMatch(estudiantId, personaGranId, esEstudiant)
                    if (creat) {
                        val esMatch = matchDAO.esMatch(matchId)
                        if (esMatch) {
                            crearChat(estudiantId, personaGranId)
                        }
                        callback(esMatch)
                    } else {
                        callback(false)
                    }
                }
            }
        }
    }
    fun obtenirUsuarisPerNom(nom: String) {
        viewModelScope.launch {
            correuActual?.let { correu ->
                when (val resposta = usuarisDAO.obtenirUsuarisPerNom(nom, correu)) {
                    is Resposta.Exit -> {
                        _estat.update { it.copy(usuaris = resposta.dades) }
                    }
                    is Resposta.Fracas -> {
                        _estat.update { it.copy(error = resposta.missatgeError) }
                    }
                }
            }
        }
    }

    private fun crearChat(estudiantId: String, personaGranId: String) {
        viewModelScope.launch {
            chatsDAO.crearChat(estudiantId, personaGranId)
        }
    }
    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = radioTierra * c
        return distancia
    }
    private fun calcularEdat(dataNaixement: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(dataNaixement)
            val birthCal = Calendar.getInstance()
            val today = Calendar.getInstance()
            birthCal.time = birthDate!!
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            -1
        }
    }
    data class Estat(
        val usuaris: List<UsuariBase> = listOf(),
        val error: String = ""
    )
}
