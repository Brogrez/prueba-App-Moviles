package cl.duoc.vitairosm.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

object Formato {
    private val locale: Locale = Locale.forLanguageTag("es-CL")

    fun fechaHora(millis: Long): String =
        SimpleDateFormat("d MMM yyyy, HH:mm", locale).format(Date(millis))

    fun fechaCorta(iso: String): String =
        LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE d MMM", locale))

    fun hoyLegible(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale))
            .replaceFirstChar { it.uppercase() }

    fun aFecha(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    fun inicialDia(dia: LocalDate): String =
        dia.dayOfWeek.getDisplayName(TextStyle.NARROW, locale).uppercase()

    fun codigoCorto(id: String): String = id.takeLast(6).uppercase()
}
