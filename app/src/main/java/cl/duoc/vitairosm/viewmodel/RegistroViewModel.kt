package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.RegistroEmocional
import cl.duoc.vitairosm.util.Formato
import cl.duoc.vitairosm.util.Seguridad
import cl.duoc.vitairosm.validacion.Validadores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class NuevoRegistroUiState(
    val animo: Int? = null,
    val errorAnimo: String? = null,
    val dominio: String = "",
    val errorDominio: String? = null,
    val nota: Campo = Campo(),
    val alertaRiesgo: Boolean = false,
    val guardando: Boolean = false
) {
    val progreso: Float
        get() = listOf(animo != null, dominio.isNotBlank(), nota.valido).count { it } / 3f
}

data class HistorialUiState(
    val registros: List<RegistroEmocional> = emptyList(),
    val filtro: String? = null,
    val total: Int = 0
)

class RegistroViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)

    private val _form = MutableStateFlow(NuevoRegistroUiState())
    val form: StateFlow<NuevoRegistroUiState> = _form.asStateFlow()

    private val _filtro = MutableStateFlow<String?>(null)

    val historial: StateFlow<HistorialUiState> =
        combine(repo.registros, _filtro) { registros, filtro ->
            HistorialUiState(
                registros = if (filtro == null) registros else registros.filter { it.dominio == filtro },
                filtro = filtro,
                total = registros.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistorialUiState())

    fun onAnimo(valor: Int) {
        _form.update { it.copy(animo = valor, errorAnimo = null) }
    }

    fun onDominio(valor: String) {
        _form.update { it.copy(dominio = valor, errorDominio = null) }
    }

    fun onNotaChange(valor: String) {
        _form.update {
            it.copy(
                nota = it.nota.escribir(valor, Validadores::nota),
                alertaRiesgo = Seguridad.detectarSenalesDeRiesgo(valor)
            )
        }
    }

    fun onNotaBlur() {
        _form.update { it.copy(nota = it.nota.revisar(Validadores::nota)) }
    }

    fun guardar(onListo: (riesgo: Boolean) -> Unit) {
        val s = _form.value
        val nota = s.nota.revisar(Validadores::nota)
        val errorAnimo = Validadores.animo(s.animo)
        val errorDominio = Validadores.dominio(s.dominio)
        _form.update { it.copy(nota = nota, errorAnimo = errorAnimo, errorDominio = errorDominio) }
        if (listOf(errorAnimo, errorDominio, nota.error).any { it != null }) return

        viewModelScope.launch {
            val animo = s.animo ?: return@launch
            _form.update { it.copy(guardando = true) }
            delay(500)
            val riesgo = Seguridad.detectarSenalesDeRiesgo(nota.valor)
            repo.agregarRegistro(
                RegistroEmocional(
                    id = UUID.randomUUID().toString(),
                    animo = animo,
                    dominio = s.dominio,
                    nota = nota.valor.trim(),
                    fechaMillis = System.currentTimeMillis()
                )
            )
            _form.value = NuevoRegistroUiState()
            onListo(riesgo)
        }
    }

    fun limpiarFormulario() {
        _form.value = NuevoRegistroUiState()
    }

    fun setFiltro(dominio: String?) {
        _filtro.value = dominio
    }

    fun eliminar(id: String) {
        viewModelScope.launch { repo.eliminarRegistro(id) }
    }

    fun resumenParaCompartir(): String {
        val lista = historial.value.registros
        if (lista.isEmpty()) return "Sin registros."
        return buildString {
            appendLine("Historial VITAIROS-M (datos de prueba)")
            lista.forEach {
                appendLine("- ${Formato.fechaHora(it.fechaMillis)} | ${it.dominio} | ánimo ${it.animo}/5 | ${it.nota}")
            }
        }
    }
}
