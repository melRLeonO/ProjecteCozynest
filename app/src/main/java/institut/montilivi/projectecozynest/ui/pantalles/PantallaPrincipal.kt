package institut.montilivi.projectecozynest.ui.pantalles

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import institut.montilivi.projectecozynest.R
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.UsuariBase
import institut.montilivi.projectecozynest.model.UsuariActual
import institut.montilivi.projectecozynest.navegacio.DestinacioOpinionsValoracions
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaMissatges
import institut.montilivi.projectecozynest.navegacio.DestinacioPantallaPrincipal
import institut.montilivi.projectecozynest.navegacio.DestinacioPerfil
import institut.montilivi.projectecozynest.ui.viewmodels.ViewModelLike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf

@Composable
fun PantallaPrincipal(navController: NavController) {
    val viewModel: ViewModelLike = viewModel()
    var city by remember { mutableStateOf("") }
    var distance by remember { mutableFloatStateOf(0f) }
    var ageMin by remember {mutableFloatStateOf(18f) }
    var ageMax by remember { mutableFloatStateOf(100f) }
    var selectedGender by remember { mutableStateOf("") }
    var starsMin by remember { mutableFloatStateOf(0f) }
    var reviewsMin by remember { mutableFloatStateOf(0f) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val usuariActual by viewModel._usuariActual.collectAsState()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text("Filtrar per:", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    LaunchedEffect(Unit) {
                        Log.d("DEBUG", "usuariActual = $usuariActual")
                    }

                    if (usuariActual is Estudiant) {
                        UbicacioAllotjament { address, latLng ->
                            city = address
                            selectedLatLng = latLng
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Radi de cerca (km)", fontWeight = FontWeight.SemiBold)
                        Text("Fins a ${distance.toInt()} km")
                        Slider(
                            value = distance,
                            onValueChange = { distance = it },
                            valueRange = 0f..200f,
                            steps = 199,
                            enabled = selectedLatLng != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("Cercar per edat")
                    Text("Edat mínima: ${ageMin.toInt()}")
                    Slider(
                        value = ageMin,
                        onValueChange = {
                            ageMin = it.coerceAtMost(ageMax)
                        },
                        valueRange = 18f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Edat màxima: ${ageMax.toInt()}")
                    Slider(
                        value = ageMax,
                        onValueChange = {
                            ageMax = it.coerceAtLeast(ageMin)
                        },
                        valueRange = 18f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cercar per gènere")
                    Row {
                        GenderChip("Dona", selectedGender == "Dona") {
                            selectedGender = if (selectedGender == "Dona") "" else "Dona"
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GenderChip("Home", selectedGender == "Home") {
                            selectedGender = if (selectedGender == "Home") "" else "Home"
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cercar per valoracions")
                    Text("NOMBRE D'ESTRELLES")
                    Text("Mínim: ${starsMin.toInt()} ⭐")
                    Slider(
                        value = starsMin,
                        onValueChange = { starsMin = it },
                        valueRange = 0f..5f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reviewsMin.toInt().toString(),
                        onValueChange = {
                            reviewsMin = it.toIntOrNull()?.coerceIn(0, 1000)?.toFloat() ?: 0f
                        },
                        label = { Text("Nombre mínim de valoracions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = {
                                city = ""
                                distance = 0f
                                ageMin = 18f
                                ageMax = 100f
                                selectedGender = ""
                                starsMin = 0f
                                reviewsMin = 0f

                                viewModel.obtenirUsuariAleatoriFiltrat(
                                    genere = selectedGender,
                                    edatMin = ageMin.toInt(),
                                    edatMax = ageMax.toInt(),
                                    numEstrellesMin = starsMin.toInt(),
                                    numValoracionsMin = reviewsMin.toInt(),
                                    ciutat = city,
                                    distanciaMaxima = distance.toInt(),
                                    selectedLatLng = selectedLatLng
                                )

                                scope.launch { drawerState.close() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A73BE))
                        ) {
                            Text("Restablir filtres")
                        }

                        Button(
                            onClick = {
                                viewModel.obtenirUsuariAleatoriFiltrat(
                                    genere = selectedGender,
                                    edatMin = ageMin.toInt(),
                                    edatMax = ageMax.toInt(),
                                    numEstrellesMin = starsMin.toInt(),
                                    numValoracionsMin = reviewsMin.toInt(),
                                    ciutat = city,
                                    distanciaMaxima = distance.toInt(),
                                    selectedLatLng = selectedLatLng
                                )

                                scope.launch { drawerState.close() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A73BE))
                        ) {
                            Text("Aplicar filtres")
                        }
                    }
                }
            }
        }
    ) {
        ContingutPantallaPrincipal(
            navController = navController,
            drawerState = drawerState,
            scope = scope,
            selectedGender = selectedGender,
            ageMin = ageMin.toInt(),
            ageMax = ageMax.toInt(),
            starsMin = starsMin.toInt(),
            reviewsMin = reviewsMin.toInt(),
            city = city,
            distance = distance.toInt(),
            selectedLatLng = selectedLatLng
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContingutPantallaPrincipal(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    selectedGender: String,
    ageMin: Int,
    ageMax: Int,
    starsMin: Int,
    reviewsMin: Int,
    city: String,
    distance: Int,
    selectedLatLng: LatLng?
)
{
    val viewModel: ViewModelLike = viewModel()
    val dadesEstat by viewModel.estat.collectAsState()
    var textCerca by remember { mutableStateOf("") }
    val usuarisFiltrats = dadesEstat.usuaris
    val usuari = dadesEstat.usuaris.firstOrNull()
    val (mostrarDialog, setMostrarDialog) = remember { mutableStateOf(false) }
    val (usuariDelMatch, setUsuariDelMatch) = remember { mutableStateOf<UsuariBase?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logotopaapbar),
                            contentDescription = "Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(DestinacioPerfil)
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4A5A7D))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF5EEF5)) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(DestinacioPantallaMissatges) },
                    icon = { Icon(Icons.Default.Mail, contentDescription = "Missatges") },
                    label = { Text("Missatges") }
                )
                NavigationBarItem(
                    selected = true,
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
        containerColor = Color(0xFFF8F3FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = textCerca,
            onValueChange = { textCerca = it },
            label = { Text("Cerca per nom") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.obtenirUsuarisPerNom(textCerca)
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (textCerca.isNotBlank() && usuarisFiltrats.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(items = usuarisFiltrats) { usuari ->
                            UsuariCard(
                                usuari = usuari,
                                onLike = {
                                    viewModel.ferLike { esMatch ->
                                        if (esMatch) {
                                            setUsuariDelMatch(usuari)
                                            setMostrarDialog(true)
                                        } else {
                                            viewModel.obtenirUsuariAleatoriFiltrat(
                                                genere = selectedGender,
                                                edatMin = ageMin,
                                                edatMax = ageMax,
                                                numEstrellesMin = starsMin,
                                                numValoracionsMin = reviewsMin,
                                                ciutat = city,
                                                distanciaMaxima = distance,
                                                selectedLatLng = selectedLatLng
                                            )

                                        }
                                    }
                                },
                                onDislike = {
                                    viewModel.obtenirUsuariAleatoriFiltrat(
                                        genere = selectedGender,
                                        edatMin = ageMin,
                                        edatMax = ageMax,
                                        numEstrellesMin = starsMin,
                                        numValoracionsMin = reviewsMin,
                                        ciutat = city,
                                        distanciaMaxima = distance,
                                        selectedLatLng = selectedLatLng
                                    )
                                },
                                onVerValoracions = {
                                    UsuariActual.usuari = usuari
                                    navController.navigate(DestinacioOpinionsValoracions)
                                }
                            )


                        }
                    }
                }
                else {
                    if (usuari != null) {
                        UsuariCard(
                            usuari = usuari,
                            onLike = {
                                viewModel.ferLike { esMatch ->
                                    if (esMatch) {
                                        setUsuariDelMatch(usuari)
                                        setMostrarDialog(true)
                                    } else {
                                        viewModel.obtenirUsuariAleatoriFiltrat(
                                            genere = selectedGender,
                                            edatMin = ageMin,
                                            edatMax = ageMax,
                                            numEstrellesMin = starsMin,
                                            numValoracionsMin = reviewsMin,
                                            ciutat = city,
                                            distanciaMaxima = distance,
                                            selectedLatLng = selectedLatLng
                                        )
                                    }
                                }
                            },
                            onDislike = {
                                viewModel.obtenirUsuariAleatoriFiltrat(
                                    genere = selectedGender,
                                    edatMin = ageMin,
                                    edatMax = ageMax,
                                    numEstrellesMin = starsMin,
                                    numValoracionsMin = reviewsMin,
                                    ciutat = city,
                                    distanciaMaxima = distance,
                                    selectedLatLng = selectedLatLng
                                )
                            },
                            onVerValoracions = {
                                UsuariActual.usuari = usuari
                                navController.navigate(DestinacioOpinionsValoracions)
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hi ha més usuaris per ara per mostrar-te.",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (mostrarDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setMostrarDialog(false)
                        viewModel.obtenirUsuariAleatoriFiltrat(
                            genere = selectedGender,
                            edatMin = ageMin,
                            edatMax = ageMax,
                            numEstrellesMin = starsMin,
                            numValoracionsMin = reviewsMin,
                            ciutat = city,
                            distanciaMaxima = distance,
                            selectedLatLng = selectedLatLng
                        )

                    },
                    title = {
                        Text(text = "🎉 Has fet match!", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(text = "Has fet match amb ${usuariDelMatch?.nom ?: "aquesta persona"}! Podeu començar a parlar.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            setMostrarDialog(false)
                            viewModel.obtenirUsuariAleatoriFiltrat(
                                genere = selectedGender,
                                edatMin = ageMin,
                                edatMax = ageMax,
                                numEstrellesMin = starsMin,
                                numValoracionsMin = reviewsMin,
                                ciutat = city,
                                distanciaMaxima = distance,
                                selectedLatLng = selectedLatLng
                            )

                        }) {
                            Text("Entesos")
                        }
                    },
                    containerColor = Color(0xFFF8F3FA),
                    titleContentColor = Color(0xFF4A5A7D),
                    textContentColor = Color(0xFF4A5A7D)
                )
                }
            }
        }
    }

@Composable
fun GenderChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFBFA8E3) else Color.LightGray,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
@Composable
fun UbicacioAllotjament(
    onUbicacioSeleccionada: (String, LatLng?) -> Unit
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var ubicacioText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val token = remember { AutocompleteSessionToken.newInstance() }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = ubicacioText,
            onValueChange = { text ->
                ubicacioText = text
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(text)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        predictions = response.autocompletePredictions
                    }
                    .addOnFailureListener {
                        predictions = emptyList()
                    }
            },
            placeholder = { Text("Ubicació de la propietat") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Ubicació") },
            modifier = Modifier.fillMaxWidth()
        )
        if (predictions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp)
            ) {
                predictions.forEach { prediction ->
                    DropdownMenuItem(
                        text = { Text(prediction.getFullText(null).toString()) },
                        onClick = {
                            val placeId = prediction.placeId
                            val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
                            placesClient.fetchPlace(request)
                                .addOnSuccessListener { response ->
                                    val place = response.place
                                    val latLng = place.latLng
                                    val address = place.address ?: prediction.getFullText(null).toString()
                                    ubicacioText = address
                                    predictions = emptyList()
                                    onUbicacioSeleccionada(address, latLng)
                                }
                                .addOnFailureListener {
                                    ubicacioText = prediction.getFullText(null).toString()
                                    predictions = emptyList()
                                    onUbicacioSeleccionada(ubicacioText, null)
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
@Composable
fun UsuariCard(
    modifier: Modifier = Modifier,
    usuari: UsuariBase,
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onVerValoracions: () -> Unit = {},
) {
    var mostrarDetalles by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F9))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (!usuari.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = usuari.fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        tint = Color(0xFF7E57C2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("${usuari.nom} ${usuari.cognoms}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF8A73BE))
                Spacer(modifier = Modifier.width(4.dp))
                Text(usuari.dataNaixement, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))

                val (genereIcon, genereColor) = when (usuari.genere) {
                    "Home" -> Icons.Default.Male to Color(0xFF42A5F5)
                    "Dona" -> Icons.Default.Female to Color(0xFFF06292)
                    else -> Icons.Default.Person to Color.Gray
                }

                Icon(genereIcon, contentDescription = null, tint = genereColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(usuari.genere, fontSize = 14.sp, color = Color.Gray)
            }

            if (usuari is PersonaGran) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF5E35B1))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(usuari.ubicacioAllotjament, fontSize = 14.sp, color = Color.Gray)
                }
            }

            IconButton(
                onClick = { mostrarDetalles = !mostrarDetalles },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = if (mostrarDetalles) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                    contentDescription = "Ver detalles",
                    tint = Color(0xFF8A73BE)
                )
            }

            if (mostrarDetalles) {
                Spacer(modifier = Modifier.height(8.dp))
                when (usuari) {
                    is Estudiant -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 300.dp)
                                .background(Color(0xFFEDE7F6), RoundedCornerShape(12.dp))
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Detalls", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF5E35B1))
                                Spacer(Modifier.height(8.dp))

                                Spacer(Modifier.height(12.dp))
                                TaulaDisponibilitat(disponibilitat = usuari.disponibilitat)

                                Text("Tipus d'habitació:", fontWeight = FontWeight.Bold)
                                Text(usuari.tipusHabitacio)
                                Spacer(Modifier.height(6.dp))

                                Text("Preferència d'ajuda:", fontWeight = FontWeight.Bold)
                                Text(usuari.preferenciaTipusAjuda)
                                Spacer(Modifier.height(6.dp))

                                Text("Durada de l'estada:", fontWeight = FontWeight.Bold)
                                Text(usuari.duradaEstada)
                                Spacer(Modifier.height(6.dp))

                                Text("Habilitats a la llar:", fontWeight = FontWeight.Bold)
                                Column {
                                    usuari.habilitatsLlar.forEach { habilitat ->
                                        Text("• $habilitat", fontSize = 14.sp)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))

                                Text("Preferències convivència:", fontWeight = FontWeight.Bold)
                                Column {
                                    usuari.preferenciesConvivencia.forEach { preferencia ->
                                        Text("• $preferencia", fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                    }

                    is PersonaGran -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 300.dp)
                                .background(Color(0xFFEDE7F6), RoundedCornerShape(12.dp))
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    "Detalls",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF5E35B1)
                                )
                                Spacer(Modifier.height(8.dp))

                                Text("Preu:", fontWeight = FontWeight.Bold)
                                Text("${usuari.preuAllotjament} €/mes")
                                Spacer(Modifier.height(6.dp))

                                Text("Superfície:", fontWeight = FontWeight.Bold)
                                Text(usuari.superficieAllotjament)
                                Spacer(Modifier.height(6.dp))

                                Text("Descripció:", fontWeight = FontWeight.Bold)
                                Text(usuari.descripcioAllotjament ?: "Sense descripció")
                                Spacer(Modifier.height(6.dp))

                                Text("Tipus d'ajuda sol·licitada:", fontWeight = FontWeight.Bold)
                                Column {
                                    usuari.tipusAjudaSolicitada.forEach { ajudaSolicitada ->
                                        Text("• $ajudaSolicitada", fontSize = 14.sp)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))

                                Text("Preferències convivència:", fontWeight = FontWeight.Bold)
                                Column {
                                    usuari.preferenciesConvivencia.forEach { preferencies ->
                                        Text("• $preferencies", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Text("No hi ha detalls disponibles per aquest rol.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onVerValoracions) {
                Text("Opinions i valoracions", color = Color(0xFF8A73BE), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onLike) {
                    Icon(Icons.Default.Favorite, contentDescription = "Me gusta", tint = Color(0xFF00C853))
                }
                IconButton(onClick = onDislike) {
                    Icon(Icons.Default.Close, contentDescription = "No me gusta", tint = Color.Red)
                }
            }
        }
    }
}
@Composable
fun TaulaDisponibilitat(disponibilitat: Map<String, List<Boolean>>) {
    val diesSetmana = listOf("Dilluns", "Dimarts", "Dimecres", "Dijous", "Divendres", "Dissabte", "Diumenge")
    val franges = listOf("Matins", "Tardes", "Nits")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDE7F6), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Disponibilitat", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF5E35B1))
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Dia", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            franges.forEach { franja ->
                Text(franja, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        diesSetmana.forEach { dia ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(dia, modifier = Modifier.weight(2f))
                val valors = disponibilitat[dia] ?: listOf(false, false, false)
                valors.forEachIndexed { index, disponible ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .background(
                                if (disponible) Color(0xFFFF66B2) else Color.LightGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    if (index < valors.lastIndex) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }
    }
}
