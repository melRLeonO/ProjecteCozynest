package institut.montilivi.projectecozynest.ui.pantalles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import institut.montilivi.projectecozynest.model.UsuariBase
import coil.compose.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelEditaPerfil

@Composable
fun PantallaEditaDadesBasiques(
    navController: NavController,
    viewModel: ViewModelEditaPerfil
) {
    LaunchedEffect(Unit) {
        viewModel.obtenirUsuariActual()
    }

    val estat by viewModel.estat.collectAsState()
    val usuari = estat.dades.firstOrNull()

    var nom by remember { mutableStateOf(usuari?.nom ?: "") }
    var cognoms by remember { mutableStateOf(usuari?.cognoms ?: "") }
    var dataNaixement by remember { mutableStateOf(usuari?.dataNaixement ?: "") }
    var genere by remember { mutableStateOf(usuari?.genere ?: "") }
    var fotoPerfil by remember { mutableStateOf(usuari?.fotoPerfil ?: "") }
    LaunchedEffect(usuari) {
        nom = usuari?.nom ?: ""
        cognoms = usuari?.cognoms ?: ""
        dataNaixement = usuari?.dataNaixement ?: ""
        genere = usuari?.genere ?: ""
        fotoPerfil = usuari?.fotoPerfil ?: ""
    }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            uploadImageToFirebase(context, it) { url ->
                fotoPerfil = url
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta de perfil editable
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Foto de perfil:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (fotoPerfil.isNotEmpty()) {
                    AsyncImage(
                        model = fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        tint = Color(0xFF7E57C2)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Canviar foto")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modificar nombre
                TextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Modificar apellidos
                TextField(
                    value = cognoms,
                    onValueChange = { cognoms = it },
                    label = { Text("Cognoms") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Modificar fecha de nacimiento
                TextField(
                    value = dataNaixement,
                    onValueChange = { dataNaixement = it },
                    label = { Text("Data Naixement") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Modificar género
                Text(
                    text = "Gènere:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { genere = "Home" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (genere == "Home") Color(0xFF7E57C2) else Color.LightGray
                        )
                    ) {
                        Text("Home")
                    }

                    Button(
                        onClick = { genere = "Dona" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (genere == "Dona") Color(0xFF7E57C2) else Color.LightGray
                        )
                    ) {
                        Text("Dona")
                    }
                }

                // Botón para guardar los cambios
                Button(
                    onClick = {
                        val usuariModificat = UsuariBase(
                            id = usuari?.id ?: "",
                            correu = usuari?.correu ?: "",
                            nom = nom,
                            cognoms = cognoms,
                            dataNaixement = dataNaixement,
                            genere = genere,
                            fotoPerfil = fotoPerfil,
                            rol = usuari?.rol ?: ""
                        )
                        viewModel.modificarDadesUsuari(usuariModificat)
                        navController.popBackStack()  // Regresar a la pantalla anterior
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
