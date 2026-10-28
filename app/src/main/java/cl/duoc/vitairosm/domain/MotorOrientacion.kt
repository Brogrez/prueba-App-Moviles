package cl.duoc.vitairosm.domain

import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.util.Seguridad

object MotorOrientacion {

    data class Respuesta(val texto: String, val accion: AccionSugerida)

    val SUGERENCIAS = listOf(
        "Me siento ansioso/a",
        "No puedo dormir bien",
        "Estoy muy estresado/a",
        "Quiero agendar una cita",
        "¿Qué puedes hacer?"
    )

    fun bienvenida() = Respuesta(
        "Hola, soy el asistente de orientación de VITAIROS-M. Puedo ayudarte a ordenar lo que sientes y sugerirte un próximo paso. " +
            "No entrego diagnósticos ni reemplazo a un profesional. ¿Cómo te sientes hoy?",
        AccionSugerida.NINGUNA
    )

    fun responder(entrada: String): Respuesta {
        val t = Seguridad.normalizar(entrada)
        return when {
            Seguridad.detectarSenalesDeRiesgo(entrada) -> Respuesta(
                "Lamento que estés pasando por algo tan difícil. No tienes que enfrentarlo solo/a. " +
                    "Esta aplicación no reemplaza atención profesional: puedes comunicarte ahora con una línea de ayuda.",
                AccionSugerida.AYUDA
            )
            t.contieneAlguna("ansio", "nervios", "panico", "angustia", "preocup") -> Respuesta(
                "Gracias por contarlo. Cuando aparece la ansiedad puede ayudar una pausa de respiración lenta. " +
                    "También sirve dejar registro de qué la dispara para detectar patrones.",
                AccionSugerida.RECURSOS
            )
            t.contieneAlguna("dormir", "sueno", "insomnio", "desvel") -> Respuesta(
                "Dormir poco afecta el ánimo y la energía. Te sugiero registrar tu sueño unos días para ver cómo evoluciona.",
                AccionSugerida.REGISTRO
            )
            t.contieneAlguna("estres", "agobi", "presion", "sobrecarg", "cansad") -> Respuesta(
                "Suena a una carga importante. Dividir tareas y hacer pausas breves puede aliviar. " +
                    "Si quieres, deja un registro del día para seguir cómo cambia.",
                AccionSugerida.REGISTRO
            )
            t.contieneAlguna("cita", "agendar", "profesional", "psicolog", "hora") -> Respuesta(
                "Puedo llevarte a la agenda para que elijas un profesional y un horario disponible.",
                AccionSugerida.AGENDA
            )
            t.contieneAlguna("recurso", "informacion", "tecnica", "respira", "ayuda") -> Respuesta(
                "Tengo recursos informativos de prueba, como técnicas de respiración y rutinas de sueño.",
                AccionSugerida.RECURSOS
            )
            t.contieneAlguna("puedes", "funciona", "que haces", "para que sirves") -> Respuesta(
                "Puedes registrar cómo te sientes, ver tu historial, agendar citas simuladas con profesionales y revisar recursos. " +
                    "Todo usa datos de prueba.",
                AccionSugerida.NINGUNA
            )
            t.contieneAlguna("gracias") -> Respuesta(
                "Con gusto. Aquí estaré cuando quieras continuar.",
                AccionSugerida.NINGUNA
            )
            else -> Respuesta(
                "Te escucho. Para llevar un seguimiento, un buen siguiente paso es registrar cómo te sientes hoy.",
                AccionSugerida.REGISTRO
            )
        }
    }

    private fun String.contieneAlguna(vararg claves: String): Boolean = claves.any { contains(it) }
}
