package cl.duoc.vitairosm.model

object Catalogos {
    val DOMINIOS = listOf("Ánimo", "Sueño", "Estrés", "Energía", "Relaciones")

    val ANIMOS = listOf(
        1 to "Muy bajo",
        2 to "Bajo",
        3 to "Neutro",
        4 to "Bien",
        5 to "Muy bien"
    )

    val PROFESIONALES = listOf(
        Profesional(1, "Dra. Ana Fuentes", "Psicología clínica", "Online", listOf("09:00", "10:30", "15:00", "16:30")),
        Profesional(2, "Ps. Tomás Vergara", "Orientación y bienestar", "Online", listOf("08:30", "11:00", "14:30", "17:00")),
        Profesional(3, "Ps. Valentina Soto", "Manejo del estrés", "Presencial (simulado)", listOf("10:00", "12:00", "15:30", "18:00")),
        Profesional(4, "Ps. Matías Lagos", "Hábitos de sueño", "Online", listOf("09:30", "13:00", "16:00", "19:00"))
    )

    fun profesional(id: Int): Profesional? = PROFESIONALES.firstOrNull { it.id == id }

    val LINEAS_AYUDA = listOf(
        LineaAyuda("Salud Responde", "600 360 7777", "6003607777", "Orientación en salud, atención las 24 horas."),
        LineaAyuda("Fono *4141", "*4141", "*4141", "Línea de prevención del suicidio."),
        LineaAyuda("SAMU", "131", "131", "Urgencias médicas.")
    )

    val RECURSOS = listOf(
        Recurso(
            "Respiración 4-4-6",
            "Una pausa breve para bajar la tensión.",
            "Inhala por la nariz contando 4, sostén el aire contando 4 y exhala lento por la boca contando 6. Repite durante 2 o 3 minutos."
        ),
        Recurso(
            "Rutina para dormir mejor",
            "Hábitos simples para descansar.",
            "Mantén horarios regulares, evita pantallas y cafeína en la noche y prepara un ambiente oscuro y tranquilo."
        ),
        Recurso(
            "Pausas para el estrés",
            "Cómo repartir la carga del día.",
            "Divide las tareas grandes en pasos pequeños, haz pausas de movimiento cada hora y anota qué situaciones te generan más tensión."
        ),
        Recurso(
            "Cómo preparar una conversación con un profesional",
            "Llega a la cita con ideas claras.",
            "Anota qué te preocupa, desde cuándo ocurre, qué te ayuda y qué lo empeora. Puedes llevar tu historial de registros."
        ),
        Recurso(
            "Cuándo pedir ayuda",
            "Señales para buscar apoyo profesional.",
            "Si el malestar dura semanas, afecta tus actividades o aparecen pensamientos de hacerte daño, busca atención profesional o llama a una línea de ayuda."
        )
    )
}
