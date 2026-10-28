package cl.duoc.vitairosm.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer

object Seguridad {
    private val SENALES_RIESGO = listOf(
        "suicid", "quitarme la vida", "no quiero vivir", "no quiero seguir viviendo",
        "hacerme dano", "autolesion", "matarme", "acabar con todo"
    )

    fun generarSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.aHex()
    }

    fun hash(clave: String, sal: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((sal + clave).toByteArray(Charsets.UTF_8))
            .aHex()

    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    fun detectarSenalesDeRiesgo(texto: String): Boolean {
        val t = normalizar(texto)
        return SENALES_RIESGO.any { t.contains(it) }
    }

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }
}
