package cl.duoc.vitairosm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.CampoValidado
import cl.duoc.vitairosm.ui.components.LogoVitairos
import cl.duoc.vitairosm.viewmodel.AuthViewModel
import cl.duoc.vitairosm.viewmodel.SesionViewModel

@Composable
fun BienvenidaScreen(vm: SesionViewModel, onContinuar: () -> Unit) {
    val ui by vm.estado.collectAsState()
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoVitairos()
            Text("VITAIROS-M", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Experiencia móvil de orientación y acompañamiento",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PuntoInformativo("Orientación, no diagnóstico", "La app no diagnostica ni reemplaza la atención de un profesional de salud mental.")
            PuntoInformativo("Solo datos ficticios", "Este es un prototipo académico. No ingreses datos personales ni clínicos reales.")
            PuntoInformativo("Ayuda inmediata", "En cada momento puedes acceder a líneas de ayuda si lo necesitas.")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = ui.avisoAceptado, onCheckedChange = vm::onAvisoChange)
                Text(
                    "Entiendo que es un prototipo con datos ficticios y que no reemplaza atención profesional.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AnimatedVisibility(visible = ui.errorAviso != null) {
                Text(ui.errorAviso.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { vm.confirmarAviso(onContinuar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Continuar") }
        }
    }
}

@Composable
private fun PuntoInformativo(titulo: String, texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column {
                Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
fun LoginScreen(vm: AuthViewModel, onIngreso: () -> Unit, onCrearPerfil: () -> Unit) {
    val ui by vm.login.collectAsState()
    val foco = LocalFocusManager.current
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoVitairos()
            Text("Inicia sesión", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Perfiles simulados: usa un correo y datos ficticios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            CampoValidado(
                campo = ui.correo,
                etiqueta = "Correo (ficticio)",
                icono = Icons.Default.Email,
                onCambio = vm::onLoginCorreo,
                onPerdioFoco = vm::onLoginCorreoBlur,
                teclado = KeyboardType.Email
            )
            CampoValidado(
                campo = ui.clave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                onCambio = vm::onLoginClave,
                onPerdioFoco = vm::onLoginClaveBlur,
                esClave = true,
                imeAction = ImeAction.Done
            )
            AnimatedVisibility(visible = ui.errorGeneral != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(ui.errorGeneral.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            Button(
                onClick = {
                    foco.clearFocus()
                    vm.iniciarSesion(onIngreso)
                },
                enabled = !ui.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar")
                }
            }
            TextButton(onClick = onCrearPerfil) { Text("Crear perfil de prueba") }
        }
    }
}

@Composable
fun RegistroUsuarioScreen(vm: AuthViewModel, onVolver: () -> Unit, onCreado: () -> Unit) {
    val ui by vm.registro.collectAsState()
    val foco = LocalFocusManager.current
    val progreso by animateFloatAsState(ui.progreso, label = "progresoRegistro")

    Scaffold(topBar = { BarraSuperior("Crear perfil de prueba", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
            Text(
                "Completa los datos con información ficticia. Se guardan solo en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoValidado(ui.nombre, "Nombre", Icons.Default.Person, vm::onNombre, vm::onNombreBlur)
            CampoValidado(ui.correo, "Correo (ficticio)", Icons.Default.Email, vm::onCorreo, vm::onCorreoBlur, teclado = KeyboardType.Email)
            CampoValidado(ui.edad, "Edad", Icons.Default.Face, vm::onEdad, vm::onEdadBlur, teclado = KeyboardType.Number, ayuda = "Solo mayores de 18 años")
            CampoValidado(ui.clave, "Contraseña", Icons.Default.Lock, vm::onClave, vm::onClaveBlur, esClave = true, ayuda = "Mínimo 8 caracteres, con letras y números")
            CampoValidado(ui.confirmar, "Repetir contraseña", Icons.Default.Lock, vm::onConfirmar, vm::onConfirmarBlur, esClave = true, imeAction = ImeAction.Done)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = ui.aceptaTerminos, onCheckedChange = vm::onTerminos)
                Text("Acepto usar la app solo con datos de prueba", style = MaterialTheme.typography.bodyMedium)
            }
            AnimatedVisibility(visible = ui.errorTerminos != null) {
                Text(ui.errorTerminos.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    foco.clearFocus()
                    vm.crearPerfil(onCreado)
                },
                enabled = !ui.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear perfil")
                }
            }
        }
    }
}
