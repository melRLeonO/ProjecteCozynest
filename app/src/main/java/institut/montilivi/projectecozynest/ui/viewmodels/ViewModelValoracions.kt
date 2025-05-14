package institut.montilivi.projectecozynest.ui.viewmodels

import android.util.Log
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
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _estaCarregant = MutableStateFlow(false)
    val estaCarregant: StateFlow<Boolean> = _estaCarregant.asStateFlow()

    private val usuarisDAO = DAOFactory.obtenUsuarisDAO(null, DAOFactory.TipusBBDD.FIREBASE)
    private val valoracionsDAO = DAOFactory.obtenValoracionsDAO(null, DAOFactory.TipusBBDD.FIREBASE)

    init {
        obtenirUsuariActual()
    }

    private fun obtenirUsuariActual() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        Log.d("ViewModelValoracions", "Correu usuari autenticat: $currentUserEmail")

        if (currentUserEmail != null) {
            viewModelScope.launch {
                try {
                    val resultat = usuarisDAO.obtenUsuariPerCorreu(currentUserEmail)
                    when (resultat) {
                        is Resposta.Exit -> {
                            Log.d("ViewModelValoracions", "Usuari trobat: ${resultat.dades}")
                            _usuariActual.value = resultat.dades
                        }
                        is Resposta.Fracas -> {
                            Log.e("ViewModelValoracions", "Error obtenint usuari: ${resultat.missatgeError}")
                            _usuariActual.value = null
                            _error.value = resultat.missatgeError
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ViewModelValoracions", "Excepció obtenint usuari: ${e.message}")
                    _usuariActual.value = null
                    _error.value = e.message
                }
            }
        } else {
            Log.e("ViewModelValoracions", "Correu de l'usuari és null!")
        }
    }


    fun carregaValoracionsPerUsuari(idUsuariValorat: String) {
        viewModelScope.launch {
            try {
                valoracionsDAO.obtenValoracions().collect { resposta ->
                    when (resposta) {
                        is Resposta.Exit -> {
                            val valoracionsFiltrades = resposta.dades.filter { it.idUsuariValorat == idUsuariValorat }
                            _valoracionsUsuari.value = valoracionsFiltrades
                        }
                        is Resposta.Fracas -> {
                            _error.value = resposta.missatgeError
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
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
        Log.d("ViewModelValoracions", "Afegint valoració amb dades:")
        Log.d("ViewModelValoracions", "Usuari que valora: $idUsuariQueValora, $nomUsuariQueValora, $correuUsuariQueValora")
        Log.d("ViewModelValoracions", "Usuari valorat: $idUsuariValorat, puntuació: $puntuacio, comentari: $comentari")

        viewModelScope.launch {
            _estaCarregant.value = true
            _error.value = null
            
            val novaValoracio = Valoracio(
                idUsuariQueValora = idUsuariQueValora,
                nomUsuariQueValora = nomUsuariQueValora,
                correuUsuariQueValora = correuUsuariQueValora,
                idUsuariValorat = idUsuariValorat,
                puntuacio = puntuacio,
                comentari = comentari,
                dataValoracio = dataValoracio
            )

            try {
                val resposta = valoracionsDAO.afegeixValoracio(novaValoracio)
                
                when (resposta) {
                    is Resposta.Exit -> {
                        // Actualitzar la llista de valoracions
                        carregaValoracionsPerUsuari(idUsuariValorat)
                    }
                    is Resposta.Fracas -> {
                        _error.value = resposta.missatgeError
                        Log.e("ViewModelValoracions", "Error afegint valoració: ${resposta.missatgeError}")
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconegut"
                Log.e("ViewModelValoracions", "Error afegint valoració: ${e.message}")
            } finally {
                _estaCarregant.value = false
            }
        }
    }
}
