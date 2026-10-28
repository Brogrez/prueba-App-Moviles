package cl.duoc.vitairosm.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.ui.components.AvisoLimites
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.DialogoAyuda
import cl.duoc.vitairosm.ui.components.MiniGrafico
import cl.duoc.vitairosm.util.Formato
import cl.duoc.vitairosm.viewmodel.InicioViewModel
import cl.duoc.vitairosm.viewmodel.ProximoPaso
import cl.duoc.vitairosm.viewmodel.SesionViewModel
import java.time.LocalDate

@Composable
fun InicioScreen(
    inicioVm: InicioViewModel,
    sesionVm: SesionViewModel,
    onRegistrar: () -> Unit,
    onOrientacion: () -> Unit,
    onAgenda: () -> Unit,
    onRecursos: () -> Unit
) {
    val ui by inicioVm.estado.collectAsState()
    var mostrarAyuda by remember { mutableStateOf(false) }
    val promedio by animateFloatAsState((ui.promedio7d ?: 0f) / 5f, tween(800), label = "promedio")
    val hoy = LocalDate.now()
    val etiquetas = (6 downTo 0).map { Formato.inicialDia(hoy.minusDays(it.toLong())) }

    Scaffold(topBar = { BarraSuperior("VITAIROS-M") }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Hola, ${ui.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineSmall)
                Text(Formato.hoyLegible(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Crossfade(targetState = ui.proximoPaso, label = "proximoPaso") { paso ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tu próximo paso", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        when (paso) {
                            ProximoPaso.REGISTRAR -> {
                                Text("Registra cómo te sientes hoy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Button(onClick = onRegistrar) { Text("Hacer registro") }
                            }
                            ProximoPaso.CITA_PROXIMA -> {
                                val cita = ui.proximaCita
                                Text(
                                    if (cita != null) "Tienes una cita el ${Formato.fechaCorta(cita.fecha)} a las ${cita.hora}" else "Revisa tu agenda",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Button(onClick = onAgenda) { Text("Ver agenda") }
                            }
                            ProximoPaso.EXPLORAR -> {
                                Text("Ya registraste hoy. Explora recursos o conversa con el orientador.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Button(onClick = onRecursos) { Text("Ver recursos") }
                            }
                        }
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Últimos 7 días", style = MaterialTheme.typography.titleMedium)
                    MiniGrafico(ui.ultimos7, etiquetas)
                    LinearProgressIndicator(progress = { promedio }, modifier = Modifier.fillMaxWidth())
                    Text(
                        if (ui.promedio7d != null) "Ánimo promedio: ${"%.1f".format(ui.promedio7d)} de 5 · Racha: ${ui.racha} día(s)" else "Aún no hay registros esta semana",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (ui.totalRegistros == 0) {
                        TextButton(onClick = sesionVm::cargarDatosPrueba) { Text("Cargar datos sintéticos de ejemplo") }
                    }
                }
            }

            Text("Acciones frecuentes", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccionRapida(Icons.Default.Edit, "Registrar", onRegistrar, Modifier.weight(1f))
                AccionRapida(Icons.AutoMirrored.Filled.Send, "Orientación", onOrientacion, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccionRapida(Icons.Default.DateRange, "Agenda", onAgenda, Modifier.weight(1f))
                AccionRapida(Icons.Default.Favorite, "Recursos", onRecursos, Modifier.weight(1f))
            }

            AvisoLimites(onAyuda = { mostrarAyuda = true })
        }
    }

    if (mostrarAyuda) DialogoAyuda(onCerrar = { mostrarAyuda = false })
}

@Composable
private fun AccionRapida(icono: ImageVector, texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}
