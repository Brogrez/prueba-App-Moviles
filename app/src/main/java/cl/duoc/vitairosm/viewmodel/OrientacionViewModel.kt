package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.domain.MotorOrientacion
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.MensajeChat
import cl.duoc.vitairosm.validacion.Validadores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class OrientacionUiState(
    val entrada: Campo = Campo(),
    val escribiendo: Boolean = false
)

class OrientacionViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)

    private val _ui = MutableStateFlow(OrientacionUiState())
    val ui: StateFlow<OrientacionUiState> = _ui.asStateFlow()

    val mensajes: StateFlow<List<MensajeChat>> =
        repo.chat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repo.chat.first().isEmpty()) enviarBienvenida()
        }
    }

    private suspend fun enviarBienvenida() {
        val b = MotorOrientacion.bienvenida()
        repo.agregarMensaje(nuevoMensaje(false, b.texto, b.accion))
    }

    private fun nuevoMensaje(esUsuario: Boolean, texto: String, accion: cl.duoc.vitairosm.model.AccionSugerida) =
        MensajeChat(UUID.randomUUID().toString(), esUsuario, texto, System.currentTimeMillis(), accion)

    fun onEntradaChange(valor: String) {
        _ui.update { it.copy(entrada = it.entrada.escribir(valor, Validadores::mensaje)) }
    }

    fun enviar(textoRapido: String? = null) {
        if (_ui.value.escribiendo) return
        val texto = (textoRapido ?: _ui.value.entrada.valor).trim()
        val error = Validadores.mensaje(texto)
        if (error != null) {
            _ui.update { it.copy(entrada = it.entrada.copy(error = error, valido = false)) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(entrada = Campo(), escribiendo = true) }
            repo.agregarMensaje(nuevoMensaje(true, texto, cl.duoc.vitairosm.model.AccionSugerida.NINGUNA))
            delay(1300)
            val respuesta = MotorOrientacion.responder(texto)
            repo.agregarMensaje(nuevoMensaje(false, respuesta.texto, respuesta.accion))
            _ui.update { it.copy(escribiendo = false) }
        }
    }

    fun reiniciar() {
        viewModelScope.launch {
            repo.reiniciarChat()
            enviarBienvenida()
        }
    }
}
