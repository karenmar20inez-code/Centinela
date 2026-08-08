package com.example.centinela.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.example.centinela.BuildConfig
import com.example.centinela.ui.theme.*

// --- IMPORTS DE MAPBOX ---
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.animation.camera
import androidx.compose.animation.core.*
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Enums locales para la simulación
enum class EstadoSimulacion {
    REGISTRO_LOCAL,
    MAPA_ACTIVO
}

enum class FaseRegistro {
    SELECCION,
    FORMULARIO
}

// --- ESTRUCTURAS DE DATOS ---
data class RouteInfo(
    val points: List<Point>,
    val type: String, // "segura" o "rapida"
    val duration: Double, // Segundos con tráfico
    val distance: Double // Metros
)

@Composable
fun MapScreen(
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {},
    autoTriggerSos: Boolean = false
) {
    val context = LocalContext.current
    val mapboxToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    val scope = rememberCoroutineScope()

    // --- ESTADOS ---
    var estadoUsuario by rememberSaveable { mutableStateOf(EstadoSimulacion.REGISTRO_LOCAL) }
    var faseRegistro by rememberSaveable { mutableStateOf(FaseRegistro.SELECCION) }
    var esRegistro by rememberSaveable { mutableStateOf(value = true) }
    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var telefonoUsuario by rememberSaveable { mutableStateOf("") }

    var mostrarAlerta by rememberSaveable { mutableStateOf(autoTriggerSos) }
    
    var origen by rememberSaveable { mutableStateOf("") }
    var destino by rememberSaveable { mutableStateOf("") }
    var analizandoRuta by rememberSaveable { mutableStateOf(false) }
    var rutaTrazada by rememberSaveable { mutableStateOf(false) }
    var planificadorVisible by rememberSaveable { mutableStateOf(true) }
    
    var routesFound by remember { mutableStateOf<List<RouteInfo>>(emptyList()) }
    var preferenciaActual by rememberSaveable { mutableStateOf("segura") }

    // --- ESTADOS DE PATRULLA ---
    var patrolEta by remember { mutableIntStateOf(0) }
    var showingPatrolEta by remember { mutableStateOf(false) }

    // --- LÓGICA DE BIENVENIDA ---
    var mostrarBienvenida by remember { mutableStateOf(false) }
    LaunchedEffect(estadoUsuario) {
        if (estadoUsuario == EstadoSimulacion.MAPA_ACTIVO) {
            mostrarBienvenida = true
            delay(4000)
            mostrarBienvenida = false
        }
    }

    // --- ANIMACIÓN FLOTANTE BIENVENIDA ---
    val welcomeTransition = rememberInfiniteTransition(label = "welcome")
    val welcomeOffset by welcomeTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Referencias a los gestores de anotaciones
    var polylineAnnotationManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    var circleAnnotationManager by remember { mutableStateOf<CircleAnnotationManager?>(null) }
    var patrolAnnotationManager by remember { mutableStateOf<CircleAnnotationManager?>(null) }

    // --- ANIMACIÓN PULSANTE SOS ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacity"
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorFondo = MaterialTheme.colorScheme.background
    val colorAlerta = MaterialTheme.colorScheme.tertiary

    Box(modifier = Modifier.fillMaxSize()) {
        if (estadoUsuario == EstadoSimulacion.REGISTRO_LOCAL) {
            // PANTALLA DE REGISTRO
            Box(
                modifier = Modifier.fillMaxSize().background(colorFondo).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Centinela CDMX",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Protegiendo tus pasos 🛡️",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(60.dp))

                    if (faseRegistro == FaseRegistro.SELECCION) {
                        Button(onClick = { esRegistro = true; faseRegistro = FaseRegistro.FORMULARIO }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = colorPrimario)) { Text("REGISTRARSE") }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { esRegistro = false; faseRegistro = FaseRegistro.FORMULARIO }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("INICIAR SESIÓN") }
                    } else {
                        LoginTextField(value = nombreUsuario, onValueChange = { nombreUsuario = it }, placeholder = "Nombre", icon = Icons.Default.Person)
                        Spacer(modifier = Modifier.height(12.dp))
                        LoginTextField(value = telefonoUsuario, onValueChange = { telefonoUsuario = it }, placeholder = "Teléfono", icon = Icons.Default.Phone)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { if (nombreUsuario.isNotBlank()) estadoUsuario = EstadoSimulacion.MAPA_ACTIVO }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = colorPrimario)) { Text("ENTRAR") }
                    }
                }
            }
        } else {
            // MAPA ACTIVO
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Color.White, modifier = Modifier.width(280.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().background(colorPrimario).padding(40.dp)) {
                            Column {
                                Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(60.dp))
                                Text(nombreUsuario, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                        NavigationDrawerItem(label = { Text("Contactos") }, selected = false, onClick = onGoToContacts, icon = { Icon(Icons.Default.Contacts, null) })
                        NavigationDrawerItem(label = { Text("Perfil") }, selected = false, onClick = onGoToProfile, icon = { Icon(Icons.Default.Person, null) })
                        Spacer(modifier = Modifier.weight(1f))
                        NavigationDrawerItem(label = { Text("Cerrar Sesión") }, selected = false, onClick = { scope.launch { drawerState.close(); estadoUsuario = EstadoSimulacion.REGISTRO_LOCAL } }, icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = colorAlerta) })
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val mapView = rememberMapViewWithLifecycle(mapboxToken)

                    // Conectar con el botón de hardware de MainActivity
                    LaunchedEffect(MainActivity.dispararAlertaGlobal) {
                        if (MainActivity.dispararAlertaGlobal) {
                            mostrarAlerta = true
                            // Simular asignación de patrulla
                            patrolEta = (2..8).random()
                            showingPatrolEta = true
                            
                            MainActivity.enviarSmsGlobal(context)
                            MainActivity.dispararAlertaGlobal = false 
                        }
                    }

                    AndroidView(
                        factory = { 
                            mapView.apply {
                                getMapboxMap().getStyle {
                                    polylineAnnotationManager = annotations.createPolylineAnnotationManager()
                                    circleAnnotationManager = annotations.createCircleAnnotationManager()
                                    patrolAnnotationManager = annotations.createCircleAnnotationManager()
                                    Log.d("MapboxDebug", "Managers listos.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // --- MENSAJE DE BIENVENIDA PREMIUM ---
                    AnimatedVisibility(
                        visible = mostrarBienvenida,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 100.dp + welcomeOffset.dp)
                            .zIndex(10f)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(30.dp),
                            shadowElevation = 10.dp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "¡Bienvenido a Centinela, $nombreUsuario! 🛡️",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // --- BOTONES SUPERIORES ---
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.surface, CircleShape).shadow(4.dp, CircleShape)
                        ) { Icon(Icons.Default.Menu, null, tint = MaterialTheme.colorScheme.primary) }
                        IconButton(
                            onClick = { planificadorVisible = true },
                            modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape).shadow(4.dp, CircleShape)
                        ) { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onPrimary) }
                    }

                    // --- PLANIFICADOR (AHORA ARRIBA CON ANIMACIÓN Y GLASS) ---
                    AnimatedVisibility(
                        visible = planificadorVisible,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding().padding(top = 70.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(32.dp), shadowElevation = 15.dp, color = Color.White.copy(alpha = 0.92f)) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Planificador Seguro", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = colorPrimario)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = origen,
                                    onValueChange = { origen = it },
                                    placeholder = { Text("Origen") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = destino,
                                    onValueChange = { destino = it },
                                    placeholder = { Text("Destino") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        if (origen.isNotBlank() && destino.isNotBlank()) {
                                            analizandoRuta = true
                                            val mapCenter = mapView.getMapboxMap().cameraState.center
                                            scope.launch {
                                                try {
                                                    val queryOri = if (!origen.lowercase().contains("cdmx")) "$origen, CDMX" else origen
                                                    val queryDes = if (!destino.lowercase().contains("cdmx")) "$destino, CDMX" else destino
                                                    val pOri = geocodeSuspend(mapboxToken, queryOri, mapCenter)
                                                    val pDes = geocodeSuspend(mapboxToken, queryDes, mapCenter)
                                                    if (pOri == null || pDes == null) {
                                                        analizandoRuta = false
                                                        Toast.makeText(context, "Dirección no encontrada.", Toast.LENGTH_SHORT).show()
                                                        return@launch
                                                    }
                                                    val routes = fetchRoutesSuspend(mapboxToken, pOri, pDes)
                                                    if (routes.isNotEmpty()) {
                                                        routesFound = routes
                                                        renderizarRutaPremium(polylineAnnotationManager, circleAnnotationManager, routes, "segura")
                                                        val camera = mapView.getMapboxMap().cameraForCoordinates(routes[0].points, EdgeInsets(380.0, 120.0, 380.0, 120.0), null, null)
                                                        mapView.camera.easeTo(camera, com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions { duration(1500) })
                                                        analizandoRuta = false
                                                        rutaTrazada = true
                                                        planificadorVisible = false
                                                    } else {
                                                        analizandoRuta = false
                                                        Toast.makeText(context, "No se pudo calcular la ruta.", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    analizandoRuta = false
                                                    Toast.makeText(context, "Error de red.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimario)
                                ) { Text("TRAZAR RUTA SEGURA", fontWeight = FontWeight.ExtraBold) }
                            }
                        }
                    }

                    // --- TARJETA DE INFORMACIÓN DE TRÁFICO ---
                    if (rutaTrazada && !planificadorVisible && routesFound.isNotEmpty()) {
                        val currentRoute = if (preferenciaActual == "rapida" && routesFound.size > 1) routesFound[1] else routesFound[0]
                        val min = (currentRoute.duration / 60).toInt()
                        val km = String.format(java.util.Locale.US, "%.1f", currentRoute.distance / 1000)
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 225.dp).wrapContentWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            shadowElevation = 8.dp
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$min min", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                VerticalDivider(modifier = Modifier.height(20.dp).width(1.dp), color = MaterialTheme.colorScheme.onPrimary.copy(0.3f))
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Route, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$km km", color = MaterialTheme.colorScheme.onPrimary.copy(0.9f), fontSize = 14.sp)
                            }
                        }
                    }

                    // --- SELECTOR PILL (ABAJO CON GLASS) ---
                    if (rutaTrazada && !planificadorVisible) {
                        Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp).height(55.dp), shape = RoundedCornerShape(30.dp), color = Color.White.copy(0.9f), shadowElevation = 10.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                                PillOption("Segura", preferenciaActual == "segura") {
                                    preferenciaActual = "segura"
                                    if (routesFound.isNotEmpty()) renderizarRutaPremium(polylineAnnotationManager, circleAnnotationManager, routesFound, "segura")
                                }
                                PillOption("Rápida", preferenciaActual == "rapida") {
                                    preferenciaActual = "rapida"
                                    if (routesFound.isNotEmpty()) renderizarRutaPremium(polylineAnnotationManager, circleAnnotationManager, routesFound, "rapida")
                                }
                            }
                        }
                    }

                    // --- SELLO C5 ---
                    if (rutaTrazada) {
                        Surface(modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 100.dp), color = EmeraldGreen, shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PROTEGIDO POR C5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- BOTÓN SOS CON ANIMACIÓN DE LATIDO ---
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).zIndex(10f)) {
                        Surface(modifier = Modifier.size(100.dp * scale), shape = CircleShape, color = colorAlerta.copy(alpha = opacity), shadowElevation = 12.dp, border = androidx.compose.foundation.BorderStroke(4.dp * scale, Color.White.copy(0.4f))) {
                            Box(modifier = Modifier.fillMaxSize().clickable { 
                                MainActivity.dispararAlertaGlobal = true 
                                mostrarAlerta = true
                            }, contentAlignment = Alignment.Center) {
                                Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                            }
                        }
                    }

                    if (mostrarAlerta) {
                        AlertDialog(
                            onDismissRequest = { mostrarAlerta = false; showingPatrolEta = false },
                            confirmButton = { Button(onClick = { mostrarAlerta = false; showingPatrolEta = false }) { Text("ESTOY BIEN") } },
                            title = { Text("¡ALERTA ENVIADA!") },
                            text = { 
                                val patrolInfo = if (showingPatrolEta) "\n\n🚓 Unidad asignada en camino. Tiempo estimado: $patrolEta min." else ""
                                Text("Tu ubicación en tiempo real ha sido enviada al C5 de la CDMX y a tus contactos de confianza. El auxilio va en camino.$patrolInfo") 
                            }
                        )
                    }

                    if (analizandoRuta) {
                        Box(modifier = Modifier.fillMaxSize().background(MidnightBlue.copy(0.4f)), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
fun PillOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable { onClick() }.padding(2.dp), shape = RoundedCornerShape(20.dp), color = if (isSelected) MidnightBlue else Color.Transparent) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- FUNCIONES DE APOYO (REFACTORIZADAS A COROUTINES) ---

suspend fun simulatePatrolSearch(token: String, userLoc: Point): Pair<Point, Int> {
    val randomLat = userLoc.latitude() + (Math.random() - 0.5) / 50.0
    val randomLon = userLoc.longitude() + (Math.random() - 0.5) / 50.0
    val patrolPoint = Point.fromLngLat(randomLon, randomLat)
    val options = com.mapbox.api.directions.v5.models.RouteOptions.builder()
        .baseUrl("https://api.mapbox.com").user("mapbox").profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(listOf(patrolPoint, userLoc)).build()
    val etaSeconds = suspendCancellableCoroutine<Double> { continuation ->
        MapboxDirections.builder().accessToken(token).routeOptions(options).build().enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                val duration = response.body()?.routes()?.firstOrNull()?.duration() ?: 300.0
                continuation.resume(duration)
            }
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) { continuation.resume(300.0) }
        })
    }
    return Pair(patrolPoint, (etaSeconds / 60).toInt())
}

suspend fun geocodeSuspend(token: String, query: String, proxy: Point): Point? = suspendCancellableCoroutine { continuation ->
    MapboxGeocoding.builder().accessToken(token).query(query).country("MX").proximity(proxy).autocomplete(true).limit(1).build().enqueueCall(object : Callback<GeocodingResponse> {
        override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
            val f = response.body()?.features()
            continuation.resume(if (f.isNullOrEmpty()) null else f[0].center())
        }
        override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) { continuation.resume(null) }
    })
}

suspend fun fetchRoutesSuspend(token: String, start: Point, end: Point): List<RouteInfo> = suspendCancellableCoroutine { continuation ->
    val options = com.mapbox.api.directions.v5.models.RouteOptions.builder()
        .baseUrl("https://api.mapbox.com").user("mapbox").profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(listOf(start, end)).overview(DirectionsCriteria.OVERVIEW_FULL).geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
        .alternatives(true).annotationsList(listOf(DirectionsCriteria.ANNOTATION_DURATION, DirectionsCriteria.ANNOTATION_DISTANCE)).build()
    MapboxDirections.builder().accessToken(token).routeOptions(options).build().enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
            if (!response.isSuccessful) { continuation.resume(emptyList()); return }
            val routes = response.body()?.routes() ?: emptyList()

            // Ordenamos para que la ruta más lenta (Segura) sea la primera
            // y la más rápida sea la segunda (Rápida).
            val sortedRoutes = routes.sortedByDescending { it.duration() ?: 0.0 }

            val mapped = sortedRoutes.mapIndexed { i, r ->
                val geometry = r.geometry()
                val pts = if (geometry != null) {
                    try { com.mapbox.geojson.LineString.fromPolyline(geometry, 6).coordinates() }
                    catch (e: Exception) { try { com.mapbox.geojson.LineString.fromPolyline(geometry, 5).coordinates() } catch (e2: Exception) { emptyList() } }
                } else emptyList()
                RouteInfo(pts, if (i == 0) "segura" else "rapida", r.duration() ?: 0.0, r.distance() ?: 0.0)
            }

            // Aseguramos que la diferencia de tiempo sea mínima de 10 min
            val result = if (mapped.size >= 2) {
                val safe = mapped[0]
                val fast = mapped[1]
                val diff = safe.duration - fast.duration
                if (diff < 600.0) {
                    val newSafeDuration = fast.duration + 600.0 + (120..300).random()
                    listOf(safe.copy(duration = newSafeDuration), fast)
                } else {
                    mapped
                }
            } else if (mapped.size == 1) {
                val original = mapped[0]
                val safeDuration = original.duration + 600.0 + (120..300).random()
                listOf(original.copy(type = "segura", duration = safeDuration), original.copy(type = "rapida"))
            } else {
                emptyList()
            }

            continuation.resume(result)
        }
        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) { continuation.resume(emptyList()) }
    })
}

private fun renderizarRutaPremium(polyManager: PolylineAnnotationManager?, circleManager: CircleAnnotationManager?, routes: List<RouteInfo>, pref: String) {
    if (polyManager == null || circleManager == null) return
    try {
        polyManager.deleteAll()
        circleManager.deleteAll()
        val r = if (pref == "rapida" && routes.size > 1) routes[1] else routes[0]
        if (r.points.isEmpty()) return
        polyManager.create(PolylineAnnotationOptions().withPoints(r.points).withLineColor("#FFFFFF").withLineWidth(14.0))
        polyManager.create(PolylineAnnotationOptions().withPoints(r.points).withLineColor("#00B4D8").withLineWidth(8.0))
        val startPoint = r.points.first(); val endPoint = r.points.last()
        circleManager.create(CircleAnnotationOptions().withPoint(startPoint).withCircleRadius(10.0).withCircleColor("#FFFFFF").withCircleStrokeWidth(2.0).withCircleStrokeColor("#00B4D8"))
        circleManager.create(CircleAnnotationOptions().withPoint(startPoint).withCircleRadius(6.0).withCircleColor("#00B4D8"))
        circleManager.create(CircleAnnotationOptions().withPoint(endPoint).withCircleRadius(12.0).withCircleColor("#FFFFFF").withCircleStrokeWidth(3.0).withCircleStrokeColor("#FF0000"))
        circleManager.create(CircleAnnotationOptions().withPoint(endPoint).withCircleRadius(7.0).withCircleColor("#FF0000"))
    } catch (e: Exception) { Log.e("MapboxDebug", "Error render: ${e.message}") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun rememberMapViewWithLifecycle(accessToken: String): MapView {
    val context = LocalContext.current
    val mapView = remember {
        val mapInitOptions = MapInitOptions(context = context, resourceOptions = ResourceOptions.Builder().accessToken(accessToken).build())
        MapView(context, mapInitOptions).apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            getMapboxMap().setCamera(CameraOptions.Builder().center(Point.fromLngLat(-99.1332, 19.4326)).zoom(14.0).build())
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val observer = remember { LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> Unit
        }
    } }
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}
