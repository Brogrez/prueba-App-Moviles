package cl.duoc.vitairosm.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.RegistroEmocional
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.CampoValidado
import cl.duoc.vitairosm.ui.components.DialogoAyuda
import cl.duoc.vitairosm.ui.components.DialogoConfirmacion
import cl.duoc.vitairosm.ui.components.EstadoVacio
import cl.duoc.vitairosm.ui.components.SelectorAnimo
import cl.duoc.vitairosm.ui.components.TarjetaRegistro
import cl.duoc.vitairosm.util.Dispositivo
import cl.duoc.vitairosm.util.Formato
import cl.duoc.vitairosm.validacion.Validadores
import cl.duoc.vitairosm.viewmodel.RegistroViewModel
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevoRegistroScreen(vm: RegistroViewModel, onVolver: () -> Unit) {
    val ui by vm.form.collectAsState()
    val context = LocalContext.current
    val foco = LocalFocusManager.current
    var mostrarAyuda by remember { mutableStateOf(false) }
    var volverAlCerrar by remember { mutableStateOf(false) }
    val progreso by animateFloatAsState(ui.progreso, label = "progresoRegistro")

    Scaffold(topBar = { BarraSuperior("Nuevo registro", onVolver = {
        vm.limpiarFormulario()
        onVolver()
    }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

            SelectorAnimo(ui.animo, ui.errorAnimo, vm::onAnimo)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Sobre qué área quieres hacer seguimiento?", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Catalogos.DOMINIOS.forEach { dominio ->
                        FilterChip(
                            selected = ui.dominio == dominio,
                            onClick = { vm.onDominio(dominio) },
                            label = { Text(dominio) }
                        )
                    }
                }
                AnimatedVisibility(visible = ui.errorDominio != null) {
                    Text(ui.errorDominio.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            CampoValidado(
                campo = ui.nota,
                etiqueta = "Nota del día (datos ficticios)",
                icono = Icons.Default.Edit,
                onCambio = vm::onNotaChange,
                onPerdioFoco = vm::onNotaBlur,
                maxCaracteres = Validadores.NOTA_MAX,
                lineas = 4
            )

            AnimatedVisibility(visible = ui.alertaRiesgo) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text(
                                "Lo que escribes puede indicar un momento muy difícil. No estás solo/a: puedes pedir ayuda ahora.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(onClick = { mostrarAyuda = true }) { Text("Ver líneas de ayuda") }
                    }
                }
            }

            Text(
                "Se guardará con fecha y hora actuales: ${Formato.fechaHora(System.currentTimeMillis())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    foco.clearFocus()
                    vm.guardar { riesgo ->
                        Dispositivo.vibrar(context, 60)
                        if (riesgo) {
                            volverAlCerrar = true
                            mostrarAyuda = true
                        } else {
                            onVolver()
                        }
                    }
                },
                enabled = !ui.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (ui.guardando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar registro")
                }
            }
        }
    }

    if (mostrarAyuda) {
        DialogoAyuda(onCerrar = {
            mostrarAyuda = false
            if (volverAlCerrar) {
                volverAlCerrar = false
                onVolver()
            }
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistorialScreen(vm: RegistroViewModel, onNuevo: () -> Unit) {
    val ui by vm.historial.collectAsState()
    val context = LocalContext.current
    var porEliminar by remember { mutableStateOf<RegistroEmocional?>(null) }

    Scaffold(
        topBar = {
            BarraSuperior("Bitácora") {
                IconButton(onClick = { compartirHistorial(context, vm) }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir historial")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo registro")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(selected = ui.filtro == null, onClick = { vm.setFiltro(null) }, label = { Text("Todos") })
                }
                items(Catalogos.DOMINIOS) { dominio ->
                    FilterChip(selected = ui.filtro == dominio, onClick = { vm.setFiltro(dominio) }, label = { Text(dominio) })
                }
            }
            if (ui.registros.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Default.Edit,
                    titulo = if (ui.total == 0) "Aún no tienes registros" else "Sin resultados para este filtro",
                    mensaje = "Cada registro guarda fecha, hora, ánimo y una nota para que puedas ver tu evolución."
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${ui.registros.size} registro(s)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(ui.registros, key = { it.id }) { registro ->
                        TarjetaRegistro(
                            registro = registro,
                            onEliminar = { porEliminar = registro },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    porEliminar?.let { registro ->
        DialogoConfirmacion(
            titulo = "Eliminar registro",
            mensaje = "Se eliminará el registro del ${Formato.fechaHora(registro.fechaMillis)}. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                vm.eliminar(registro.id)
                porEliminar = null
            },
            onCancelar = { porEliminar = null }
        )
    }
}

private fun compartirHistorial(context: Context, vm: RegistroViewModel) {
    Dispositivo.compartir(context, "Historial VITAIROS-M", vm.resumenParaCompartir())
}
