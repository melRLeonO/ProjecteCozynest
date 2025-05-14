package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariActual
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.navegacio.DestinacioDeRegistre
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPrincipal
import institut.montilivi.projectecozynest.navegacio.DestinacioResetContra
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

@Composable
fun PantallaIniciDeSessio(
    manegadorAutentificacio: ManegadorAutentificacio,
    navegaAInici: () -> Unit,
    navController: NavController,
) {
    val ambitDeCorrutina = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF5F8))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logologocozy),
                contentDescription = "COZYNEST",
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = ""
                },
                label = { Text("correu electrònic") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1)
                ),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = "" // Clear error when user types
                },
                label = { Text("contrasenya") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1)
                ),
                enabled = !isLoading
            )

            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    ambitDeCorrutina.launch {
                        try {
                            isLoading = true
                            errorMessage = ""
                            
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "El correu i la contrasenya no poden estar buits"
                                return@launch
                            }

                            when (val resultat = manegadorAutentificacio.iniciarSessioAmbCorreuIContrasenya(email, password)) {
                                is Resposta.Exit -> {
                                    val firestore = FirebaseFirestore.getInstance()
                                    val userDoc = firestore.collection("Usuaris")
                                        .whereEqualTo("correu", email)
                                        .get()
                                        .await()
                                    
                                    if (userDoc.isEmpty) {
                                        errorMessage = "Usuari no trobat a la base de dades"
                                        manegadorAutentificacio.tancaSessio()
                                    } else {
                                        val document = userDoc.documents.first()
                                        val rol = document.getString("rol")
                                        val usuari = when (rol) {
                                            "Estudiant" -> document.toObject(Estudiant::class.java)
                                            "Persona Gran" -> document.toObject(PersonaGran::class.java)
                                            else -> document.toObject(UsuariBase::class.java)
                                        }
                                        if (usuari != null) {
                                            UsuariActual.usuari = usuari
                                            Log.d("UsuariActual", "Usuari establert després d'iniciar sessió: ${usuari.rol}")
                                             navController.navigate(DestinacioPantallaPrincipal) {
                                                popUpTo(0)
                                            }
                                        } else {
                                            errorMessage = "Error al carregar les dades de l'usuari"
                                            manegadorAutentificacio.tancaSessio()
                                        }
                                    }
                                }
                                is Resposta.Fracas -> errorMessage = resultat.missatgeError
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error inesperat: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF3B5998)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text("Iniciar sessió", color = Color.White)
                }
            }

            TextButton(
                onClick = { navController.navigate(DestinacioDeRegistre) },
                modifier = Modifier.padding(bottom = 8.dp),
                enabled = !isLoading
            ) {
                Text("Registrar-se", color = Color(0xFF3B5998))
            }

            TextButton(
                onClick = { navController.navigate(DestinacioResetContra) },
                enabled = !isLoading
            ) {
                Text("Has oblidat la teva contrasenya?", color = Color.Gray)
            }
        }
    }
}
