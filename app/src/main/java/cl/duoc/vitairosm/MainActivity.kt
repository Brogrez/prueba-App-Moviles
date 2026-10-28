package cl.duoc.vitairosm

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.vitairosm.navigation.AppNavigation
import cl.duoc.vitairosm.ui.theme.VitairosTheme
import cl.duoc.vitairosm.util.Notificaciones
import cl.duoc.vitairosm.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notificaciones.crearCanal(this)
        setContent {
            val sesionVm: SesionViewModel = viewModel()
            val estado by sesionVm.estado.collectAsState()
            val oscuro = estado.prefs.modoOscuro

            DisposableEffect(oscuro) {
                val estilo = if (oscuro) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose {}
            }

            VitairosTheme(darkTheme = oscuro) {
                if (estado.cargado) {
                    AppNavigation(sesionVm)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
