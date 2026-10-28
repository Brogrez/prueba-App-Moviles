package cl.duoc.vitairosm.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.MensajeChat
import cl.duoc.vitairosm.model.RegistroEmocional
import cl.duoc.vitairosm.util.Formato

@Composable
fun TarjetaRegistro(registro: RegistroEmocional, onEliminar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    registro.animo.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${registro.dominio} · ${Catalogos.ANIMOS.first { it.first == registro.animo }.second}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(registro.nota, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${Formato.fechaHora(registro.fechaMillis)} · ID ${Formato.codigoCorto(registro.id)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar registro")
            }
        }
    }
}

@Composable
fun TarjetaCita(cita: Cita, onCancelar: () -> Unit, modifier: Modifier = Modifier) {
    val profesional = Catalogos.profesional(cita.profesionalId)
    val (contenedor, contenido) = when (cita.estado) {
        EstadoCita.PENDIENTE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        EstadoCita.CONFIRMADA -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        EstadoCita.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val colorFondo by animateColorAsState(contenedor, label = "fondoEstado")
    val colorTexto by animateColorAsState(contenido, label = "textoEstado")

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(profesional?.nombre ?: "Profesional", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${profesional?.especialidad.orEmpty()} · ${profesional?.modalidad.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    cita.estado.etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorTexto,
                    modifier = Modifier
                        .background(colorFondo, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("${Formato.fechaCorta(cita.fecha)} · ${cita.hora}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(cita.motivo, style = MaterialTheme.typography.bodyMedium)
            if (cita.estado != EstadoCita.CANCELADA) {
                TextButton(onClick = onCancelar) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Cancelar cita", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
fun BurbujaChat(mensaje: MensajeChat, onAccion: (AccionSugerida) -> Unit, modifier: Modifier = Modifier) {
    val esUsuario = mensaje.esUsuario
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (esUsuario) Alignment.End else Alignment.Start
    ) {
        Text(
            mensaje.texto,
            style = MaterialTheme.typography.bodyMedium,
            color = if (esUsuario) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    if (esUsuario) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        if (!esUsuario && mensaje.accion != AccionSugerida.NINGUNA) {
            val texto = when (mensaje.accion) {
                AccionSugerida.REGISTRO -> "Hacer un registro"
                AccionSugerida.RECURSOS -> "Ver recursos"
                AccionSugerida.AGENDA -> "Agendar cita"
                AccionSugerida.AYUDA -> "Ayuda inmediata"
                AccionSugerida.NINGUNA -> ""
            }
            AssistChip(
                onClick = { onAccion(mensaje.accion) },
                label = { Text(texto, fontWeight = FontWeight.Medium) },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
