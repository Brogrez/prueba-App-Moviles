package cl.duoc.vitairosm.validacion

object Validadores {
    const val NOTA_MAX = 300
    const val MOTIVO_MAX = 120
    const val MENSAJE_MAX = 200
    const val EDAD_MIN = 18
    const val EDAD_MAX = 99

    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val HORA = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun nombre(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "El nombre es obligatorio"
            t.length < 3 -> "Debe tener al menos 3 caracteres"
            t.length > 40 -> "Máximo 40 caracteres"
            !t.all { it.isLetter() || it == ' ' } -> "Solo letras y espacios"
            else -> null
        }
    }

    fun correo(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "El correo es obligatorio"
            !CORREO.matches(t) -> "Formato de correo no válido"
            else -> null
        }
    }

    fun edad(v: String): String? {
        val n = v.toIntOrNull()
        return when {
            v.isBlank() -> "La edad es obligatoria"
            n == null -> "Ingresa solo números"
            n < EDAD_MIN -> "Debes ser mayor de $EDAD_MIN años"
            n > EDAD_MAX -> "Ingresa una edad válida"
            else -> null
        }
    }

    fun clave(v: String): String? = when {
        v.isEmpty() -> "La contraseña es obligatoria"
        v.length < 8 -> "Mínimo 8 caracteres"
        v.none { it.isLetter() } -> "Incluye al menos una letra"
        v.none { it.isDigit() } -> "Incluye al menos un número"
        else -> null
    }

    fun claveLogin(v: String): String? = if (v.isEmpty()) "Ingresa tu contraseña" else null

    fun confirmarClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite la contraseña"
        confirmacion != clave -> "Las contraseñas no coinciden"
        else -> null
    }

    fun terminos(aceptado: Boolean): String? =
        if (aceptado) null else "Debes aceptar para continuar"

    fun animo(v: Int?): String? =
        if (v == null || v !in 1..5) "Selecciona cómo te sientes" else null

    fun dominio(v: String): String? =
        if (v.isBlank()) "Elige un área de seguimiento" else null

    fun nota(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "Escribe una breve nota"
            t.length < 5 -> "Escribe al menos 5 caracteres"
            t.length > NOTA_MAX -> "Máximo $NOTA_MAX caracteres"
            else -> null
        }
    }

    fun motivo(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "Indica el motivo de la cita"
            t.length < 5 -> "Describe un poco más el motivo"
            t.length > MOTIVO_MAX -> "Máximo $MOTIVO_MAX caracteres"
            else -> null
        }
    }

    fun mensaje(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "Escribe un mensaje"
            t.length > MENSAJE_MAX -> "Máximo $MENSAJE_MAX caracteres"
            else -> null
        }
    }

    fun hora(v: String): String? = when {
        v.isBlank() -> "Ingresa una hora"
        !HORA.matches(v) -> "Usa el formato HH:MM (24 horas)"
        else -> null
    }

    fun seleccion(v: String?, mensaje: String): String? =
        if (v.isNullOrBlank()) mensaje else null
}
