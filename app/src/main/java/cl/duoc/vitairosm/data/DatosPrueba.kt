package cl.duoc.vitairosm.data

import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.RegistroEmocional
import java.time.LocalDate

object DatosPrueba {
    private const val DIA = 86_400_000L
    private const val PREFIJO = "demo-"

    fun esDePrueba(id: String) = id.startsWith(PREFIJO)

    fun registros(ahora: Long = System.currentTimeMillis()): List<RegistroEmocional> = listOf(
        Triple(1, 4, "Sueño" to "Dormí 7 horas y desperté con energía."),
        Triple(2, 3, "Estrés" to "Semana de entregas, sentí presión pero la manejé con pausas."),
        Triple(3, 2, "Ánimo" to "Día pesado, me costó concentrarme."),
        Triple(5, 4, "Relaciones" to "Conversé con un amigo y me sentí acompañado."),
        Triple(6, 3, "Energía" to "Energía media, caminé 20 minutos."),
        Triple(7, 5, "Ánimo" to "Buen día: terminé un trabajo pendiente."),
        Triple(8, 2, "Sueño" to "Dormí poco por estudiar hasta tarde.")
    ).mapIndexed { i, (dias, animo, par) ->
        RegistroEmocional(
            id = "${PREFIJO}r${i + 1}",
            animo = animo,
            dominio = par.first,
            nota = par.second,
            fechaMillis = ahora - dias * DIA - i * 2_220_000L
        )
    }

    fun citas(ahora: Long = System.currentTimeMillis()): List<Cita> {
        val hoy = LocalDate.now()
        return listOf(
            Cita("${PREFIJO}c1", 1, hoy.plusDays(2).toString(), "10:30", "Seguimiento de estrés académico", EstadoCita.CONFIRMADA, ahora - 3_600_000L, true),
            Cita("${PREFIJO}c2", 4, hoy.minusDays(5).toString(), "16:00", "Consulta sobre hábitos de sueño", EstadoCita.CANCELADA, ahora - 6 * DIA, false)
        )
    }
}
