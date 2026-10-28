package cl.duoc.vitairosm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairosm.data.VitairosRepository
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.Usuario
import cl.duoc.vitairosm.util.Seguridad
import cl.duoc.vitairosm.validacion.Validadores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: Campo = Campo(),
    val clave: Campo = Campo(),
    val cargando: Boolean = false,
    val errorGeneral: String? = null
)

data class RegistroUsuarioUiState(
    val nombre: Campo = Campo(),
    val correo: Campo = Campo(),
    val edad: Campo = Campo(),
    val clave: Campo = Campo(),
    val confirmar: Campo = Campo(),
    val aceptaTerminos: Boolean = false,
    val errorTerminos: String? = null,
    val cargando: Boolean = false
) {
    val progreso: Float
        get() = listOf(
            nombre.valido, correo.valido, edad.valido,
            clave.valido, confirmar.valido, aceptaTerminos
        ).count { it } / 6f
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VitairosRepository(app)

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroUsuarioUiState())
    val registro: StateFlow<RegistroUsuarioUiState> = _registro.asStateFlow()

    fun onLoginCorreo(v: String) = _login.update { it.copy(correo = it.correo.escribir(v, Validadores::correo), errorGeneral = null) }
    fun onLoginCorreoBlur() = _login.update { it.copy(correo = it.correo.revisar(Validadores::correo)) }
    fun onLoginClave(v: String) = _login.update { it.copy(clave = it.clave.escribir(v, Validadores::claveLogin), errorGeneral = null) }
    fun onLoginClaveBlur() = _login.update { it.copy(clave = it.clave.revisar(Validadores::claveLogin)) }

    fun iniciarSesion(onOk: () -> Unit) {
        val correo = _login.value.correo.revisar(Validadores::correo)
        val clave = _login.value.clave.revisar(Validadores::claveLogin)
        _login.update { it.copy(correo = correo, clave = clave, errorGeneral = null) }
        if (correo.error != null || clave.error != null) return

        viewModelScope.launch {
            _login.update { it.copy(cargando = true) }
            delay(700)
            val usuario = repo.usuario.first()
            val valido = usuario != null &&
                usuario.correo == correo.valor.trim().lowercase() &&
                Seguridad.hash(clave.valor, usuario.sal) == usuario.claveHash
            if (valido) {
                repo.iniciarSesion()
                _login.value = LoginUiState()
                onOk()
            } else {
                _login.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = if (usuario == null) {
                            "No hay un perfil en este dispositivo. Crea uno de prueba."
                        } else {
                            "Correo o contraseña incorrectos"
                        }
                    )
                }
            }
        }
    }

    fun onNombre(v: String) = _registro.update { it.copy(nombre = it.nombre.escribir(v, Validadores::nombre)) }
    fun onNombreBlur() = _registro.update { it.copy(nombre = it.nombre.revisar(Validadores::nombre)) }
    fun onCorreo(v: String) = _registro.update { it.copy(correo = it.correo.escribir(v, Validadores::correo)) }
    fun onCorreoBlur() = _registro.update { it.copy(correo = it.correo.revisar(Validadores::correo)) }

    fun onEdad(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(2)
        _registro.update { it.copy(edad = it.edad.escribir(soloDigitos, Validadores::edad)) }
    }

    fun onEdadBlur() = _registro.update { it.copy(edad = it.edad.revisar(Validadores::edad)) }

    fun onClave(v: String) {
        _registro.update { s ->
            val clave = s.clave.escribir(v, Validadores::clave)
            val confirmar = if (s.confirmar.valido || s.confirmar.error != null) {
                s.confirmar.revisar { Validadores.confirmarClave(clave.valor, it) }
            } else {
                s.confirmar
            }
            s.copy(clave = clave, confirmar = confirmar)
        }
    }

    fun onClaveBlur() = _registro.update { it.copy(clave = it.clave.revisar(Validadores::clave)) }

    fun onConfirmar(v: String) {
        _registro.update { s ->
            s.copy(confirmar = s.confirmar.escribir(v) { Validadores.confirmarClave(s.clave.valor, it) })
        }
    }

    fun onConfirmarBlur() {
        _registro.update { s ->
            s.copy(confirmar = s.confirmar.revisar { Validadores.confirmarClave(s.clave.valor, it) })
        }
    }

    fun onTerminos(v: Boolean) = _registro.update { it.copy(aceptaTerminos = v, errorTerminos = null) }

    fun crearPerfil(onOk: () -> Unit) {
        val s = _registro.value
        val nombre = s.nombre.revisar(Validadores::nombre)
        val correo = s.correo.revisar(Validadores::correo)
        val edad = s.edad.revisar(Validadores::edad)
        val clave = s.clave.revisar(Validadores::clave)
        val confirmar = s.confirmar.revisar { Validadores.confirmarClave(clave.valor, it) }
        val errorTerminos = Validadores.terminos(s.aceptaTerminos)
        _registro.update {
            it.copy(nombre = nombre, correo = correo, edad = edad, clave = clave, confirmar = confirmar, errorTerminos = errorTerminos)
        }
        val hayErrores = listOf(nombre.error, correo.error, edad.error, clave.error, confirmar.error, errorTerminos).any { it != null }
        if (hayErrores) return

        viewModelScope.launch {
            _registro.update { it.copy(cargando = true) }
            delay(700)
            val sal = Seguridad.generarSal()
            repo.registrarUsuario(
                Usuario(
                    nombre = nombre.valor.trim(),
                    correo = correo.valor.trim().lowercase(),
                    claveHash = Seguridad.hash(clave.valor, sal),
                    sal = sal,
                    edad = edad.valor.toInt()
                )
            )
            _registro.value = RegistroUsuarioUiState()
            onOk()
        }
    }

    fun limpiar() {
        _login.value = LoginUiState()
        _registro.value = RegistroUsuarioUiState()
    }
}
