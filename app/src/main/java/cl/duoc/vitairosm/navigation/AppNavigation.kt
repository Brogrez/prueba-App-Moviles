package cl.duoc.vitairosm.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.ui.screen.AgendaScreen
import cl.duoc.vitairosm.ui.screen.AgendarCitaScreen
import cl.duoc.vitairosm.ui.screen.BienvenidaScreen
import cl.duoc.vitairosm.ui.screen.HistorialScreen
import cl.duoc.vitairosm.ui.screen.InicioScreen
import cl.duoc.vitairosm.ui.screen.LoginScreen
import cl.duoc.vitairosm.ui.screen.NuevoRegistroScreen
import cl.duoc.vitairosm.ui.screen.OrientacionScreen
import cl.duoc.vitairosm.ui.screen.PerfilScreen
import cl.duoc.vitairosm.ui.screen.RecursosScreen
import cl.duoc.vitairosm.ui.screen.RegistroUsuarioScreen
import cl.duoc.vitairosm.viewmodel.AgendaViewModel
import cl.duoc.vitairosm.viewmodel.AuthViewModel
import cl.duoc.vitairosm.viewmodel.InicioViewModel
import cl.duoc.vitairosm.viewmodel.OrientacionViewModel
import cl.duoc.vitairosm.viewmodel.RegistroViewModel
import cl.duoc.vitairosm.viewmodel.SesionViewModel

object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val REGISTRO_USUARIO = "registro_usuario"
    const val INICIO = "inicio"
    const val BITACORA = "bitacora"
    const val NUEVO_REGISTRO = "nuevo_registro"
    const val ORIENTACION = "orientacion"
    const val AGENDA = "agenda"
    const val AGENDAR = "agendar"
    const val RECURSOS = "recursos"
    const val PERFIL = "perfil"
}

private data class DestinoTab(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val TABS = listOf(
    DestinoTab(Rutas.INICIO, "Inicio", Icons.Default.Home),
    DestinoTab(Rutas.BITACORA, "Bitácora", Icons.Default.Edit),
    DestinoTab(Rutas.ORIENTACION, "Orientación", Icons.AutoMirrored.Filled.Send),
    DestinoTab(Rutas.AGENDA, "Agenda", Icons.Default.DateRange),
    DestinoTab(Rutas.PERFIL, "Perfil", Icons.Default.Person)
)

private fun NavHostController.irATab(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(sesionVm: SesionViewModel) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val inicioVm: InicioViewModel = viewModel()
    val registroVm: RegistroViewModel = viewModel()
    val orientacionVm: OrientacionViewModel = viewModel()
    val agendaVm: AgendaViewModel = viewModel()

    val sesion by sesionVm.estado.collectAsState()
    val destinoInicial = remember {
        when {
            !sesion.prefs.onboardingAceptado -> Rutas.BIENVENIDA
            !sesion.prefs.sesionActiva || sesion.usuario == null -> Rutas.LOGIN
            else -> Rutas.INICIO
        }
    }

    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val mostrarBarra = TABS.any { it.ruta == rutaActual }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = mostrarBarra,
                enter = slideInVertically(tween(250)) { it } + fadeIn(tween(250)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
            ) {
                NavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = entrada?.destination?.hierarchy?.any { it.route == tab.ruta } == true,
                            onClick = { navController.irATab(tab.ruta) },
                            icon = { Icon(tab.icono, contentDescription = tab.etiqueta) },
                            label = { Text(tab.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
            NavHost(
                navController = navController,
                startDestination = destinoInicial,
                enterTransition = { fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 16 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 16 } }
            ) {
                composable(Rutas.BIENVENIDA) {
                    BienvenidaScreen(vm = sesionVm, onContinuar = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                        }
                    })
                }
                composable(Rutas.LOGIN) {
                    LoginScreen(
                        vm = authVm,
                        onIngreso = {
                            navController.navigate(Rutas.INICIO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                        },
                        onCrearPerfil = { navController.navigate(Rutas.REGISTRO_USUARIO) }
                    )
                }
                composable(Rutas.REGISTRO_USUARIO) {
                    RegistroUsuarioScreen(
                        vm = authVm,
                        onVolver = { navController.popBackStack() },
                        onCreado = {
                            navController.navigate(Rutas.INICIO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                        }
                    )
                }
                composable(Rutas.INICIO) {
                    InicioScreen(
                        inicioVm = inicioVm,
                        sesionVm = sesionVm,
                        onRegistrar = { navController.navigate(Rutas.NUEVO_REGISTRO) },
                        onOrientacion = { navController.irATab(Rutas.ORIENTACION) },
                        onAgenda = { navController.irATab(Rutas.AGENDA) },
                        onRecursos = { navController.navigate(Rutas.RECURSOS) }
                    )
                }
                composable(Rutas.BITACORA) {
                    HistorialScreen(vm = registroVm, onNuevo = { navController.navigate(Rutas.NUEVO_REGISTRO) })
                }
                composable(Rutas.NUEVO_REGISTRO) {
                    NuevoRegistroScreen(vm = registroVm, onVolver = { navController.popBackStack() })
                }
                composable(Rutas.ORIENTACION) {
                    OrientacionScreen(
                        vm = orientacionVm,
                        onAccion = { accion ->
                            when (accion) {
                                AccionSugerida.REGISTRO -> navController.navigate(Rutas.NUEVO_REGISTRO)
                                AccionSugerida.RECURSOS -> navController.navigate(Rutas.RECURSOS)
                                AccionSugerida.AGENDA -> navController.irATab(Rutas.AGENDA)
                                else -> Unit
                            }
                        }
                    )
                }
                composable(Rutas.AGENDA) {
                    AgendaScreen(vm = agendaVm, onNueva = { navController.navigate(Rutas.AGENDAR) })
                }
                composable(Rutas.AGENDAR) {
                    AgendarCitaScreen(vm = agendaVm, onVolver = { navController.popBackStack() })
                }
                composable(Rutas.RECURSOS) {
                    RecursosScreen(onVolver = { navController.popBackStack() })
                }
                composable(Rutas.PERFIL) {
                    PerfilScreen(
                        vm = sesionVm,
                        onRecursos = { navController.navigate(Rutas.RECURSOS) },
                        onCerrarSesion = {
                            authVm.limpiar()
                            navController.navigate(Rutas.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
