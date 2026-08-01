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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast

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
    val type: String // "segura" o "rapida"
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
    var esRegistro by rememberSaveable { mutableStateOf(true) }
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

    // --- LÓGICA DE BIENVENIDA ---
    var mostrarBienvenida by remember { mutableStateOf(false) }
    LaunchedEffect(estadoUsuario) {
        if (estadoUsuario == EstadoSimulacion.MAPA_ACTIVO) {
            mostrarBienvenida = true
            delay(4000L)
            mostrarBienvenida = false
        }
    }

    // Referencia al gestor de rutas
    var polylineAnnotationManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val colorPrimario = MidnightBlue
    val colorFondo = BoneWhite
    val colorAlerta = CrimsonRed

    Box(modifier = Modifier.fillMaxSize()) {
        if (estadoUsuario == EstadoSimulacion.REGISTRO_LOCAL) {
            // PANTALLA DE REGISTRO
            Box(
                modifier = Modifier.fillMaxSize().background(colorFondo).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Centinela CDMX", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = colorPrimario)
                    Text(text = "Protegiendo tus pasos 🛡️", fontSize = 14.sp, color = SteelGray)
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

                    AndroidView(
                        factory = { 
                            mapView.apply {
                                polylineAnnotationManager = annotations.createPolylineAnnotationManager()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // --- BOTONES SUPERIORES ---
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(50.dp).background(Color.White, CircleShape).shadow(4.dp, CircleShape)
                        ) { Icon(Icons.Default.Menu, null, tint = colorPrimario) }

                        IconButton(
                            onClick = { planificadorVisible = true },
                            modifier = Modifier.size(50.dp).background(colorPrimario, CircleShape).shadow(4.dp, CircleShape)
                        ) { Icon(Icons.Default.Search, null, tint = Color.White) }
                    }

                    // --- PLANIFICADOR ---
                    if (planificadorVisible) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 15.dp
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Planificador Seguro", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colorPrimario)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(value = origen, onValueChange = { origen = it }, placeholder = { Text("Origen") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = destino, onValueChange = { destino = it }, placeholder = { Text("Destino") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        if (origen.isNotBlank() && destino.isNotBlank()) {
                                            analizandoRuta = true
                                            val mapCenter = mapView.getMapboxMap().cameraState.center
                                            trazarRutaReal(mapboxToken, origen, destino, mapCenter, polylineAnnotationManager, { res ->
                                                routesFound = res
                                                analizandoRuta = false
                                                rutaTrazada = true
                                                planificadorVisible = false
                                            }, {
                                                analizandoRuta = false
                                                Toast.makeText(context, "No se encontró ruta.", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimario)
                                ) { Text("TRAZAR RUTA SEGURA") }
                            }
                        }
                    }

                    // --- SELECTOR PILL (ABAJO) ---
                    if (rutaTrazada && !planificadorVisible) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 150.dp).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                                PillOption("Segura", preferenciaActual == "segura") {
                                    preferenciaActual = "segura"
                                    renderizarRuta(polylineAnnotationManager, routesFound, "segura")
                                }
                                PillOption("Rápida", preferenciaActual == "rapida") {
                                    preferenciaActual = "rapida"
                                    renderizarRuta(polylineAnnotationManager, routesFound, "rapida")
                                }
                            }
                        }
                    }

                    // --- SELLO C5 ---
                    if (rutaTrazada) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 120.dp),
                            color = EmeraldGreen,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PROTEGIDO POR C5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- BOTÓN SOS ---
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).zIndex(10f)) {
                        Button(
                            onClick = { mostrarAlerta = true },
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colorAlerta),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) { Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
                    }

                    if (mostrarAlerta) {
                        AlertDialog(
                            onDismissRequest = { mostrarAlerta = false },
                            confirmButton = { Button(onClick = { mostrarAlerta = false }) { Text("ESTOY BIEN") } },
                            title = { Text("¡ALERTA ENVIADA!") },
                            text = { Text("Notificando a C5 y compartiendo ubicación.") }
                        )
                    }

                    if (analizandoRuta) {
                        Box(modifier = Modifier.fillMaxSize().background(MidnightBlue.copy(0.4f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PillOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }.padding(2.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MidnightBlue else Color.Transparent
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun trazarRutaReal(
    token: String,
    ori: String,
    des: String,
    proxy: Point,
    manager: PolylineAnnotationManager?,
    onSuccess: (List<RouteInfo>) -> Unit,
    onError: () -> Unit
) {
    geocodeProxy(token, ori, proxy) { pOri ->
        if (pOri == null) { onError(); return@geocodeProxy }
        geocodeProxy(token, des, proxy) { pDes ->
            if (pDes == null) { onError(); return@geocodeProxy }
            
            val options = com.mapbox.api.directions.v5.models.RouteOptions.builder()
                .baseUrl("https://api.mapbox.com")
                .user("mapbox")
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .coordinatesList(listOf(pOri, pDes))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
                .alternatives(true)
                .build()

            MapboxDirections.builder().accessToken(token).routeOptions(options).build().enqueueCall(object : Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    val routes = response.body()?.routes() ?: emptyList()
                    val result = routes.mapIndexed { i, r ->
                        val pts = com.mapbox.geojson.LineString.fromPolyline(r.geometry()!!, 6).coordinates()
                        RouteInfo(pts, if (i == 0) "segura" else "rapida")
                    }
                    if (result.isNotEmpty()) {
                        renderizarRuta(manager, result, "segura")
                        onSuccess(result)
                    } else onError()
                }
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) { onError() }
            })
        }
    }
}

private fun renderizarRuta(manager: PolylineAnnotationManager?, routes: List<RouteInfo>, pref: String) {
    manager?.deleteAll()
    val r = if (pref == "rapida" && routes.size > 1) routes[1] else routes[0]
    manager?.create(PolylineAnnotationOptions().withPoints(r.points).withLineColor("#ffffff").withLineWidth(12.0))
    manager?.create(PolylineAnnotationOptions().withPoints(r.points).withLineColor("#1c2e4a").withLineWidth(7.0))
}

private fun geocodeProxy(token: String, q: String, proxy: Point, cb: (Point?) -> Unit) {
    MapboxGeocoding.builder().accessToken(token).query(q).country("MX").proximity(proxy).limit(1).build().enqueueCall(object : Callback<GeocodingResponse> {
        override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
            val f = response.body()?.features()
            cb(if (f.isNullOrEmpty()) null else f[0].center())
        }
        override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) { cb(null) }
    })
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
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun rememberMapViewWithLifecycle(accessToken: String): MapView {
    val context = LocalContext.current
    val mapView = remember {
        val mapInitOptions = MapInitOptions(
            context = context,
            resourceOptions = ResourceOptions.Builder().accessToken(accessToken).build()
        )
        MapView(context, mapInitOptions).apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            getMapboxMap().setCamera(CameraOptions.Builder().center(Point.fromLngLat(-99.1332, 19.4326)).zoom(14.0).build())
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val observer = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
    }
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}
