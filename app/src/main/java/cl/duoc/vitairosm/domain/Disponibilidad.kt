package cl.duoc.vitairosm.domain

import cl.duoc.vitairosm.model.Catalogos
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.Slot
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object Disponibilidad {

    fun fechas(dias: Int = 7, desde: LocalDate = LocalDate.now()): List<String> =
        generateSequence(desde) { it.plusDays(1) }
            .filter { it.dayOfWeek != DayOfWeek.SUNDAY }
            .take(dias)
            .map { it.toString() }
            .toList()

    fun slots(
        profesionalId: Int?,
        fecha: String?,
        citas: List<Cita>,
        ahora: LocalDateTime = LocalDateTime.now()
    ): List<Slot> {
        val profesional = Catalogos.profesional(profesionalId ?: -1) ?: return emptyList()
        if (fecha == null) return emptyList()
        val dia = LocalDate.parse(fecha)
        return profesional.horarios.map { hora ->
            val pasado = LocalDateTime.of(dia, LocalTime.parse(hora)).isBefore(ahora)
            val ocupadoSintetico =
                (profesional.id * 7 + dia.dayOfMonth * 3 + hora.take(2).toInt()) % 4 == 0
            val ocupadoPorUsuario = citas.any {
                it.estado != EstadoCita.CANCELADA &&
                    it.profesionalId == profesional.id &&
                    it.fecha == fecha &&
                    it.hora == hora
            }
            Slot(hora, !pasado && !ocupadoSintetico && !ocupadoPorUsuario)
        }
    }
}
