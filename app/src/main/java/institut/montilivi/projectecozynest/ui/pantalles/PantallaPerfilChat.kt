package institut.montilivi.projectecozynest.ui.pantalles

import androidx.compose.runtime.Composable
import institut.montilivi.projectecozynest.model.UsuariActual
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaMissatges
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelPerfilChat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilChat(navController: NavController) {
    val usuari = UsuariActual.usuari
    val viewModel: ViewModelPerfilChat = viewModel()
    val estaBloquejat by viewModel.estaBloquejat.collectAsState()

    // Comprobar si el usuario está bloqueado cuando se carga la pantalla
    LaunchedEffect(usuari?.id) {
        usuari?.id?.let { viewModel.comprovaBloqueig(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tornar enrere",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            usuari?.let { u ->
                if (!u.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = u.fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Icono de perfil per defecte",
                        modifier = Modifier.size(160.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = u.nom,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        if (!estaBloquejat) {
                            viewModel.bloquejaUsuari(u.id)
                                navController.navigate(DestinacioPantallaMissatges)

                        }
                    },
                    enabled = !estaBloquejat
                ) {
                    Text(if (estaBloquejat) "Bloquejat" else "Bloquejar")
                }

            } ?: run {
                Text(text = "Usuari desconegut", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
