package institut.montilivi.projectecozynest.ui.pantalles

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.autentificacio.ManegadorAutentificacio
import institut.montilivi.projectecozynest.dades.UsuarisDAOFirebaseImpl
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPrincipal
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelRegistre
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDeRegistre(
    manegadorAutentificacio: ManegadorAutentificacio,
    navController: NavController,
    manegadorFirestore : ManegadorFirestore,
    viewModel: ViewModelRegistre = viewModel()
) {
    val step = viewModel.step
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            uploadImageToFirebase(context, it) { url ->
                viewModel.downloadUrl.value = url
            }
        }
    }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val totalSteps = when (viewModel.rol.value) {
        "Persona Gran" -> 8
        else -> 9
    }

    var allotjamentImageUri by remember { mutableStateOf<Uri?>(null) }
    val allotjamentImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            allotjamentImageUri = it
            uploadImageToFirebase(context, it) { url ->
                viewModel.allotjamentDownloadUrl.value = url
            }
        }
    }
    val ambitDeCorrutina = rememberCoroutineScope()
    var ubicacioAllotjament by remember { mutableStateOf("") }
    var preu by remember { mutableStateOf(0) }
    var superficie by remember { mutableStateOf("") }
    var descripcio by remember { mutableStateOf("") }
    val diesSetmana = listOf("Dilluns", "Dimarts", "Dimecres", "Dijous", "Divendres", "Dissabte", "Diumenge")
    val habilitatsSeleccionades = viewModel.habilitatsLlar.filter { it.value.value }.map { it.key }
    val preferenciesEstudiant = viewModel.preferenciesConvivenciaEstudiant.filter { it.value.value }.map { it.key }
    val preferenciesGran = viewModel.preferenciesConvivencia.filter { it.value.value }.map { it.key }
    val ajudesSeleccionades = viewModel.tipusAjudaSolicitada.filter { it.value.value }.map { it.key }

    BackHandler {
        if (step.value == 1) {
            // Solo si cancela en el primer paso, borra el usuario
            FirebaseAuth.getInstance().currentUser?.delete()
            navController.popBackStack()
        } else {
            // Si está en otro paso, solo retrocede de paso
            step.value = step.value - 1
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = {
                    if (step.value == 1) {
                        FirebaseAuth.getInstance().currentUser?.delete()
                        navController.popBackStack()
                    } else {
                        step.value = step.value - 1
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Tornar enrere")
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
        ) {
            when (step.value) {
                1 -> {
                    OutlinedTextField(
                        value = viewModel.email.value,
                        onValueChange = {
                            viewModel.email.value = it
                        },
                        label = { Text("Correu electrònic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.contrasenya.value,
                        onValueChange = {
                            viewModel.contrasenya.value = it
                        },
                        label = { Text("Contrasenya") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )

                }

                2 -> {
                    Text(
                        text = "El meu nom és:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )

                    OutlinedTextField(
                        value = viewModel.nom.value,
                        onValueChange = {
                            viewModel.nom.value = it
                        },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "El meu cognom és:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )

                    OutlinedTextField(
                        value = viewModel.cognom.value,
                        onValueChange = {
                            viewModel.cognom.value = it
                        },
                        label = { Text("Cognom") },
                        modifier = Modifier.fillMaxWidth()
                    )

                }

                3 -> {
                    Text(
                        text = "El meu aniversari és:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )
                    OutlinedTextField(
                        value = viewModel.dataNaixement.value,
                        onValueChange = {
                            viewModel.dataNaixement.value = it
                        },
                        label = { Text("DD/MM/YYYY") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                4 -> {
                    Text(
                        text = "Sóc:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectedGender.value = "Dona"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.selectedGender.value == "Dona") Color.Blue else Color.Gray
                            )
                        ) {
                            Text("Dona")
                        }

                        Button(
                            onClick = {
                                viewModel.selectedGender.value = "Home"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.selectedGender.value == "Home") Color.Blue else Color.Gray
                            )
                        ) {
                            Text("Home")
                        }
                    }
                }

                5 -> {
                    Text(
                        text = "Afegeix la teva foto de perfil:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = { launcher.launch("image/*") }) {
                        Text("Selecciona una imatge")
                    }

                    viewModel.downloadUrl.value?.let { url ->
                        Spacer(modifier = Modifier.height(16.dp))
                        AsyncImage(
                            model = url,
                            contentDescription = "Foto de perfil (Firebase)",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                6 -> {
                    Text(
                        text = "Sóc:",
                        color = Color(0xFF4C5D82),
                        fontFamily = FontFamily(Font(R.font.cardoregular)),
                        fontSize = 30.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.rol.value = "Estudiant"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.rol.value == "Estudiant") Color.Blue else Color.Gray
                            )
                        ) {
                            Text("Estudiant")
                        }

                        Button(
                            onClick = {
                                viewModel.rol.value = "Persona Gran"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.rol.value == "Persona Gran") Color.Blue else Color.Gray
                            )
                        ) {
                            Text("Persona Gran")
                        }
                    }
                }

                7 -> {
                    if (viewModel.rol.value == "Persona Gran") {
                        Text(
                            text = "Mostra el teu allotjament",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { allotjamentImageLauncher.launch("image/*") }) {
                            Text("Selecciona imatge d'allotjament")
                        }

                        viewModel.allotjamentDownloadUrl.value?.let { url ->
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = url,
                                contentDescription = "Imatge allotjament",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = superficie,
                            onValueChange = {
                                superficie = it
                                viewModel.superficieAllotjament.value = it
                            },
                            label = { Text("Superfície") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = descripcio,
                            onValueChange = {
                                descripcio = it
                                viewModel.descripcioAllotjament.value = it
                            },
                            label = { Text("Descripció") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Preu seleccionat: $preu €",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF425270),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Slider(
                            value = preu.toFloat(),
                            onValueChange = {
                                preu = it.toInt()
                                viewModel.preuAllotjament.value = it.toInt()
                            },
                            valueRange = 0f..1000f,
                            steps = 1000,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        UbicacioAllotjament { address, latLng ->
                            ubicacioAllotjament = address
                            selectedLatLng = latLng
                            viewModel.ubicacioAllotjament.value = address
                        }
                    }
                    else if (viewModel.rol.value == "Estudiant") {
                        Text(
                            text = "Horaris de classe i disponibilitat",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quins dies a la setmana tens disponibilitat?",
                            fontSize = 16.sp,
                            color = Color(0xFF6E7C9A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF425270))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        "Dia",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        "Matins",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "Tardes",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "Nits",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                diesSetmana.forEach { dia ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dia,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(2f)
                                        )

                                        for (i in 0..2) {
                                            Checkbox(
                                                checked = viewModel.disponibilitat[dia]?.value?.get(i) ?: false,
                                                onCheckedChange = { isChecked ->
                                                    viewModel.disponibilitat[dia]?.value =
                                                        viewModel.disponibilitat[dia]?.value?.copyOf()?.apply {
                                                            this[i] = isChecked
                                                        } ?: BooleanArray(3) { false }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFFFF66B2),
                                                    uncheckedColor = Color.White
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                8 -> {
                    if (viewModel.rol.value == "Persona Gran") {
                        Text(
                            text = "Tipus d'Ajuda Sol·licitada",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            viewModel.tipusAjudaSolicitada.forEach { (text, state) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = state.value,
                                        onCheckedChange = { state.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF66B2))
                                    )
                                    Text(text, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Les teves preferències de convivència",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            viewModel.preferenciesConvivencia.forEach { (text, state) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = state.value,
                                        onCheckedChange = { state.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF66B2))
                                    )
                                    Text(text, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    } else if (viewModel.rol.value == "Estudiant") {
                        Text(
                            text = "Personalitza el teu allotjament ideal",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Preferencia tipus de habitació",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6E7C9A)
                        )
                        Column {
                            listOf("Individual", "Compartida", "M'és indiferent").forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = viewModel.tipusHabitacio.value == option,
                                        onClick = { viewModel.tipusHabitacio.value = option },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF66B2))
                                    )
                                    Text(option, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Preferencies sobre la persona major",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6E7C9A)
                        )
                        Column {
                            listOf(
                                "Prefereixo algú que necessiti més companyia que ajuda en tasques",
                                "Puc ajudar en tasques domèstiques",
                                "No tinc preferències"
                            ).forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = viewModel.preferenciaPersona.value == option,
                                        onClick = { viewModel.preferenciaPersona.value = option },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF66B2))
                                    )
                                    Text(option, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Durada de l'estada",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6E7C9A)
                        )
                        Column {
                            listOf("3 mesos", "6 mesos", "Un any", "Indiferent").forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = viewModel.duradaEstada.value == option,
                                        onClick = { viewModel.duradaEstada.value = option },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF66B2))
                                    )
                                    Text(option, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }

                9 -> {
                    if (viewModel.rol.value == "Estudiant") {
                        Text(
                            text = "Les teves habilitats a la llar",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            viewModel.habilitatsLlar.forEach { (text, state) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = state.value,
                                        onCheckedChange = { state.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF66B2))
                                    )
                                    Text(text, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Les teves preferències de convivència",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF425270),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            viewModel.preferenciesConvivenciaEstudiant.forEach { (text, state) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = state.value,
                                        onCheckedChange = { state.value = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF66B2))
                                    )
                                    Text(text, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (viewModel.validarStepActual()) {
                    if(viewModel.step.value == 1){
                        ambitDeCorrutina.launch {
                            val resultat =
                                manegadorAutentificacio.registraUsuariAmbCorreuIContrasenya(
                                    viewModel.email.value,
                                    viewModel.contrasenya.value
                                )
                            when (resultat) {
                                is Resposta.Exit -> {
                                    step.value += 1
                                }
                                is Resposta.Fracas -> {
                                    viewModel.errorMessage.value = resultat.missatgeError
                                }
                            }
                        }
                    }else if (viewModel.step.value == totalSteps) {
                        ambitDeCorrutina.launch {
                            val fotoPerfilFinal = viewModel.downloadUrl.value ?: ""
                            val fotoAllotjamentFinal = viewModel.allotjamentDownloadUrl.value ?: ""

                            val usuari: UsuariBase = if (viewModel.rol.value == "Estudiant") {
                                Estudiant(
                                    correu = viewModel.email.value,
                                    nom = viewModel.nom.value,
                                    cognoms = viewModel.cognom.value,
                                    dataNaixement = viewModel.dataNaixement.value,
                                    genere = viewModel.selectedGender.value ?: "",
                                    fotoPerfil = fotoPerfilFinal,
                                    rol = viewModel.rol.value,
                                    disponibilitat = viewModel.obtenirDisponibilitat(),
                                    tipusHabitacio = viewModel.tipusHabitacio.value,
                                    preferenciaTipusAjuda = viewModel.preferenciaPersona.value,
                                    duradaEstada = viewModel.duradaEstada.value,
                                    habilitatsLlar = habilitatsSeleccionades,
                                    preferenciesConvivencia = preferenciesEstudiant
                                )
                            } else {
                                PersonaGran(
                                    correu = viewModel.email.value,
                                    nom = viewModel.nom.value,
                                    cognoms = viewModel.cognom.value,
                                    dataNaixement = viewModel.dataNaixement.value,
                                    genere = viewModel.selectedGender.value ?: "",
                                    fotoPerfil = fotoPerfilFinal,
                                    rol = viewModel.rol.value,
                                    imatgesAllotjament = fotoAllotjamentFinal,
                                    ubicacioAllotjament = ubicacioAllotjament,
                                    preuAllotjament = preu,
                                    superficieAllotjament = superficie,
                                    descripcioAllotjament = descripcio,
                                    tipusAjudaSolicitada = ajudesSeleccionades,
                                    preferenciesConvivencia = preferenciesGran,
                                    latitud = selectedLatLng?.latitude,
                                    longitud = selectedLatLng?.longitude
                                )
                            }

                            val dao = UsuarisDAOFirebaseImpl(manegadorFirestore)
                            dao.afegeixUsuari(usuari)

                            navController.navigate(DestinacioPantallaPrincipal)
                        }
                    }
                    else {
                        viewModel.step.value += 1
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            enabled = totalSteps != null || viewModel.step.value < 6
        ) {
            Text(
                text = if (totalSteps != null && viewModel.step.value == totalSteps) "Finalitza registre"
                else "Següent"
            )
        }

        if (viewModel.errorMessage.value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp)
            ) {
                Text(
                    text = viewModel.errorMessage.value,
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
    }
}
fun uploadImageToFirebase(context: Context, imageUri: Uri, onSuccess: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileName = UUID.randomUUID().toString() + ".jpg"
    val imageRef = storageRef.child("fotosPerfil/$fileName")

    val inputStream = context.contentResolver.openInputStream(imageUri)

    inputStream?.let {
        val uploadTask = imageRef.putStream(it)
        uploadTask
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
            .addOnFailureListener {
            }
    }
}