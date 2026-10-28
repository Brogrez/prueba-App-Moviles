package cl.duoc.vitairosm.data

import cl.duoc.vitairosm.model.AccionSugerida
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.MensajeChat
import cl.duoc.vitairosm.model.RegistroEmocional
import cl.duoc.vitairosm.model.Usuario
import org.json.JSONArray
import org.json.JSONObject

private fun <T> List<T>.aArreglo(convertir: (T) -> JSONObject): String {
    val arreglo = JSONArray()
    forEach { arreglo.put(convertir(it)) }
    return arreglo.toString()
}

private fun <T> leerLista(texto: String?, convertir: (JSONObject) -> T): List<T> {
    if (texto.isNullOrBlank()) return emptyList()
    return runCatching {
        val arreglo = JSONArray(texto)
        (0 until arreglo.length()).map { convertir(arreglo.getJSONObject(it)) }
    }.getOrDefault(emptyList())
}

internal fun usuarioAJson(u: Usuario): String = JSONObject()
    .put("nombre", u.nombre)
    .put("correo", u.correo)
    .put("claveHash", u.claveHash)
    .put("sal", u.sal)
    .put("edad", u.edad)
    .toString()

internal fun leerUsuario(texto: String?): Usuario? {
    if (texto.isNullOrBlank()) return null
    return runCatching {
        val o = JSONObject(texto)
        Usuario(o.getString("nombre"), o.getString("correo"), o.getString("claveHash"), o.getString("sal"), o.getInt("edad"))
    }.getOrNull()
}

internal fun registrosAJson(lista: List<RegistroEmocional>): String = lista.aArreglo {
    JSONObject()
        .put("id", it.id)
        .put("animo", it.animo)
        .put("dominio", it.dominio)
        .put("nota", it.nota)
        .put("fechaMillis", it.fechaMillis)
}

internal fun leerRegistros(texto: String?): List<RegistroEmocional> = leerLista(texto) {
    RegistroEmocional(it.getString("id"), it.getInt("animo"), it.getString("dominio"), it.getString("nota"), it.getLong("fechaMillis"))
}

internal fun citasAJson(lista: List<Cita>): String = lista.aArreglo {
    JSONObject()
        .put("id", it.id)
        .put("profesionalId", it.profesionalId)
        .put("fecha", it.fecha)
        .put("hora", it.hora)
        .put("motivo", it.motivo)
        .put("estado", it.estado.name)
        .put("creadaMillis", it.creadaMillis)
        .put("recordatorio", it.recordatorio)
}

internal fun leerCitas(texto: String?): List<Cita> = leerLista(texto) {
    Cita(
        it.getString("id"),
        it.getInt("profesionalId"),
        it.getString("fecha"),
        it.getString("hora"),
        it.getString("motivo"),
        EstadoCita.valueOf(it.getString("estado")),
        it.getLong("creadaMillis"),
        it.optBoolean("recordatorio", false)
    )
}

internal fun chatAJson(lista: List<MensajeChat>): String = lista.aArreglo {
    JSONObject()
        .put("id", it.id)
        .put("esUsuario", it.esUsuario)
        .put("texto", it.texto)
        .put("fechaMillis", it.fechaMillis)
        .put("accion", it.accion.name)
}

internal fun leerChat(texto: String?): List<MensajeChat> = leerLista(texto) {
    MensajeChat(
        it.getString("id"),
        it.getBoolean("esUsuario"),
        it.getString("texto"),
        it.getLong("fechaMillis"),
        runCatching { AccionSugerida.valueOf(it.optString("accion", "NINGUNA")) }.getOrDefault(AccionSugerida.NINGUNA)
    )
}
