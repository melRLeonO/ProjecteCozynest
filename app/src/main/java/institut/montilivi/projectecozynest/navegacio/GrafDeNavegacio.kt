package institut.montilivi.projectecozynest.navegacio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.ui.pantalles.PantallaConversa
import institut.montilivi.projectecozynest.ui.pantalles.PantallaDeRegistre
import institut.montilivi.projectecozynest.ui.pantalles.PantallaEditaDadesBasiques
import institut.montilivi.projectecozynest.ui.pantalles.PantallaEditaDadesRegistre
import institut.montilivi.projectecozynest.ui.pantalles.PantallaIniciDeSessio
import institut.montilivi.projectecozynest.ui.pantalles.PantallaMissatges
import institut.montilivi.projectecozynest.ui.pantalles.PantallaOpinionsValoracions
import institut.montilivi.projectecozynest.ui.pantalles.PantallaPerfil
import institut.montilivi.projectecozynest.ui.pantalles.PantallaPerfilChat
import institut.montilivi.projectecozynest.ui.pantalles.PantallaPrincipal
import institut.montilivi.projectecozynest.ui.pantalles.PantallaResetContra
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelDadesPerfil
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelEditaPerfil
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelValoracions

@Composable
fun GrafDeNavegacio(controladorDeNavegacio: NavHostController = rememberNavController()) {
    val manegadorAutentificacio = ManegadorAutentificacio(LocalContext.current)
    val manegadorFirestore = ManegadorFirestore()
    var haCompletatRegistre by remember { mutableStateOf(false) }
    var estaVerificant by remember { mutableStateOf(true) }

    val navegaAPortada = {
        controladorDeNavegacio.navigate(if (manegadorAutentificacio.hiHaUsuariIniciat() && haCompletatRegistre)
            DestinacioPantallaPrincipal
        else
            DestinacioIniciDeSessio)
    }

    val viewModel: ViewModelDadesPerfil = viewModel()
    val viewModelEditaDadesbasiques: ViewModelEditaPerfil = viewModel()
    val viewModelValoracions: ViewModelValoracions = viewModel()

    LaunchedEffect(manegadorAutentificacio.hiHaUsuariIniciat()) {
        if (manegadorAutentificacio.hiHaUsuariIniciat()) {
            val correu = manegadorAutentificacio.obtenUsuariActual()?.email
            if (correu != null) {
                haCompletatRegistre = manegadorFirestore.haCompletatRegistre(correu)
            }
        }
        estaVerificant = false
    }

    NavHost(
        navController = controladorDeNavegacio,
        startDestination = if (manegadorAutentificacio.hiHaUsuariIniciat() && haCompletatRegistre)
            DestinacioPantallaPrincipal
        else
            DestinacioIniciDeSessio
    ) {
        composable<DestinacioIniciDeSessio> {
            PantallaIniciDeSessio(manegadorAutentificacio, navegaAPortada, controladorDeNavegacio)
        }

        composable<DestinacioDeRegistre> {
            PantallaDeRegistre(manegadorAutentificacio, controladorDeNavegacio, manegadorFirestore)
        }

        composable<DestinacioEditaDades> {
            PantallaEditaDadesRegistre()
        }

        composable<DestinacioEditaDadesBasiques> {
            PantallaEditaDadesBasiques(controladorDeNavegacio, viewModelEditaDadesbasiques)
        }

        composable<DestinacioPerfil> {
            PantallaPerfil(controladorDeNavegacio, manegadorAutentificacio, navegaAPortada, viewModel)
        }
        composable<DestinacioPantallaPrincipal> {
            PantallaPrincipal(controladorDeNavegacio)
        }

        composable<DestinacioResetContra> {
            PantallaResetContra(manegadorAutentificacio, navegaAPortada, controladorDeNavegacio)
        }

        composable<DestinacioPantallaMissatges> {
            PantallaMissatges(controladorDeNavegacio)
        }

        composable<DestinacioOpinionsValoracions> {
            PantallaOpinionsValoracions(controladorDeNavegacio, viewModelValoracions, manegadorAutentificacio)
        }

        composable<DestinacioConversa> {
            PantallaConversa(controladorDeNavegacio)
        }

        composable<DestinacioPantallaPerfilChat> {
            PantallaPerfilChat(controladorDeNavegacio)
        }
    }
}