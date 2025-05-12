package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import institut.montilivi.projectecozynest.model.ContracteDigital
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelContracteDigital
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelEditaPerfil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaContracteDigital(
    navController: NavController,
    viewModel: ViewModelContracteDigital
) {
    var horesAssistencia by remember { mutableStateOf("") }
    var horaris by remember { mutableStateOf("") }
    var normes by remember { mutableStateOf("") }
    var duradaEstada by remember { mutableStateOf("") }
    var preuAllotjament by remember { mutableStateOf("") }

    val estat by viewModel.estat.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contracte digital") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Enrere")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = horesAssistencia,
                onValueChange = { horesAssistencia = it },
                label = { Text("Hores de companyia o assistència setmanal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Regles bàsiques de la llar", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = horaris,
                onValueChange = { horaris = it },
                label = { Text("Horaris de convivència") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = normes,
                onValueChange = { normes = it },
                label = { Text("Normes de convivència") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = duradaEstada,
                onValueChange = { duradaEstada = it },
                label = { Text("Expectatives sobre la durada mínima de l’estada") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = preuAllotjament,
                onValueChange = { preuAllotjament = it },
                label = { Text("Preu de l’allotjament") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val contracte = ContracteDigital(
                        horesAsistenciaSetmanal = horesAssistencia,
                        horarisConvivencia = horaris,
                        normesDeConvivencia = normes,
                        duracioMinima = duradaEstada,
                        preuAllotjament = preuAllotjament.toIntOrNull() ?: 0,
                        )
                    viewModel.afegeixContracte(contracte)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
            ) {
                Text("Generar i sol·licita Contracte", color = Color.White)
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (estat.estaCarregant) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (estat.esErroni) {
                Text(
                    text = estat.missatgeError,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (estat.contracte != null) {
                Text(
                    text = "Contracte afegit correctament!",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

