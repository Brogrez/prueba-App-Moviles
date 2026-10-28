package cl.duoc.vitairosm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.vitairosm.model.Cita
import cl.duoc.vitairosm.model.EstadoCita
import cl.duoc.vitairosm.model.MensajeChat
import cl.duoc.vitairosm.model.PreferenciasApp
import cl.duoc.vitairosm.model.RegistroEmocional
import cl.duoc.vitairosm.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vitairos_prefs")

class VitairosRepository(context: Context) {

    private val ds = context.applicationContext.dataStore

    private object K {
        val ONBOARDING = booleanPreferencesKey("onboarding")
        val SESION = booleanPreferencesKey("sesion")
        val OSCURO = booleanPreferencesKey("modo_oscuro")
        val RECORDATORIOS = booleanPreferencesKey("recordatorios")
        val HORA = stringPreferencesKey("hora_recordatorio")
        val USUARIO = stringPreferencesKey("usuario")
        val REGISTROS = stringPreferencesKey("registros")
        val CITAS = stringPreferencesKey("citas")
        val CHAT = stringPreferencesKey("chat")
    }

    private val datos: Flow<Preferences> = ds.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val preferencias: Flow<PreferenciasApp> = datos.map { p ->
        PreferenciasApp(
            onboardingAceptado = p[K.ONBOARDING] ?: false,
            sesionActiva = p[K.SESION] ?: false,
            modoOscuro = p[K.OSCURO] ?: false,
            recordatorios = p[K.RECORDATORIOS] ?: false,
            horaRecordatorio = p[K.HORA] ?: "20:00"
        )
    }

    val usuario: Flow<Usuario?> = datos.map { leerUsuario(it[K.USUARIO]) }

    val registros: Flow<List<RegistroEmocional>> =
        datos.map { leerRegistros(it[K.REGISTROS]).sortedByDescending { r -> r.fechaMillis } }

    val citas: Flow<List<Cita>> =
        datos.map { leerCitas(it[K.CITAS]).sortedWith(compareBy<Cita>({ c -> c.fecha }, { c -> c.hora })) }

    val chat: Flow<List<MensajeChat>> = datos.map { leerChat(it[K.CHAT]) }

    suspend fun aceptarOnboarding() {
        ds.edit { it[K.ONBOARDING] = true }
    }

    suspend fun registrarUsuario(usuario: Usuario) {
        ds.edit {
            it[K.USUARIO] = usuarioAJson(usuario)
            it[K.SESION] = true
        }
    }

    suspend fun iniciarSesion() {
        ds.edit { it[K.SESION] = true }
    }

    suspend fun cerrarSesion() {
        ds.edit { it[K.SESION] = false }
    }

    suspend fun guardarModoOscuro(activado: Boolean) {
        ds.edit { it[K.OSCURO] = activado }
    }

    suspend fun guardarRecordatorios(activado: Boolean) {
        ds.edit { it[K.RECORDATORIOS] = activado }
    }

    suspend fun guardarHora(hora: String) {
        ds.edit { it[K.HORA] = hora }
    }

    suspend fun agregarRegistro(registro: RegistroEmocional) {
        ds.edit { p -> p[K.REGISTROS] = registrosAJson(leerRegistros(p[K.REGISTROS]) + registro) }
    }

    suspend fun eliminarRegistro(id: String) {
        ds.edit { p -> p[K.REGISTROS] = registrosAJson(leerRegistros(p[K.REGISTROS]).filterNot { it.id == id }) }
    }

    suspend fun agregarCita(cita: Cita) {
        ds.edit { p -> p[K.CITAS] = citasAJson(leerCitas(p[K.CITAS]) + cita) }
    }

    suspend fun actualizarEstadoCita(id: String, estado: EstadoCita) {
        ds.edit { p ->
            p[K.CITAS] = citasAJson(leerCitas(p[K.CITAS]).map { if (it.id == id) it.copy(estado = estado) else it })
        }
    }

    suspend fun agregarMensaje(mensaje: MensajeChat) {
        ds.edit { p -> p[K.CHAT] = chatAJson((leerChat(p[K.CHAT]) + mensaje).takeLast(100)) }
    }

    suspend fun reiniciarChat() {
        ds.edit { it.remove(K.CHAT) }
    }

    suspend fun cargarDatosPrueba() {
        ds.edit { p ->
            val registros = leerRegistros(p[K.REGISTROS]).filterNot { DatosPrueba.esDePrueba(it.id) }
            val citas = leerCitas(p[K.CITAS]).filterNot { DatosPrueba.esDePrueba(it.id) }
            p[K.REGISTROS] = registrosAJson(registros + DatosPrueba.registros())
            p[K.CITAS] = citasAJson(citas + DatosPrueba.citas())
        }
    }

    suspend fun borrarDatos() {
        ds.edit {
            it.remove(K.REGISTROS)
            it.remove(K.CITAS)
            it.remove(K.CHAT)
        }
    }
}
