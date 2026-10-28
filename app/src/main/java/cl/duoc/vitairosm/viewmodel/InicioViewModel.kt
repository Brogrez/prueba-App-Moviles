package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.util.Formato
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

enum class ProximoPaso { REGISTRAR, CITA_PROXIMA, EXPLORAR }

data class InicioUiState(
    val nombre: String = "",
    val totalRegistros: Int = 0,
    val registroHoy: Boolean = false,
    val racha: Int = 0,
    val promedio7d: Float? = null,
    val ultimos7: List<Float?> = List(7) { null },
    val proximaCita: Cita? = null,
    val proximoPaso: ProximoPaso = ProximoPaso.REGISTRAR
)

class InicioViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)

    val estado: StateFlow<InicioUiState> =
        combine(repo.usuario, repo.registros, repo.citas) { usuario, registros, citas ->
            val hoy = LocalDate.now()
            val porDia = registros.groupBy { Formato.aFecha(it.fechaMillis) }
            val registroHoy = porDia.containsKey(hoy)

            var racha = 0
            var dia = if (registroHoy) hoy else hoy.minusDays(1)
            while (porDia.containsKey(dia)) {
                racha++
                dia = dia.minusDays(1)
            }

            val ultimos7 = (6 downTo 0).map { n ->
                porDia[hoy.minusDays(n.toLong())]?.map { it.animo }?.average()?.toFloat()
            }
            val validos = ultimos7.filterNotNull()
            val proxima = citas.firstOrNull {
                it.estado != EstadoCita.CANCELADA && it.fecha >= hoy.toString()
            }

            InicioUiState(
                nombre = usuario?.nombre.orEmpty(),
                totalRegistros = registros.size,
                registroHoy = registroHoy,
                racha = racha,
                promedio7d = if (validos.isEmpty()) null else validos.average().toFloat(),
                ultimos7 = ultimos7,
                proximaCita = proxima,
                proximoPaso = when {
                    !registroHoy -> ProximoPaso.REGISTRAR
                    proxima != null -> ProximoPaso.CITA_PROXIMA
                    else -> ProximoPaso.EXPLORAR
                }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InicioUiState())
}
