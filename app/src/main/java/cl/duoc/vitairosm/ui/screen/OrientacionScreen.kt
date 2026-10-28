package cl.duoc.vitairosm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.domain.MotorOrientacion
import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.ui.components.AvisoLimites
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.BurbujaChat
import cl.duoc.vitairosm.ui.components.DialogoAyuda
import cl.duoc.vitairosm.ui.components.DialogoConfirmacion
import cl.duoc.vitairosm.ui.components.IndicadorEscribiendo
import cl.duoc.vitairosm.viewmodel.OrientacionViewModel

@Composable
fun OrientacionScreen(
    vm: OrientacionViewModel,
    onAccion: (AccionSugerida) -> Unit
) {
    val ui by vm.ui.collectAsState()
    val mensajes by vm.mensajes.collectAsState()
    val lista = rememberLazyListState()
    var mostrarAyuda by remember { mutableStateOf(false) }
    var confirmarReinicio by remember { mutableStateOf(false) }

    LaunchedEffect(mensajes.size, ui.escribiendo) {
        val total = mensajes.size + if (ui.escribiendo) 1 else 0
        if (total > 0) lista.animateScrollToItem(total - 1)
    }

    Scaffold(
        topBar = {
            BarraSuperior("Orientación") {
                IconButton(onClick = { confirmarReinicio = true }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar conversación")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AvisoLimites(onAyuda = { mostrarAyuda = true }, modifier = Modifier.padding(12.dp))

            LazyColumn(
                state = lista,
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mensajes, key = { it.id }) { mensaje ->
                    BurbujaChat(
                        mensaje = mensaje,
                        onAccion = { accion ->
                            if (accion == AccionSugerida.AYUDA) mostrarAyuda = true else onAccion(accion)
                        }
                    )
                }
                if (ui.escribiendo) {
                    item(key = "escribiendo") { IndicadorEscribiendo() }
                }
            }

            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MotorOrientacion.SUGERENCIAS) { sugerencia ->
                    AssistChip(
                        onClick = { vm.enviar(sugerencia) },
                        label = { Text(sugerencia) },
                        enabled = !ui.escribiendo
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                OutlinedTextField(
                    value = ui.entrada.valor,
                    onValueChange = vm::onEntradaChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe tu mensaje") },
                    isError = ui.entrada.error != null,
                    supportingText = { ui.entrada.error?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { vm.enviar() })
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { vm.enviar() },
                    enabled = !ui.escribiendo,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }

    if (mostrarAyuda) DialogoAyuda(onCerrar = { mostrarAyuda = false })
    if (confirmarReinicio) {
        DialogoConfirmacion(
            titulo = "Reiniciar conversación",
            mensaje = "Se borrará el historial de esta conversación de prueba.",
            textoConfirmar = "Reiniciar",
            onConfirmar = {
                vm.reiniciar()
                confirmarReinicio = false
            },
            onCancelar = { confirmarReinicio = false }
        )
    }
}
