package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.Timestamp
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import institut.montilivi.projectecozynest.model.Valoracio
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.update
import kotlin.math.*
class ViewModelLike : ViewModel() {
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

    init {
        Log.d("FiltroDebug", "INIT ViewModelLike")
        Log.d("FiltroDebug", "selectedLatLngActual al init: $selectedLatLngActual")
        obtenirUsuariAleatoriFiltrat(
            genereFiltratActual, edatMinActual, edatMaxActual,
            numEstrellesMinActual, numValoracionsMinActual,
            ciutat, distanciaMaximaActual, selectedLatLngActual
        )
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
        Log.d("FiltroDebug", "Iniciant filtre → genere=$genere, edatMin=$edatMin, edatMax=$edatMax, numEstrellesMin=$numEstrellesMin, numValoracionsMin=$numValoracionsMin, ciutat=$ciutat, distanciaMaxima=$distanciaMaxima, selectedLatLng=$selectedLatLng")

        genereFiltratActual = genere
        val firestore = FirebaseFirestore.getInstance()
        val col = firestore.collection("Usuaris")
        val matchRef = firestore.collection("match")
        val valoracionsRef = firestore.collection("Valoracions")

        Log.d("FiltroDebug", "Obtenint usuari actual amb correu=$correuActual")

        correuActual?.let { correu ->
            col.whereEqualTo("correu", correu).get().addOnSuccessListener { docs ->
                Log.d("FiltroDebug", "Resultat consulta usuari actual: ${docs.size()} documents")
                val actual = docs.firstOrNull()?.toObject(UsuariBase::class.java)
                usuariActual = actual

                if (actual != null) {
                    Log.d("FiltroDebug", "Usuari actual trobat: ${actual.id}, rol=${actual.rol}")
                    val rolOposat = if (actual.rol == "Estudiant") "Persona Gran" else "Estudiant"
                    val idsLikeDonats = mutableSetOf<String>()

                    matchRef.whereEqualTo("estudiantId", actual.id).get().addOnSuccessListener { matchDocsEstudiant ->
                        Log.d("FiltroDebug", "Matches on estudiantId: ${matchDocsEstudiant.size()} documents")
                        matchRef.whereEqualTo("personaGranId", actual.id).get().addOnSuccessListener { matchDocsPersonaGran ->
                            Log.d("FiltroDebug", "Matches on personaGranId: ${matchDocsPersonaGran.size()} documents")

                            (matchDocsEstudiant + matchDocsPersonaGran).forEach { doc ->
                                val data = doc.data
                                Log.d("FiltroDebug", "Processant match doc: ${doc.id}, data=$data")

                                val accEst = data["accepta_estudiant"] as? Boolean ?: false
                                val accGran = data["accepta_persona_gran"] as? Boolean ?: false
                                val idEstudiant = data["estudiantId"] as? String
                                val idPersonaGran = data["personaGranId"] as? String

                                if (actual.rol == "Estudiant" && accEst) {
                                    idsLikeDonats.add(idPersonaGran ?: "")
                                    Log.d("FiltroDebug", "Afegit a usuarisExcluits (Estudiant): $idPersonaGran")
                                }
                                if (actual.rol != "Estudiant" && accGran) {
                                    idsLikeDonats.add(idEstudiant ?: "")
                                    Log.d("FiltroDebug", "Afegit a usuarisExcluits (Persona Gran): $idEstudiant")
                                }
                            }

                            usuarisExcluits = idsLikeDonats.toList()
                            Log.d("FiltroDebug", "IDs usuaris excluits finals: $usuarisExcluits")

                            col.whereEqualTo("rol", rolOposat).get().addOnSuccessListener { resultats ->
                                Log.d("FiltroDebug", "Total candidats abans filtres: ${resultats.size()} documents")
                                val candidats = resultats.mapNotNull {
                                    val rol = it.getString("rol")
                                    Log.d("FiltroDebug", "Convertint document ${it.id} a objecte, rol=$rol")
                                    when (rol) {
                                        "Estudiant" -> it.toObject(Estudiant::class.java)
                                        "Persona Gran" -> it.toObject(PersonaGran::class.java)
                                        else -> null
                                    }
                                }
                                    .filter { it.correu != correu }
                                    .also { Log.d("FiltroDebug", "Després filtre correu diferent, ${it.size} candidats") }
                                    .filter { !usuarisExcluits.contains(it.id) }
                                    .also { Log.d("FiltroDebug", "Després filtre usuarisExcluits, ${it.size} candidats") }
                                    .filter { genere.isNullOrBlank() || it.genere.trim().equals(genere, ignoreCase = false) }
                                    .also { Log.d("FiltroDebug", "Després filtre genere, ${it.size} candidats") }
                                    .filter { calcularEdat(it.dataNaixement).also { edat -> Log.d("FiltroDebug", "Usuari ${it.nom} té edat $edat") } in edatMin..edatMax }
                                    .also { Log.d("FiltroDebug", "Després filtre edat, ${it.size} candidats") }
                                    .filter {
                                        if (usuariActual?.rol == "Estudiant" && it is PersonaGran) {
                                            if (distanciaMaxima > 0 && selectedLatLng != null && it.latitud != null && it.longitud != null) {
                                                val distancia = calcularDistancia(selectedLatLng.latitude, selectedLatLng.longitude, it.latitud, it.longitud)
                                                Log.d("FiltroDebug", "Filtre per distancia a ${it.nom}: $distancia km (max: $distanciaMaxima)")
                                                distancia <= distanciaMaxima
                                            } else {
                                                val resultat = ciutat.isNullOrBlank() || it.ubicacioAllotjament.equals(ciutat, ignoreCase = true)
                                                Log.d("FiltroDebug", "Filtre ciutat per ${it.nom}: $resultat")
                                                resultat
                                            }
                                        } else true
                                    }

                                    .filter {
                                        val selectedLat = selectedLatLng?.latitude
                                        val selectedLon = selectedLatLng?.longitude

                                        if (it is PersonaGran && selectedLat != null && selectedLon != null && it.latitud != null && it.longitud != null) {
                                            val distancia = calcularDistancia(selectedLat, selectedLon, it.latitud, it.longitud)
                                            Log.d("FiltroDebug", "Calculant distancia a ${it.nom}: $distancia km (max: $distanciaMaxima)")
                                            if (distanciaMaxima > 0) {
                                                distancia <= distanciaMaxima
                                            } else {
                                                true
                                            }
                                        } else {
                                            Log.d("FiltroDebug", "Saltant filtre distancia per ${it.nom} (coordenades faltants o no és PersonaGran)")
                                            true
                                        }
                                    }

                                Log.d("FiltroDebug", "Candidats després de tots els filtres inicials: ${candidats.size}")

                                val candidatsFiltrats = mutableListOf<UsuariBase>()
                                if (candidats.isEmpty()) {
                                    Log.d("FiltroDebug", "Cap candidat després de filtres → ERROR")
                                    _estat.update {
                                        it.copy(usuaris = emptyList(), error = "No hi ha usuaris disponibles amb aquests filtres")
                                    }
                                    return@addOnSuccessListener
                                }

                                var procesats = 0

                                candidats.forEach { usuari ->
                                    Log.d("FiltroDebug", "Consultant valoracions per usuari: ${usuari.id} (${usuari.nom})")
                                    valoracionsRef
                                        .whereEqualTo("idUsuariValorat", usuari.id)
                                        .get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val valDocs = task.result
                                                Log.d("FiltroDebug", "Valoracions trobades: ${valDocs.size()} per usuari ${usuari.nom}")

                                                val valoracions = valDocs.mapNotNull { it.toObject(Valoracio::class.java) }
                                                val mitjana = if (valoracions.isNotEmpty()) {
                                                    valoracions.map { it.puntuacio }.average()
                                                } else {
                                                    0.0
                                                }
                                                val numValoracions = valoracions.size

                                                Log.d("FiltroDebug", "Usuari ${usuari.nom}: mitjana=$mitjana, numValoracions=$numValoracions")

                                                if (mitjana >= numEstrellesMin && numValoracions >= numValoracionsMin) {
                                                    Log.d("FiltroDebug", "Usuari ${usuari.nom} passa el filtre de estrelles i valoracions")
                                                    candidatsFiltrats.add(usuari)
                                                } else {
                                                    Log.d("FiltroDebug", "Usuari ${usuari.nom} NO passa filtre de estrelles/valoracions")
                                                }
                                            } else {
                                                Log.e("FiltroDebug", "Error consultando valoraciones per ${usuari.nom}: ${task.exception}")
                                            }

                                            procesats++
                                            Log.d("FiltroDebug", "Processats: $procesats/${candidats.size}")
                                            if (procesats == candidats.size) {
                                                if (candidatsFiltrats.isNotEmpty()) {
                                                    val usuariAleatori = candidatsFiltrats.random()
                                                    Log.d("FiltroDebug", "Usuari seleccionat finalment: ${usuariAleatori.nom}")
                                                    _estat.update {
                                                        it.copy(
                                                            usuaris = listOf(usuariAleatori),
                                                            error = ""
                                                        )
                                                    }
                                                } else {
                                                    Log.d("FiltroDebug", "Cap usuari supera filtre final de estrelles i valoracions")
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
                } else {
                    Log.d("FiltroDebug", "Usuari actual NO trobat a la base de dades")
                    _estat.update {
                        it.copy(error = "Usuari actual no trobat")
                    }
                }
            }.addOnFailureListener {
                Log.e("FiltroDebug", "Error obtenint usuari actual: ${it.message}")
            }
        } ?: Log.d("FiltroDebug", "correuActual és null, no es pot obtenir usuari")
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = radioTierra * c
        Log.d("FiltroDebug", "calcularDistancia → ($lat1,$lon1) a ($lat2,$lon2): $distancia km")
        return distancia
    }
    data class Estat(
        val usuaris: List<UsuariBase> = listOf(),
        val error: String = ""
    )
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

    fun ferLike(callback: (Boolean) -> Unit) {
        val usuariMostrat = _estat.value.usuaris.firstOrNull()
        val actual = usuariActual

        if (usuariMostrat != null && actual != null) {
            val (estudiantId, personaGranId, esEstudiant) = if (actual.rol == "Estudiant") {
                Triple(actual.id, usuariMostrat.id, true)
            } else {
                Triple(usuariMostrat.id, actual.id, false)
            }

            val firestore = FirebaseFirestore.getInstance()
            val matchId = listOf(estudiantId, personaGranId).sorted().joinToString("_")
            val matchRef = firestore.collection("match").document(matchId)

            matchRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val actualitzacio = if (esEstudiant) {
                        mapOf("accepta_estudiant" to true)
                    } else {
                        mapOf("accepta_persona_gran" to true)
                    }

                    matchRef.update(actualitzacio).addOnSuccessListener {
                        matchRef.get().addOnSuccessListener { updatedDoc ->
                            val dades = updatedDoc.data
                            val accEst = dades?.get("accepta_estudiant") as? Boolean ?: false
                            val accGran = dades?.get("accepta_persona_gran") as? Boolean ?: false
                            val esMatch = accEst && accGran
                            if (esMatch) {
                                crearChat(estudiantId, personaGranId)
                            }
                            callback(esMatch)
                        }
                    }
                } else {
                    val nouMatch = hashMapOf(
                        "estudiantId" to estudiantId,
                        "personaGranId" to personaGranId,
                        "accepta_estudiant" to esEstudiant,
                        "accepta_persona_gran" to !esEstudiant
                    )
                    matchRef.set(nouMatch).addOnSuccessListener {
                        callback(false)
                    }
                }
            }
        }
    }
    fun obtenirTotsUsuarisPerNom(
        nom: String,
        onResult: (List<UsuariBase>) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val col = firestore.collection("Usuaris")
        val correuActual = FirebaseAuth.getInstance().currentUser?.email

        correuActual?.let { correu ->
            col.whereEqualTo("correu", correu).get().addOnSuccessListener { docs ->
                val actual = docs.firstOrNull()?.toObject(UsuariBase::class.java)
                if (actual != null) {
                    val rolOposat = if (actual.rol == "Estudiant") "Persona Gran" else "Estudiant"

                    col.whereEqualTo("rol", rolOposat).get().addOnSuccessListener { resultats ->
                        val usuaris = resultats.mapNotNull {
                            when (it.getString("rol")) {
                                "Estudiant" -> it.toObject(Estudiant::class.java)
                                "Persona Gran" -> it.toObject(PersonaGran::class.java)
                                else -> null
                            }
                        }.filter { usuari ->
                            usuari.correu != correu &&
                                    usuari.nom.contains(nom, ignoreCase = true)
                        }

                        onResult(usuaris)
                    }.addOnFailureListener {
                        onResult(emptyList()) // Error al obtener usuarios del rol opuesto
                    }
                } else {
                    onResult(emptyList()) // Usuario actual no encontrado
                }
            }.addOnFailureListener {
                onResult(emptyList()) // Error al obtener el usuario actual
            }
        } ?: onResult(emptyList()) // Correo actual null
    }
    fun obtenirUsuarisPerNom(nom: String) {
        obtenirTotsUsuarisPerNom(nom) { usuaris ->
            _estat.update { it.copy(usuaris = usuaris) }
        }
    }


    private fun crearChat(estudiantId: String, personaGranId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val matchId = listOf(estudiantId, personaGranId).sorted().joinToString("_")
        val chatsRef = firestore.collection("Chats")

        chatsRef.whereEqualTo("idMatch", matchId).get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                val docRef = chatsRef.document()
                val chatData = hashMapOf(
                    "idChat" to docRef.id,
                    "idMatch" to matchId,
                    "idEstudiant" to estudiantId,
                    "idPersonaGran" to personaGranId,
                    "lastMessage" to "",
                    "lastMessageSenderId" to "",
                    "timestamp" to Timestamp.now(),
                    "participants" to listOf(estudiantId, personaGranId)
                )
                docRef.set(chatData)
            } else {
                Log.d("CrearChat", "Ya existe un chat con el mismo idMatch: $matchId")
            }
        }
    }

}
