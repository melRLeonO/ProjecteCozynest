package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.ContracteDigital
import institut.montilivi.projectecozynest.model.Resposta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewModelContracteDigital : ViewModel() {

    private val _estat = MutableStateFlow(EstatContracteDigital())
    val estat: StateFlow<EstatContracteDigital> = _estat

    private val repositori = DAOFactory.obtenContractesDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    fun afegeixContracte(contracte: ContracteDigital) {
        viewModelScope.launch {
            _estat.value = EstatContracteDigital(estaCarregant = true)

            val resposta = repositori.afegeixContracte(contracte)

            if (resposta is Resposta.Exit && resposta.dades) {
                _estat.value = EstatContracteDigital(
                    estaCarregant = false,
                    contracte = contracte
                )
            } else {
                _estat.value = EstatContracteDigital(
                    estaCarregant = false,
                    esErroni = true,
                    missatgeError = (resposta as? Resposta.Fracas)?.missatgeError ?: "Error desconegut"
                )
            }
        }
    }

    data class EstatContracteDigital(
        val estaCarregant: Boolean = false,
        val contracte: ContracteDigital? = null,
        val esErroni: Boolean = false,
        val missatgeError: String = ""
    )
}