package institut.montilivi.projectecozynest.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.Resposta

class ViewModelDadesPerfil : ViewModel() {
    private var _dada = MutableStateFlow<DadesEstat>(DadesEstat())
    val dada: StateFlow<DadesEstat> = _dada.asStateFlow()
    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    fun obtenUsuari(correu: String) {
        if (correu.isNullOrEmpty()) {
            _dada.value = DadesEstat(
                estaCarregant = false,
                esErroni = true,
                missatgeError = "No s'ha pogut obtenir el correu de l'usuari"
            )
            return
        }
        _dada.value = DadesEstat(estaCarregant = true)

        viewModelScope.launch {
            try {
                val resultat = usuarisDAO.obtenUsuariPerCorreu(correu)
                when (resultat) {
                    is Resposta.Exit -> {
                        _dada.value = DadesEstat(
                            estaCarregant = false,
                            dades = listOf(resultat.dades)
                        )
                    }
                    is Resposta.Fracas -> {
                        _dada.value = DadesEstat(
                            estaCarregant = false,
                            esErroni = true,
                            missatgeError = resultat.missatgeError
                        )
                    }
                }
            } catch (e: Exception) {
                _dada.value = DadesEstat(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = "Error: ${e.message}"
                )
            }
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