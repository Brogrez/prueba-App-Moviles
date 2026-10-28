package cl.duoc.vitairosm.ui.components

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.util.Dispositivo
import androidx.compose.foundation.layout.RowScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones
    )
}

@Composable
fun LogoVitairos(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "logo")
    val pulso by transicion.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulsoLogo"
    )
    Box(
        modifier = modifier
            .size(88.dp)
            .scale(pulso)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = "VITAIROS-M",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
fun AvisoLimites(onAyuda: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Column {
                Text(
                    "VITAIROS-M ofrece orientación general. No entrega diagnósticos ni reemplaza la atención de un profesional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                TextButton(onClick = onAyuda) { Text("Ayuda inmediata") }
            }
        }
    }
}

@Composable
fun DialogoAyuda(onCerrar: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Default.Call, contentDescription = null) },
        title = { Text("Ayuda inmediata") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Si tú o alguien cercano está en riesgo, no esperes: comunícate ahora. " +
                        "Esta aplicación es un prototipo y no reemplaza la atención profesional.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Catalogos.LINEAS_AYUDA.forEach { linea ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(linea.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(linea.descripcion, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = {
                            Dispositivo.vibrar(context)
                            if (!Dispositivo.llamar(context, linea.numero)) {
                                Toast.makeText(context, "Este dispositivo no puede realizar llamadas", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text(linea.visible) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (accion != null) {
            Spacer(Modifier.height(4.dp))
            accion()
        }
    }
}

@Composable
fun IndicadorEscribiendo(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "escribiendo")
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { i ->
            val alfa by transicion.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500, delayMillis = i * 160), RepeatMode.Reverse),
                label = "punto$i"
            )
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(alfa)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
            )
        }
    }
}

@Composable
fun MiniGrafico(valores: List<Float?>, etiquetas: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        valores.forEachIndexed { i, v ->
            val fraccion by animateFloatAsState(
                targetValue = ((v ?: 0f) / 5f).coerceIn(0f, 1f),
                animationSpec = tween(700, delayMillis = i * 60),
                label = "barra$i"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(64.dp).width(18.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((64 * fraccion).dp.coerceAtLeast(if (v == null) 4.dp else 6.dp))
                            .background(
                                if (v == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
                Text(etiquetas.getOrElse(i) { "" }, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
