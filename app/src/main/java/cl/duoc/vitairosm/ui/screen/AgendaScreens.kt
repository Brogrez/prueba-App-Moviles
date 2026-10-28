package cl.duoc.vitairosm.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.ui.components.BarraSuperior
import cl.duoc.vitairosm.ui.components.CampoValidado
import cl.duoc.vitairosm.ui.components.DialogoConfirmacion
import cl.duoc.vitairosm.ui.components.EstadoVacio
import cl.duoc.vitairosm.ui.components.TarjetaCita
import cl.duoc.vitairosm.util.Dispositivo
import cl.duoc.vitairosm.util.Formato
import cl.duoc.vitairosm.util.Notificaciones
import cl.duoc.vitairosm.validacion.Validadores
import cl.duoc.vitairosm.viewmodel.AgendaViewModel

@Composable
fun AgendaScreen(vm: AgendaViewModel, onNueva: () -> Unit) {
    val ui by vm.estado.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var porCancelar by remember { mutableStateOf<Cita?>(null) }

    LaunchedEffect(Unit) {
        vm.mensajes.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = { BarraSuperior("Agenda") },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agendar") }
            )
        }
    ) { padding ->
        if (ui.citas.isEmpty()) {
            EstadoVacio(
                icono = Icons.Default.DateRange,
                titulo = "No tienes citas",
                mensaje = "Agenda una cita simulada con un profesional de prueba para dar continuidad a tu acompañamiento.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.citas, key = { it.id }) { cita ->
                    TarjetaCita(cita = cita, onCancelar = { porCancelar = cita }, modifier = Modifier.animateItem())
                }
            }
        }
    }

    porCancelar?.let { cita ->
        DialogoConfirmacion(
            titulo = "Cancelar cita",
            mensaje = "¿Quieres cancelar la cita del ${Formato.fechaCorta(cita.fecha)} a las ${cita.hora}?",
            textoConfirmar = "Cancelar cita",
            onConfirmar = {
                vm.cancelar(cita.id)
                porCancelar = null
            },
            onCancelar = { porCancelar = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgendarCitaScreen(vm: AgendaViewModel, onVolver: () -> Unit) {
    val ui by vm.estado.collectAsState()
    val form = ui.form
    val context = LocalContext.current
    val foco = LocalFocusManager.current

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) vm.onRecordatorio(true) else vm.onPermisoDenegado()
    }

    Scaffold(topBar = {
        BarraSuperior("Agendar cita", onVolver = {
            vm.limpiarFormulario()
            onVolver()
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("1. Elige un profesional (perfiles ficticios)", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Catalogos.PROFESIONALES.forEach { p ->
                    val seleccionado = form.profesionalId == p.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.onProfesional(p.id) },
                        border = BorderStroke(
                            if (seleccionado) 2.dp else 1.dp,
                            if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = seleccionado, onClick = { vm.onProfesional(p.id) })
                            Column {
                                Text(p.nombre, style = MaterialTheme.typography.titleSmall)
                                Text("${p.especialidad} · ${p.modalidad}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                ErrorTexto(form.errorProfesional)
            }

            Text("2. Elige una fecha", style = MaterialTheme.typography.titleMedium)
            Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.fechas) { fecha ->
                        FilterChip(
                            selected = form.fecha == fecha,
                            onClick = { vm.onFecha(fecha) },
                            label = { Text(Formato.fechaCorta(fecha)) }
                        )
                    }
                }
                ErrorTexto(form.errorFecha)
            }

            Text("3. Elige un horario", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.animateContentSize()) {
                if (ui.slots.isEmpty()) {
                    Text(
                        "Selecciona profesional y fecha para ver los horarios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ui.slots.forEach { slot ->
                            FilterChip(
                                selected = form.hora == slot.hora,
                                onClick = { vm.onHora(slot.hora) },
                                enabled = slot.disponible,
                                label = { Text(if (slot.disponible) slot.hora else "${slot.hora} (ocupado)") }
                            )
                        }
                    }
                }
                ErrorTexto(form.errorHora)
            }

            CampoValidado(
                campo = form.motivo,
                etiqueta = "Motivo de la cita (ficticio)",
                icono = Icons.Default.Edit,
                onCambio = vm::onMotivoChange,
                onPerdioFoco = vm::onMotivoBlur,
                maxCaracteres = Validadores.MOTIVO_MAX,
                lineas = 2
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text("Recordatorio simulado", style = MaterialTheme.typography.titleSmall)
                            Text("Recibe una notificación al confirmarse la cita.", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = form.recordatorio,
                            onCheckedChange = { activar ->
                                if (activar && !Notificaciones.tienePermiso(context)) {
                                    permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.onRecordatorio(activar)
                                }
                            }
                        )
                    }
                    ErrorTexto(form.avisoPermiso)
                }
            }

            AnimatedVisibility(visible = form.profesionalId != null && form.fecha != null && form.hora != null) {
                val p = Catalogos.profesional(form.profesionalId ?: -1)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Resumen", style = MaterialTheme.typography.titleSmall)
                        Text("${p?.nombre.orEmpty()} · ${form.fecha?.let { Formato.fechaCorta(it) }.orEmpty()} a las ${form.hora.orEmpty()}")
                    }
                }
            }

            Button(
                onClick = {
                    foco.clearFocus()
                    vm.confirmarCita {
                        Dispositivo.vibrar(context, 60)
                        onVolver()
                    }
                },
                enabled = !form.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (form.guardando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar cita")
                }
            }
        }
    }
}

@Composable
private fun ErrorTexto(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Text(
            mensaje.orEmpty(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
