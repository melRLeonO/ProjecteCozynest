package institut.montilivi.projectecozynest.ui.pantalles

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.navegacio.DestinacioIniciDeSessio
import kotlinx.coroutines.launch

@Composable
fun PantallaResetContra(
    manegadorAutentificacio: ManegadorAutentificacio,
    navegaAInici: () -> Unit,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val ambitDeCorrutina = rememberCoroutineScope()
    val tag = "PANTALLA_RESET_CONTRA"

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
                    .height(150.dp)
                    .padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correu electrònic") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1)
                )
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    errorMessage = ""
                    Log.d(tag, "Botó de restabliment clicat amb correu: $email")
                    ambitDeCorrutina.launch {
                        if (email.isBlank()) {
                            errorMessage = "El correu electrònic no pot estar buit"
                            Log.e(tag, "Intent fallit: el correu està buit")
                        } else {
                            try {
                                Log.d(tag, "Intentant enviar correu de restabliment...")
                                val resposta = manegadorAutentificacio.enviaCorreuDeRestablimentContrasenya(email)
                                if (resposta is Resposta.Exit) {
                                    Log.d(tag, "Correu enviat correctament")
                                    navegaAInici()
                                } else if (resposta is Resposta.Fracas) {
                                    Log.e(tag, "Error de restabliment: ${resposta.missatgeError}")
                                    errorMessage = "Error en enviar el correu: ${resposta.missatgeError}"
                                }
                            } catch (e: Exception) {
                                Log.e(tag, "Excepció inesperada: ${e.message}")
                                errorMessage = "Error en enviar el correu: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF3B5998))
            ) {
                Text("Envia correu per canviar contrasenya", color = Color.White)
            }

            TextButton(
                onClick = {
                    Log.d(tag, "Navegant a pantalla d'inici de sessió")
                    navController.navigate(DestinacioIniciDeSessio)
                }
            ) {
                Text("Tornar a iniciar sessió", color = Color(0xFF3B5998))
            }
        }
    }
}
