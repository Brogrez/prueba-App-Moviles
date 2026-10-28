package cl.duoc.vitairosm.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Dispositivo {

    fun vibrar(context: Context, milisegundos: Long = 40) {
        val vibrador = obtenerVibrador(context)
        if (vibrador.hasVibrator()) {
            vibrador.vibrate(
                VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun obtenerVibrador(context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    fun llamar(context: Context, numero: String): Boolean =
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numero)))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    fun compartir(context: Context, asunto: String, texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        context.startActivity(
            Intent.createChooser(intent, asunto).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
