package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.PreferenciasApp
import cl.duoc.vitairosm.model.Usuario
import cl.duoc.vitairosm.validacion.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SesionUiState(
    val cargado: Boolean = false,
    val prefs: PreferenciasApp = PreferenciasApp(),
    val usuario: Usuario? = null,
    val avisoAceptado: Boolean = false,
    val errorAviso: String? = null,
    val hora: Campo = Campo(PreferenciasApp().horaRecordatorio, valido = true),
    val mensaje: String? = null
)

class SesionViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)
    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()
    private var horaCargada = false

    init {
        viewModelScope.launch {
            combine(repo.preferencias, repo.usuario) { p, u -> p to u }.collect { (p, u) ->
                _estado.update {
                    it.copy(
                        cargado = true,
                        prefs = p,
                        usuario = u,
                        hora = if (horaCargada) it.hora else Campo(p.horaRecordatorio, valido = true)
                    )
                }
                horaCargada = true
            }
        }
    }

    fun onAvisoChange(valor: Boolean) {
        _estado.update { it.copy(avisoAceptado = valor, errorAviso = null) }
    }

    fun confirmarAviso(onOk: () -> Unit) {
        if (!_estado.value.avisoAceptado) {
            _estado.update { it.copy(errorAviso = Validadores.terminos(false)) }
            return
        }
        viewModelScope.launch {
            repo.aceptarOnboarding()
            onOk()
        }
    }

    fun cerrarSesion(onOk: () -> Unit) {
        viewModelScope.launch {
            repo.cerrarSesion()
            onOk()
        }
    }

    fun onModoOscuro(activado: Boolean) {
        viewModelScope.launch { repo.guardarModoOscuro(activado) }
    }

    fun onRecordatorios(activado: Boolean) {
        viewModelScope.launch { repo.guardarRecordatorios(activado) }
    }

    fun onPermisoDenegado() {
        viewModelScope.launch { repo.guardarRecordatorios(false) }
        mostrarMensaje("Sin permiso de notificaciones no se pueden enviar recordatorios")
    }

    fun onHoraChange(valor: String) {
        _estado.update { it.copy(hora = it.hora.escribir(valor.take(5), Validadores::hora)) }
    }

    fun onHoraBlur() {
        _estado.update { it.copy(hora = it.hora.revisar(Validadores::hora)) }
    }

    fun guardarHora() {
        val hora = _estado.value.hora.revisar(Validadores::hora)
        _estado.update { it.copy(hora = hora) }
        if (hora.error != null) return
        viewModelScope.launch {
            repo.guardarHora(hora.valor)
            mostrarMensaje("Hora de recordatorio guardada: ${hora.valor}")
        }
    }

    fun cargarDatosPrueba() {
        viewModelScope.launch {
            repo.cargarDatosPrueba()
            mostrarMensaje("Datos sintéticos cargados")
        }
    }

    fun borrarDatos() {
        viewModelScope.launch {
            repo.borrarDatos()
            mostrarMensaje("Registros, citas y conversación eliminados")
        }
    }

    fun mostrarMensaje(texto: String) {
        _estado.update { it.copy(mensaje = texto) }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }
}
