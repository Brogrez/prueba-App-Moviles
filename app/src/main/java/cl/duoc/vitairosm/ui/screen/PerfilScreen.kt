package cl.duoc.vitairosm.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.CampoValidado
import cl.duoc.vitairosm.ui.components.DialogoAyuda
import cl.duoc.vitairosm.ui.components.DialogoConfirmacion
import cl.duoc.vitairosm.util.Dispositivo
import cl.duoc.vitairosm.util.Notificaciones
import cl.duoc.vitairosm.viewmodel.SesionViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PerfilScreen(vm: SesionViewModel, onRecursos: () -> Unit, onCerrarSesion: () -> Unit) {
    val ui by vm.estado.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var mostrarAyuda by remember { mutableStateOf(false) }
    var confirmarBorrado by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) vm.onRecordatorios(true) else vm.onPermisoDenegado()
    }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    val usuario = ui.usuario
    val iniciales = usuario?.nombre?.split(' ')?.filter { it.isNotBlank() }?.take(2)
        ?.joinToString("") { it.first().uppercase() }.orEmpty()

    Scaffold(
        topBar = { BarraSuperior("Perfil y ajustes") },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column {
                    Text(usuario?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(usuario?.correo.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    Text("Perfil simulado · ${usuario?.edad ?: ""} años", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider()
            Text("Apariencia", style = MaterialTheme.typography.titleSmall)
            FilaSwitch("Modo oscuro", ui.prefs.modoOscuro) { vm.onModoOscuro(it) }

            HorizontalDivider()
            Text("Recordatorios", style = MaterialTheme.typography.titleSmall)
            FilaSwitch("Recordatorios de acompañamiento", ui.prefs.recordatorios) { activar ->
                if (activar && !Notificaciones.tienePermiso(context)) {
                    permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    vm.onRecordatorios(activar)
                }
            }
            CampoValidado(
                campo = ui.hora,
                etiqueta = "Hora del recordatorio",
                icono = Icons.Default.Notifications,
                onCambio = vm::onHoraChange,
                onPerdioFoco = vm::onHoraBlur,
                teclado = KeyboardType.Number,
                ayuda = "Formato 24 horas, por ejemplo 20:30",
                imeAction = ImeAction.Done
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::guardarHora) { Text("Guardar hora") }
                OutlinedButton(onClick = {
                    val enviada = Notificaciones.mostrar(
                        context,
                        1001,
                        "Recordatorio de VITAIROS-M",
                        "¿Quieres registrar cómo te sientes hoy? (notificación simulada)"
                    )
                    Dispositivo.vibrar(context)
                    if (!enviada) vm.mostrarMensaje("Activa el permiso de notificaciones para probar")
                }) { Text("Probar notificación") }
            }

            HorizontalDivider()
            Text("Datos de prueba", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = vm::cargarDatosPrueba) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Cargar datos")
                }
                OutlinedButton(onClick = { confirmarBorrado = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Borrar todo")
                }
            }

            HorizontalDivider()
            Text("Ayuda", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { mostrarAyuda = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Líneas de ayuda inmediata")
            }
            OutlinedButton(onClick = onRecursos, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Recursos informativos")
            }

            Button(onClick = { confirmarSalida = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Cerrar sesión")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "VITAIROS-M · MVP académico DSY1105. Todos los datos son ficticios y se almacenan solo en este dispositivo.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    if (mostrarAyuda) DialogoAyuda(onCerrar = { mostrarAyuda = false })
    if (confirmarBorrado) {
        DialogoConfirmacion(
            titulo = "Borrar datos de prueba",
            mensaje = "Se eliminarán tus registros, citas y la conversación de orientación. Tu perfil se mantiene.",
            textoConfirmar = "Borrar",
            onConfirmar = {
                vm.borrarDatos()
                confirmarBorrado = false
            },
            onCancelar = { confirmarBorrado = false }
        )
    }
    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = "Volverás a la pantalla de inicio de sesión.",
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalida = false
                vm.cerrarSesion(onCerrarSesion)
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}

@Composable
private fun FilaSwitch(texto: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(texto, modifier = Modifier.weight(1f))
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}
