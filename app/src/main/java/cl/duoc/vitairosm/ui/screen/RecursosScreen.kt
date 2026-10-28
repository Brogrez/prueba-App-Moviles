package cl.duoc.vitairosm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.Recurso
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.util.Dispositivo

@Composable
fun RecursosScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = { BarraSuperior("Recursos", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ayuda inmediata", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        "Si estás en riesgo o necesitas hablar con alguien ahora, llama a una de estas líneas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Catalogos.LINEAS_AYUDA.forEach { linea ->
                        OutlinedButton(
                            onClick = { Dispositivo.llamar(context, linea.numero) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  ${linea.nombre}: ${linea.visible}")
                        }
                    }
                }
            }

            EjercicioRespiracion()

            Text("Recursos informativos (contenido de prueba)", style = MaterialTheme.typography.titleMedium)
            Catalogos.RECURSOS.forEach { TarjetaRecurso(it) }

            Text(
                "Contenido informativo de prueba. No constituye diagnóstico ni atención clínica.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaRecurso(recurso: Recurso) {
    var expandido by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandido = !expandido }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(recurso.titulo, style = MaterialTheme.typography.titleSmall)
                    Text(recurso.resumen, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Contraer" else "Expandir"
                )
            }
            AnimatedVisibility(visible = expandido) {
                Text(recurso.detalle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EjercicioRespiracion() {
    var activo by remember { mutableStateOf(false) }
    val transicion = rememberInfiniteTransition(label = "respiracion")
    val escala by transicion.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escalaRespiracion"
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pausa de respiración", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.height(140.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(120.dp)
                        .scale(if (activo) escala else 0.55f)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                )
            }
            Text(
                if (activo) "Inhala cuando el círculo crece y exhala cuando se contrae." else "Presiona comenzar y sigue el ritmo del círculo.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = { activo = !activo }) { Text(if (activo) "Detener" else "Comenzar") }
        }
    }
}
