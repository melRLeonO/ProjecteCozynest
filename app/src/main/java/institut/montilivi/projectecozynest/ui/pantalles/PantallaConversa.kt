package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import institut.montilivi.projectecozynest.model.ChatSeleccionat
import institut.montilivi.projectecozynest.model.Missatge
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelConversa
import android.util.Log
import androidx.compose.foundation.clickable
import institut.montilivi.projectecozynest.model.UsuariActual
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPerfilChat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConversa(navController: NavController) {
    val viewModel: ViewModelConversa = viewModel()
    val missatges by viewModel.missatges.collectAsState()
    val usuariActualId = viewModel.usuariActual.collectAsState().value?.id ?: ""
    val altreUsuari by viewModel.altreUsuari.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(ChatSeleccionat.idChatSeleccionat, usuariActualId) {
        if (ChatSeleccionat.idChatSeleccionat.isNotEmpty() && usuariActualId.isNotEmpty()) {
            viewModel.carregarChat(ChatSeleccionat.idChatSeleccionat) { chat ->
                chat?.let {
                    val altreId = when (usuariActualId) {
                        it.idEstudiant -> it.idPersonaGran
                        it.idPersonaGran -> it.idEstudiant
                        else -> null
                    }
                    if (!altreId.isNullOrEmpty()) {
                        viewModel.carregarAltreUsuari(altreId)
                    }
                }
            }
            Log.d("DEBUG", "Carregant missatges per al chat: ${ChatSeleccionat.idChatSeleccionat}")
            viewModel.carregarMissatges(ChatSeleccionat.idChatSeleccionat)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F2F8)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!altreUsuari?.fotoPerfil.isNullOrEmpty()) {
                        AsyncImage(
                            model = altreUsuari!!.fotoPerfil,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    altreUsuari?.let {
                                        UsuariActual.usuari = it
                                        navController.navigate(DestinacioPantallaPerfilChat)
                                    }
                                }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Icono de perfil por defecto",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    altreUsuari?.let {
                                        UsuariActual.usuari = it
                                        navController.navigate(DestinacioPantallaPerfilChat)
                                    }
                                },
                            tint = Color(0xFFFFFFFF)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = altreUsuari?.nom ?: "carregant...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable {
                            altreUsuari?.let {
                                UsuariActual.usuari = it
                                navController.navigate(DestinacioPantallaPerfilChat)
                            }
                        }
                    )

                }

            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Tornar enrere", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
        )


        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(missatges) { missatge ->
                MissatgeItem(missatge, usuariActualId)
            }
        }


        var text by remember { mutableStateOf("") }

        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )

            IconButton(onClick = {
                viewModel.enviarMissatge(text)
                text = ""
            }) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}
@Composable
fun MissatgeItem(missatge: Missatge, usuariActualId: String) {
    val isUser = missatge.remitent == usuariActualId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFFDCF8C6) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = missatge.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}
