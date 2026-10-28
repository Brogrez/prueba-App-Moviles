package cl.duoc.vitairosm

import cl.duoc.vitairosm.domain.Disponibilidad
import cl.duoc.vitairosm.domain.MotorOrientacion
import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.util.Seguridad
import cl.duoc.vitairosm.validacion.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ValidadoresTest {

    @Test
    fun nombre_valido_e_invalido() {
        assertNull(Validadores.nombre("Camila Rojas"))
        assertNotNull(Validadores.nombre(""))
        assertNotNull(Validadores.nombre("Ab"))
        assertNotNull(Validadores.nombre("Cami123"))
    }

    @Test
    fun correo_y_edad() {
        assertNull(Validadores.correo("test@vitairos.cl"))
        assertNotNull(Validadores.correo("sin-arroba"))
        assertNull(Validadores.edad("25"))
        assertNotNull(Validadores.edad("17"))
        assertNotNull(Validadores.edad(""))
    }

    @Test
    fun clave_y_confirmacion() {
        assertNull(Validadores.clave("abc12345"))
        assertNotNull(Validadores.clave("abcdefgh"))
        assertNotNull(Validadores.clave("12345678"))
        assertNotNull(Validadores.clave("a1"))
        assertNull(Validadores.confirmarClave("abc12345", "abc12345"))
        assertNotNull(Validadores.confirmarClave("abc12345", "otra"))
    }

    @Test
    fun registro_emocional_y_hora() {
        assertNotNull(Validadores.animo(null))
        assertNull(Validadores.animo(3))
        assertNotNull(Validadores.nota("hola"))
        assertNull(Validadores.nota("Me siento bien"))
        assertNull(Validadores.hora("20:30"))
        assertNotNull(Validadores.hora("25:00"))
    }

    @Test
    fun campo_valida_en_vivo_solo_tras_primer_error() {
        val sinTocar = Campo().escribir("a", Validadores::nombre)
        assertNull(sinTocar.error)
        val revisado = sinTocar.revisar(Validadores::nombre)
        assertNotNull(revisado.error)
        assertNull(revisado.escribir("Camila", Validadores::nombre).error)
    }

    @Test
    fun hash_es_determinista_y_depende_de_la_sal() {
        assertEquals(Seguridad.hash("clave1234", "sal"), Seguridad.hash("clave1234", "sal"))
        assertFalse(Seguridad.hash("clave1234", "sal1") == Seguridad.hash("clave1234", "sal2"))
    }

    @Test
    fun deteccion_de_riesgo_y_respuesta_de_ayuda() {
        assertTrue(Seguridad.detectarSenalesDeRiesgo("Quiero hacerme daño"))
        assertFalse(Seguridad.detectarSenalesDeRiesgo("Hoy dormí bien"))
        assertEquals(AccionSugerida.AYUDA, MotorOrientacion.responder("no quiero vivir más").accion)
        assertEquals(AccionSugerida.AGENDA, MotorOrientacion.responder("quiero agendar una cita").accion)
    }

    @Test
    fun disponibilidad_excluye_domingos_y_horas_pasadas() {
        val fechas = Disponibilidad.fechas(7, LocalDate.of(2026, 11, 1))
        assertEquals(7, fechas.size)
        assertFalse(fechas.contains("2026-11-01"))
        val slots = Disponibilidad.slots(1, "2026-11-02", emptyList(), LocalDateTime.of(2026, 11, 2, 23, 0))
        assertTrue(slots.none { it.disponible })
    }
}
