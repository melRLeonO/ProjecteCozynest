package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelChats
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import institut.montilivi.projectecozynest.model.Chat
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.model.ChatSeleccionat
import institut.montilivi.projectecozynest.navegacio.DestinacioConversa
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaMissatges
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPrincipal
import institut.montilivi.projectecozynest.navegacio.DestinacioPerfil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMissatges(navController: NavController) {
    val viewModel: ViewModelChats = viewModel()
    val llistaChats by viewModel.chats.collectAsState()
    val filtreNom = remember { mutableStateOf("") }

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
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.Transparent
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = Color.Transparent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
            )

        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF5EEF5)) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate(DestinacioPantallaMissatges) },
                    icon = { Icon(Icons.Default.Mail, contentDescription = "Missatges") },
                    label = { Text("Missatges") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(DestinacioPantallaPrincipal) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(DestinacioPerfil) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        containerColor = Color(0xFFF9F2F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = filtreNom.value,
                onValueChange = { filtreNom.value = it },
                label = { Text("Filtrar per nom") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
            )
            val llistaFiltrada = llistaChats.filter { chat ->
                val idAltUsuari = if (viewModel.rolActual == "Persona Gran") {
                    chat.idEstudiant
                } else {
                    chat.idPersonaGran
                }
                val nomAltUsuari = runBlocking {
                    viewModel.obtenirNomUsuariPerId(idAltUsuari)
                }

                nomAltUsuari.contains(filtreNom.value, ignoreCase = true)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(llistaFiltrada) { chat ->
                    ChatItem(chat = chat, viewModel = viewModel) { idChat ->
                        ChatSeleccionat.idChatSeleccionat = chat.idChat
                        navController.navigate(DestinacioConversa)
                    }
                }
            }
        }
    }
}


@Composable
fun ChatItem(chat: Chat, viewModel: ViewModelChats, onClick: (String) -> Unit) {
    val nomAltUsuari = remember { mutableStateOf("Carregant...") }
    val fotoPerfilAltUsuari = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val esMissatgeNou = remember { mutableStateOf(false) }

    LaunchedEffect(chat) {
        coroutineScope.launch {
            val idAltUsuari = if (viewModel.rolActual == "Persona Gran") {
                chat.idEstudiant
            } else {
                chat.idPersonaGran
            }
            nomAltUsuari.value = viewModel.obtenirNomUsuariPerId(idAltUsuari)
            fotoPerfilAltUsuari.value = viewModel.obtenirFotoUsuariPerId(idAltUsuari)

            esMissatgeNou.value = chat.lastMessageSenderId != null &&
                    chat.lastMessageSenderId != viewModel.usuariIdActual
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(chat.idChat) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (fotoPerfilAltUsuari.value.isNotEmpty()) {
                AsyncImage(
                    model = fotoPerfilAltUsuari.value,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil per defecte",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nomAltUsuari.value,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 👇 Mostrar "Nou match" si no hay mensaje y es una conversación nueva
                    if (chat.lastMessage.isEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nou match",
                            color = Color(0xFF4A5A7D),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (esMissatgeNou.value) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nou missatge",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (chat.lastMessage.isNotEmpty()) chat.lastMessage else "Inicia la conversa",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTimestampSmart(chat.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


fun formatTimestampSmart(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val calMessage = Calendar.getInstance().apply { time = date }
    val calNow = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"))

    return if (calMessage.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
        && calMessage.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)) {
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        hourFormat.timeZone = TimeZone.getTimeZone("Europe/Madrid")
        hourFormat.format(date)
    } else {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Madrid")
        dateFormat.format(date)
    }
}


