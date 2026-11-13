package com.jps.jaysscandy

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Guarda solo el id del usuario con sesión iniciada. El rol se consulta en la BD. */
class SesionManager(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardar(usuarioId: String) = prefs.edit().putString(CLAVE_ID, usuarioId).apply()

    fun usuarioId(): String? = prefs.getString(CLAVE_ID, null)

    fun cerrar() = prefs.edit().remove(CLAVE_ID).apply()

    private companion object {
        const val CLAVE_ID = "usuario_id"
    }
}

/**
 * Punto único donde se elige la implementación.
 * En la rama nube, aquí se cambia UsuarioRepositoryRoom por la de Firebase y nada más.
 */
object Graph {
    lateinit var usuarios: UsuarioRepository
        private set
    lateinit var sesion: SesionManager
        private set
    lateinit var productos: ProductoRepository
        private set

    fun iniciar(context: Context) {
        if (::usuarios.isInitialized && ::sesion.isInitialized && ::productos.isInitialized) return
        val appContext = context.applicationContext
        val bd = AppDatabase.obtener(appContext)
        usuarios = UsuarioRepositoryRoom(bd.usuarioDao())
        sesion = SesionManager(appContext)
        productos = ProductoRepositoryRoom(bd.productoDao())
    }
}

class JaysCandysApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.iniciar(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Graph.usuarios.asegurarDueno()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
