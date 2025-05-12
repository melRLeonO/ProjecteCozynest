package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class ViewModelEditaPerfil : ViewModel() {
    private val _estat = MutableStateFlow(EditaDadesEstat())
    val estat: StateFlow<EditaDadesEstat> = _estat

    private val repositori = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    init {
        obtenirUsuariActual()
    }

    fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        Log.d("ViewModelEditaPerfil", "Obtenint usuari amb correu: $currentUserEmail")

        if (currentUserEmail == null) {
            Log.e("ViewModelEditaPerfil", "Error: correu és null")
            _estat.value = EditaDadesEstat(
                estaCarregant = false,
                esErroni = true,
                missatgeError = "No s'ha pogut obtenir el correu de l'usuari"
            )
            return
        }

        _estat.value = EditaDadesEstat(estaCarregant = true)

        FirebaseFirestore.getInstance().collection("Usuaris")
            .whereEqualTo("correu", currentUserEmail)
            .get()
            .addOnSuccessListener { documents ->
                val document = documents.firstOrNull()
                if (document != null) {
                    Log.d("ViewModelEditaPerfil", "Document trobat: ${document.id}")
                    Log.d("ViewModelEditaPerfil", "Dades del document: ${document.data}")
                    val rol = document.getString("rol")
                    Log.d("ViewModelEditaPerfil", "Obtenint usuari amb rol: $rol")
                    val usuari = when (rol) {
                        "Estudiant" -> {
                            Log.d("ViewModelEditaPerfil", "Deserialitzant com a Estudiant")
                            document.toObject(Estudiant::class.java)
                        }
                        "Persona Gran" -> {
                            Log.d("ViewModelEditaPerfil", "Deserialitzant com a Persona Gran")
                            val personaGran = document.toObject(PersonaGran::class.java)
                            Log.d("ViewModelEditaPerfil", "PersonaGran deserialitzada: $personaGran")
                            personaGran
                        }
                        else -> {
                            Log.d("ViewModelEditaPerfil", "Deserialitzant com a UsuariBase")
                            document.toObject(UsuariBase::class.java)
                        }
                    }
                    
                    if (usuari != null) {
                        Log.d("ViewModelEditaPerfil", "Usuari deserialitzat correctament: ${usuari.id}, ${usuari.rol}")
                        if (usuari is PersonaGran) {
                            Log.d("ViewModelEditaPerfil", "Detalls PersonaGran: ubicacio=${usuari.ubicacioAllotjament}, preu=${usuari.preuAllotjament}")
                        }
                        _estat.value = EditaDadesEstat(
                            estaCarregant = false,
                            dades = listOf(usuari)
                        )
                    } else {
                        Log.e("ViewModelEditaPerfil", "Error: usuari deserialitzat és null")
                        _estat.value = EditaDadesEstat(
                            estaCarregant = false,
                            esErroni = true,
                            missatgeError = "Error al carregar les dades de l'usuari"
                        )
                    }
                } else {
                    Log.e("ViewModelEditaPerfil", "Usuari no trobat amb correu: $currentUserEmail")
                    _estat.value = EditaDadesEstat(
                        estaCarregant = false,
                        esErroni = true,
                        missatgeError = "Usuari no trobat"
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModelEditaPerfil", "Error obtenint usuari: ${e.message}")
                _estat.value = EditaDadesEstat(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = "Error: ${e.message}"
                )
            }
    }

    fun modificarDadesUsuari(usuari: UsuariBase) {
        viewModelScope.launch {
            _estat.value = EditaDadesEstat(estaCarregant = true)
            val resposta = repositori.modificaUsuari(usuari)
            if (resposta is Resposta.Exit && resposta.dades) {
                _estat.value = EditaDadesEstat(
                    estaCarregant = false,
                    dades = listOf(usuari)
                )
            } else {
                _estat.value = EditaDadesEstat(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = "Error al modificar l'usuari"
                )
            }
        }
    }

    data class EditaDadesEstat(
        val estaCarregant: Boolean = true,
        val dades: List<UsuariBase> = listOf(),
        val esErroni: Boolean = false,
        val missatgeError: String = "",
        val correus: List<String> = listOf()
    )
}
