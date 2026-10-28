package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.domain.Disponibilidad
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.Slot
import cl.duoc.vitairosm.util.Notificaciones
import cl.duoc.vitairosm.validacion.Validadores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AgendarFormUiState(
    val profesionalId: Int? = null,
    val errorProfesional: String? = null,
    val fecha: String? = null,
    val errorFecha: String? = null,
    val hora: String? = null,
    val errorHora: String? = null,
    val motivo: Campo = Campo(),
    val recordatorio: Boolean = false,
    val avisoPermiso: String? = null,
    val guardando: Boolean = false
)

data class AgendaUiState(
    val form: AgendarFormUiState = AgendarFormUiState(),
    val citas: List<Cita> = emptyList(),
    val slots: List<Slot> = emptyList(),
    val fechas: List<String> = emptyList()
)

class AgendaViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)

    private val _form = MutableStateFlow(AgendarFormUiState())

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    val estado: StateFlow<AgendaUiState> =
        combine(_form, repo.citas) { form, citas ->
            AgendaUiState(
                form = form,
                citas = citas,
                slots = Disponibilidad.slots(form.profesionalId, form.fecha, citas),
                fechas = Disponibilidad.fechas()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AgendaUiState())

    init {
        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            repo.citas.first()
                .filter { it.estado == EstadoCita.PENDIENTE && ahora - it.creadaMillis > 4000 }
                .forEach { repo.actualizarEstadoCita(it.id, EstadoCita.CONFIRMADA) }
        }
    }

    fun onProfesional(id: Int) {
        _form.update { it.copy(profesionalId = id, errorProfesional = null, hora = null, errorHora = null) }
    }

    fun onFecha(fecha: String) {
        _form.update { it.copy(fecha = fecha, errorFecha = null, hora = null, errorHora = null) }
    }

    fun onHora(hora: String) {
        _form.update { it.copy(hora = hora, errorHora = null) }
    }

    fun onMotivoChange(valor: String) {
        _form.update { it.copy(motivo = it.motivo.escribir(valor, Validadores::motivo)) }
    }

    fun onMotivoBlur() {
        _form.update { it.copy(motivo = it.motivo.revisar(Validadores::motivo)) }
    }

    fun onRecordatorio(activado: Boolean) {
        _form.update { it.copy(recordatorio = activado, avisoPermiso = null) }
    }

    fun onPermisoDenegado() {
        _form.update {
            it.copy(recordatorio = false, avisoPermiso = "Sin permiso de notificaciones no se enviará el recordatorio")
        }
    }

    fun confirmarCita(onCreada: () -> Unit) {
        val s = _form.value
        val citas = estado.value.citas
        val motivo = s.motivo.revisar(Validadores::motivo)
        val errorProfesional = Validadores.seleccion(s.profesionalId?.toString(), "Elige un profesional")
        val errorFecha = Validadores.seleccion(s.fecha, "Elige una fecha")
        var errorHora = Validadores.seleccion(s.hora, "Elige un horario")

        if (errorHora == null && s.profesionalId != null) {
            val disponible = Disponibilidad.slots(s.profesionalId, s.fecha, citas).firstOrNull { it.hora == s.hora }?.disponible == true
            val choque = citas.any { it.estado != EstadoCita.CANCELADA && it.fecha == s.fecha && it.hora == s.hora }
            errorHora = when {
                !disponible -> "Ese horario ya no está disponible"
                choque -> "Ya tienes una cita en ese horario"
                else -> null
            }
        }

        _form.update {
            it.copy(motivo = motivo, errorProfesional = errorProfesional, errorFecha = errorFecha, errorHora = errorHora)
        }
        if (listOf(motivo.error, errorProfesional, errorFecha, errorHora).any { it != null }) return

        viewModelScope.launch {
            val profesionalId = s.profesionalId ?: return@launch
            val fecha = s.fecha ?: return@launch
            val hora = s.hora ?: return@launch
            _form.update { it.copy(guardando = true) }
            delay(600)
            val cita = Cita(
                id = UUID.randomUUID().toString(),
                profesionalId = profesionalId,
                fecha = fecha,
                hora = hora,
                motivo = motivo.valor.trim(),
                estado = EstadoCita.PENDIENTE,
                creadaMillis = System.currentTimeMillis(),
                recordatorio = s.recordatorio
            )
            repo.agregarCita(cita)
            _form.value = AgendarFormUiState()
            onCreada()
            simularConfirmacion(cita)
        }
    }

    private fun simularConfirmacion(cita: Cita) {
        viewModelScope.launch {
            delay(4000)
            repo.actualizarEstadoCita(cita.id, EstadoCita.CONFIRMADA)
            val profesional = Catalogos.profesional(cita.profesionalId)?.nombre.orEmpty()
            _mensajes.tryEmit("Tu cita con $profesional fue confirmada")
            if (cita.recordatorio) {
                Notificaciones.mostrar(
                    getApplication<Application>(),
                    cita.id.hashCode(),
                    "Cita confirmada",
                    "$profesional · ${cita.fecha} a las ${cita.hora} (simulado)"
                )
            }
        }
    }

    fun cancelar(id: String) {
        viewModelScope.launch {
            repo.actualizarEstadoCita(id, EstadoCita.CANCELADA)
            _mensajes.tryEmit("Cita cancelada")
        }
    }

    fun limpiarFormulario() {
        _form.value = AgendarFormUiState()
    }
}
