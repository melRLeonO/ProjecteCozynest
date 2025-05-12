package institut.montilivi.projectecozynest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import institut.montilivi.projectecozynest.dades.DAOFactory
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.model.Valoracio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewModelValoracions : ViewModel() {
    private val _usuariActual = MutableStateFlow<UsuariBase?>(null)
    val usuariActual: StateFlow<UsuariBase?> = _usuariActual.asStateFlow()
    private val _valoracionsUsuari = MutableStateFlow<List<Valoracio>>(emptyList())
    val valoracionsUsuari: StateFlow<List<Valoracio>> = _valoracionsUsuari

    init {
        obtenirUsuariActual()
    }

    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            FirebaseFirestore.getInstance().collection("Usuaris")
                .whereEqualTo("correu", currentUserEmail)
                .get()
                .addOnSuccessListener { documents ->
                    _usuariActual.value = documents.firstOrNull()?.toObject(UsuariBase::class.java)
                }
                .addOnFailureListener {
                    _usuariActual.value = null
                }
        }
    }

    fun carregaValoracionsPerUsuari(idUsuariValorat: String) {
        viewModelScope.launch {
            DAOFactory.obtenValoracionsDAO(null, DAOFactory.TipusBBDD.FIREBASE)
                .obtenValoracions()
                .collect { resposta ->
                    if (resposta is Resposta.Exit) {
                        val valoracionsFiltrades = resposta.dades.filter { it.idUsuariValorat == idUsuariValorat }
                        _valoracionsUsuari.value = valoracionsFiltrades
                    }
                }
        }
    }

    fun afegeixValoracio(
        idUsuariQueValora: String,
        nomUsuariQueValora: String,
        correuUsuariQueValora: String,
        idUsuariValorat: String,
        puntuacio: Int,
        comentari: String,
        dataValoracio: String
    ) {
        viewModelScope.launch {
            val novaValoracio = Valoracio(
                idUsuariQueValora = idUsuariQueValora,
                nomUsuariQueValora = nomUsuariQueValora,
                correuUsuariQueValora = correuUsuariQueValora,
                idUsuariValorat = idUsuariValorat,
                puntuacio = puntuacio,
                comentari = comentari,
                dataValoracio = dataValoracio
            )

            Firebase.firestore.collection("Valoracions")
                .add(novaValoracio)
                .addOnSuccessListener { documentReference ->
                    documentReference.update("idValoracio", documentReference.id)
                }
                .addOnFailureListener {
                    // Manejar errores
                }
        }
    }
}
