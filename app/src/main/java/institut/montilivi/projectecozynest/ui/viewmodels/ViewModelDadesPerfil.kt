package institut.montilivi.projectecozynest.ui.viewmodels
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ViewModelDadesPerfil : ViewModel() {
    private var _dada = MutableStateFlow<DadesEstat>(DadesEstat())
    val dada: StateFlow<DadesEstat> = _dada.asStateFlow()

    fun obtenUsuari(correu: String) {
        if (correu.isNullOrEmpty()) {
            Log.e("ViewModelDadesPerfil", "Error: correu és null o buit")
            _dada.value = DadesEstat(
                estaCarregant = false,
                esErroni = true,
                missatgeError = "No s'ha pogut obtenir el correu de l'usuari"
            )
            return
        }

        Log.d("ViewModelDadesPerfil", "Iniciant obtenció d'usuari amb correu: $correu")
        _dada.value = DadesEstat(estaCarregant = true)

        FirebaseFirestore.getInstance().collection("Usuaris")
            .whereEqualTo("correu", correu)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.e("ViewModelDadesPerfil", "No s'ha trobat cap document amb el correu: $correu")
                    _dada.value = DadesEstat(
                        estaCarregant = false,
                        esErroni = true,
                        missatgeError = "Usuari no trobat"
                    )
                } else {
                    val document = documents.firstOrNull()
                    if (document != null) {
                        Log.d("ViewModelDadesPerfil", "Document trobat: ${document.id}")
                        Log.d("ViewModelDadesPerfil", "Dades del document: ${document.data}")
                        val rol = document.getString("rol")
                        Log.d("ViewModelDadesPerfil", "Obtenint usuari amb rol: $rol")
                        val usuari = when (rol) {
                            "Estudiant" -> {
                                Log.d("ViewModelDadesPerfil", "Deserialitzant com a Estudiant")
                                document.toObject(Estudiant::class.java)
                            }
                            "Persona Gran" -> {
                                Log.d("ViewModelDadesPerfil", "Deserialitzant com a Persona Gran")
                                val personaGran = document.toObject(PersonaGran::class.java)
                                Log.d("ViewModelDadesPerfil", "PersonaGran deserialitzada: $personaGran")
                                personaGran
                            }
                            else -> {
                                Log.d("ViewModelDadesPerfil", "Deserialitzant com a UsuariBase")
                                document.toObject(UsuariBase::class.java)
                            }
                        }
                        
                        if (usuari != null) {
                            Log.d("ViewModelDadesPerfil", "Usuari deserialitzat correctament: ${usuari.id}, ${usuari.rol}")
                            if (usuari is PersonaGran) {
                                Log.d("ViewModelDadesPerfil", "Detalls PersonaGran: ubicacio=${usuari.ubicacioAllotjament}, preu=${usuari.preuAllotjament}")
                            }
                            _dada.value = DadesEstat(
                                estaCarregant = false,
                                dades = listOf(usuari),
                            )
                        } else {
                            Log.e("ViewModelDadesPerfil", "Error: usuari deserialitzat és null")
                            _dada.value = DadesEstat(
                                estaCarregant = false,
                                esErroni = true,
                                missatgeError = "Error al carregar les dades de l'usuari"
                            )
                        }
                    } else {
                        Log.e("ViewModelDadesPerfil", "Document és null malgrat que documents no està buit")
                        _dada.value = DadesEstat(
                            estaCarregant = false,
                            esErroni = true,
                            missatgeError = "Usuari no trobat"
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModelDadesPerfil", "Error obtenint usuari: ${e.message}")
                _dada.value = DadesEstat(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = "Error: ${e.message}"
                )
            }
    }
    data class DadesEstat(
        val estaCarregant: Boolean = true,
        val dades: List<UsuariBase> = listOf(),
        val esErroni: Boolean = false,
        val missatgeError: String = "",
        val correus: List<String> = listOf()
    )
}