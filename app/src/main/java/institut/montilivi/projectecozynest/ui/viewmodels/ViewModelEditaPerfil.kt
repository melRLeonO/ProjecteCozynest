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

class ViewModelEditaPerfil : ViewModel() {
    private val _estat = MutableStateFlow(EditaDadesEstat())
    val estat: StateFlow<EditaDadesEstat> = _estat

    private val repositori = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    init {
        obtenirUsuariActual()
    }

    fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail == null) {
            _estat.value = EditaDadesEstat(
                estaCarregant = false,
                esErroni = true,
                missatgeError = "No s'ha pogut obtenir el correu de l'usuari"
            )
            return
        }

        _estat.value = EditaDadesEstat(estaCarregant = true)

        viewModelScope.launch {
            try {
                val resultat = repositori.obtenUsuariPerCorreu(currentUserEmail)
                when (resultat) {
                    is Resposta.Exit -> {
                        _estat.value = EditaDadesEstat(
                            estaCarregant = false,
                            dades = listOf(resultat.dades)
                        )
                    }
                    is Resposta.Fracas -> {
                        _estat.value = EditaDadesEstat(
                            estaCarregant = false,
                            esErroni = true,
                            missatgeError = resultat.missatgeError
                        )
                    }
                }
            } catch (e: Exception) {
                _estat.value = EditaDadesEstat(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = "Error: ${e.message}"
                )
            }
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
