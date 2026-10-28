package cl.duoc.vitairosm.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val claveHash: String,
    val sal: String,
    val edad: Int
)

data class RegistroEmocional(
    val id: String,
    val animo: Int,
    val dominio: String,
    val nota: String,
    val fechaMillis: Long
)

enum class EstadoCita(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada")
}

data class Cita(
    val id: String,
    val profesionalId: Int,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: EstadoCita,
    val creadaMillis: Long,
    val recordatorio: Boolean
)

data class Profesional(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val modalidad: String,
    val horarios: List<String>
)

enum class AccionSugerida { NINGUNA, REGISTRO, RECURSOS, AGENDA, AYUDA }

data class MensajeChat(
    val id: String,
    val esUsuario: Boolean,
    val texto: String,
    val fechaMillis: Long,
    val accion: AccionSugerida
)

data class PreferenciasApp(
    val onboardingAceptado: Boolean = false,
    val sesionActiva: Boolean = false,
    val modoOscuro: Boolean = false,
    val recordatorios: Boolean = false,
    val horaRecordatorio: String = "20:00"
)

data class LineaAyuda(
    val nombre: String,
    val visible: String,
    val numero: String,
    val descripcion: String
)

data class Recurso(
    val titulo: String,
    val resumen: String,
    val detalle: String
)

data class Slot(val hora: String, val disponible: Boolean)

data class Campo(
    val valor: String = "",
    val error: String? = null,
    val valido: Boolean = false
) {
    fun escribir(nuevo: String, validador: (String) -> String?): Campo {
        return if (error != null || valido) {
            val e = validador(nuevo)
            Campo(nuevo, e, e == null)
        } else {
            copy(valor = nuevo)
        }
    }

    fun revisar(validador: (String) -> String?): Campo {
        val e = validador(valor)
        return copy(error = e, valido = e == null)
    }
}
