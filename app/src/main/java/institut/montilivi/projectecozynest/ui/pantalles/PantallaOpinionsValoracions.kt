package institut.montilivi.projectecozynest.ui.pantalles

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.model.UsuariActual
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelValoracions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaOpinionsValoracions(navController: NavController, viewModelValoracions: ViewModelValoracions,manegadorAutentificacio : ManegadorAutentificacio) {
    val usuari = UsuariActual.usuari
    var mostrarDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dataValoracio = dateFormat.format(Date())
    val valoracions by viewModelValoracions.valoracionsUsuari.collectAsState()
    val mitjanaPuntuacio = if (valoracions.isNotEmpty()) {
        valoracions.map { it.puntuacio }.average().roundToInt().toFloat()
    } else 0f
    val usuariActual by viewModelValoracions.usuariActual.collectAsState()
    val correuActual = manegadorAutentificacio.obtenUsuariActual()?.email
    val esAutovaloracio = correuActual == UsuariActual.usuari?.correu
    val error by viewModelValoracions.error.collectAsState()
    val estaCarregant by viewModelValoracions.estaCarregant.collectAsState()

    LaunchedEffect(usuari) {
        Log.d("PantallaOpinionsValoracions", "Usuari seleccionat: $usuari")
        usuari?.let {
            viewModelValoracions.carregaValoracionsPerUsuari(it.id)
        }
    }

    Scaffold(
        topBar = {
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tornar enrere",
                            tint = Color.White
                        )
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
        },
        bottomBar = {
            if (!esAutovaloracio) {
                Button(
                    onClick = { mostrarDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5472)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !estaCarregant
                ) {
                    Text("Deixa la teva ressenya", color = Color.White)
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF9F2F8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0))
                        ) {
                            Text(
                                text = error!!,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Red
                            )
                        }
                    }

                    if (estaCarregant) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(16.dp)
                        )
                    }

                    if (usuari != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${usuari.nom} ${usuari.cognoms}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                RatingStarsMitjana(puntuacioMitjana = mitjanaPuntuacio)
                                Text(
                                    text = "${valoracions.size} ressenyes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            AsyncImage(
                                model = usuari.fotoPerfil,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(valoracions) { valoracio ->
                                Card(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        RatingStars(puntuacio = valoracio.puntuacio)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Comentari: ${valoracio.comentari}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Data: ${valoracio.dataValoracio}", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("${valoracio.nomUsuariQueValora} (${valoracio.correuUsuariQueValora})", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No s'ha seleccionat cap usuari")
                    }
                }

                if (mostrarDialog) {
                    DialogDeRessenya(
                        onDismiss = { mostrarDialog = false },
                        onEnviar = { puntuacio, comentari ->
                            val usuariValorat = UsuariActual.usuari
                            val usuariActualLocal = usuariActual

                            if (usuariActualLocal != null && usuariValorat != null) {
                                Log.d("PantallaOpinionsValoracions", "Usuari actual: $usuariActualLocal")
                                Log.d("PantallaOpinionsValoracions", "Usuari valorat: $usuariValorat")

                                viewModelValoracions.afegeixValoracio(
                                    idUsuariQueValora = usuariActualLocal.id,
                                    nomUsuariQueValora = usuariActualLocal.nom,
                                    correuUsuariQueValora = usuariActualLocal.correu ?: "",
                                    idUsuariValorat = usuariValorat.id,
                                    puntuacio = puntuacio,
                                    comentari = comentari,
                                    dataValoracio = dataValoracio
                                )
                                mostrarDialog = false
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun DialogDeRessenya(
    onDismiss: () -> Unit,
    onEnviar: (Int, String) -> Unit
) {
    var puntuacio by remember { mutableStateOf(0) }
    var comentari by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Deixa la teva ressenya") },
        text = {
            Column {
                Text(text = "Selecciona una puntuació:")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { estrella ->
                        IconButton(
                            onClick = { puntuacio = estrella },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrella",
                                tint = if (estrella <= puntuacio) Color.Yellow else Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = comentari,
                    onValueChange = { comentari = it },
                    label = { Text("Comentari") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onEnviar(puntuacio, comentari)
                onDismiss()
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}
@Composable
fun RatingStars(puntuacio: Int) {
    Row {
        (1..5).forEach { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Estrella",
                tint = if (index <= puntuacio) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
fun RatingStarsMitjana(puntuacioMitjana: Float) {
    Row {
        for (i in 1..5) {
            val tint = when {
                i <= puntuacioMitjana -> Color(0xFFFFD700) // Estrella plena
                i - puntuacioMitjana < 1 -> Color(0x80FFD700) // Estrella parcial
                else -> Color.LightGray // Buida
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Estrella",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
