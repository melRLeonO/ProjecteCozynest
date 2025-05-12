package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.navegacio.DestinacioEditaDadesBasiques
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPrincipal
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelDadesPerfil
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.material.icons.filled.Male
import androidx.compose.ui.platform.LocalContext
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariActual
import institut.montilivi.projectecozynest.navegacio.DestinacioEditaDades
import institut.montilivi.projectecozynest.navegacio.DestinacioOpinionsValoracions
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaMissatges
import institut.montilivi.projectecozynest.navegacio.DestinacioPerfil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController,
                   manegadorAutentificacio: ManegadorAutentificacio,
                   navegaAInici: () -> Unit,
                   viewModel: ViewModelDadesPerfil)
{
    val coroutineScope = rememberCoroutineScope()
    val estat by viewModel.dada.collectAsState()
    val usuari = estat.dades.firstOrNull()
    val correuUsuari = manegadorAutentificacio.obtenUsuariActual()?.email
    val context = LocalContext.current

    LaunchedEffect(correuUsuari) {
        if (correuUsuari != null) {
            viewModel.obtenUsuari(correuUsuari)
        }
    }
    var mostrarFormulariCanviContrasenya by remember { mutableStateOf(false) }
    var novaContrasenya by remember { mutableStateOf("") }
    val correuActual = manegadorAutentificacio.obtenUsuariActual()?.email ?: ""
    var contrasenyaActual by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F2F8)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logotopaapbar),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu",
                        tint = Color.Transparent )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil",
                        tint = Color.Transparent )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de perfil
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (usuari != null && !usuari.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = usuari.fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        tint = Color(0xFF7E57C2)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (usuari != null) {
                        Text(
                            text = "${usuari.nom} ${usuari.cognoms}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF8A73BE) // o un altre color que t’agradi
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(usuari.dataNaixement, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))

                            val (iconGenere, colorGenere) = when (usuari.genere) {
                                "Dona" -> Icons.Default.Female to Color(0xFFE91E63) // Rosa
                                "Home" -> Icons.Default.Male to Color(0xFF42A5F5)   // Blau
                                else -> Icons.Default.Person to Color.Gray          // Per defecte
                            }

                            Icon(
                                imageVector = iconGenere,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colorGenere
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(usuari.genere, fontSize = 14.sp)
                        }

                    }
                    else if (estat.estaCarregant) {
                        CircularProgressIndicator()
                    } else if (estat.esErroni) {
                        Text("Error carregant usuari: ${estat.missatgeError}")
                    }
                }

                IconButton(onClick = {

                    navController.navigate(DestinacioEditaDadesBasiques)
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate(DestinacioEditaDades) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5472)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Edita dades registre", color = Color.White)
        }

        Button(
            onClick = {
                val usuariActual = manegadorAutentificacio.obtenUsuariActual()
                if (usuariActual != null && usuari != null) {
                    UsuariActual.usuari = usuari // estableix l’usuari del perfil com l’usuari recomanat
                    navController.navigate(DestinacioOpinionsValoracions)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5472)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Veure opinions i valoracions", color = Color.White)
        }

        Button(
            onClick = { mostrarFormulariCanviContrasenya = !mostrarFormulariCanviContrasenya },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5472)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Canviar contrasenya", color = Color.White)
        }
        if (mostrarFormulariCanviContrasenya) {
            val correuFix = manegadorAutentificacio.obtenUsuariActual()?.email ?: ""

            OutlinedTextField(
                value = correuFix,
                onValueChange = {},
                readOnly = true,
                label = { Text("Correu") }
            )

            OutlinedTextField(
                value = contrasenyaActual,
                onValueChange = { contrasenyaActual = it },
                label = { Text("Contrasenya actual") },
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                value = novaContrasenya,
                onValueChange = { novaContrasenya = it },
                label = { Text("Nova contrasenya") },
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val reautenticat = manegadorAutentificacio.reautenticaUsuari(correuActual, contrasenyaActual)
                            if (reautenticat is Resposta.Exit) {
                                val respostaContrasenya = manegadorAutentificacio.actualitzaContrasenya(novaContrasenya)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Contrasenya actualitzada correctament", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    val missatge = if (reautenticat is Resposta.Fracas && reautenticat.missatgeError == "CONTRASENYA_INCORRECTA") {
                                        "La contrasenya actual és incorrecta"
                                    } else if (reautenticat is Resposta.Fracas) {
                                        "Reautenticació fallida: ${reautenticat.missatgeError}"
                                    } else {
                                        "Reautenticació fallida"
                                    }

                                    Toast.makeText(context, missatge, Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    }
                }
            ) {
                Text("Actualitzar contrasenya")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    manegadorAutentificacio.tancaSessio()
                    navegaAInici()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tancar sessió", color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationBar(containerColor = Color(0xFFF5EEF5)) {
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate(DestinacioPantallaMissatges)},
                icon = { Icon(Icons.Default.Mail, contentDescription = "Missatges") },
                label = { Text("Missatges") }
            )
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate(DestinacioPantallaPrincipal)
                },
                icon = { Icon(Icons.Default.Groups, contentDescription = "Home") },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = true,
                onClick = { navController.navigate(DestinacioPerfil)},
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                label = { Text("Perfil") }
            )
        }
    }
}
