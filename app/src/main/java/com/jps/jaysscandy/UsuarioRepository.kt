package com.jps.jaysscandy

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro
    data class Errores(val porCampo: Map<Campo, String>) : ResultadoRegistro
}

sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin
}

/** Las pantallas y ViewModels solo conocen esta interfaz. En la rama nube se hace otra implementación. */
interface UsuarioRepository {
    suspend fun registrar(datos: DatosRegistro): ResultadoRegistro
    suspend fun iniciarSesion(telefono: String, password: String): ResultadoLogin
    suspend fun obtenerPorId(id: String): Usuario?
    suspend fun actualizar(usuario: Usuario)
    suspend fun asegurarDueno()
}

class UsuarioRepositoryRoom(private val dao: UsuarioDao) : UsuarioRepository {

    override suspend fun registrar(datos: DatosRegistro): ResultadoRegistro =
        withContext(Dispatchers.Default) {
            val errores = Validador.validarRegistro(datos).toMutableMap()
            val telefono = Validador.normalizarTelefono(datos.telefono)

            // Duplicado (solo se consulta si el formato ya es válido)
            if (Campo.TELEFONO !in errores && dao.porTelefono(telefono) != null) {
                errores[Campo.TELEFONO] = "Este número ya está registrado"
            }
            if (errores.isNotEmpty()) return@withContext ResultadoRegistro.Errores(errores)

            val sal = Seguridad.nuevaSal()
            val usuario = Usuario(
                nombre = datos.nombre.trim(),
                telefono = telefono,
                carrera = datos.carrera.trim(),
                edificio = datos.edificio.trim(),
                passwordHash = Seguridad.hash(datos.password, sal),
                salt = sal,
                rol = Rol.USUARIO // el registro SIEMPRE crea usuarios normales
            )

            try {
                dao.insertar(usuario)
                ResultadoRegistro.Exito(usuario)
            } catch (e: SQLiteConstraintException) {
                // Red de seguridad: el índice único de la BD frena cualquier duplicado
                ResultadoRegistro.Errores(mapOf(Campo.TELEFONO to "Este número ya está registrado"))
            }
        }

    override suspend fun iniciarSesion(telefono: String, password: String): ResultadoLogin =
        withContext(Dispatchers.Default) {
            val tel = Validador.normalizarTelefono(telefono)
            if (tel.isEmpty() || password.isEmpty()) {
                return@withContext ResultadoLogin.Error("Escribe tu teléfono y contraseña")
            }
            val usuario = dao.porTelefono(tel)
            // Mismo mensaje en ambos casos para no revelar qué números están registrados
            if (usuario == null || !Seguridad.verificar(password, usuario.salt, usuario.passwordHash)) {
                ResultadoLogin.Error("Teléfono o contraseña incorrectos")
            } else {
                ResultadoLogin.Exito(usuario)
            }
        }

    override suspend fun obtenerPorId(id: String): Usuario? = dao.porId(id)

    override suspend fun actualizar(usuario: Usuario) = withContext(Dispatchers.Default) {
        dao.actualizar(usuario)
    }

    /** Crea / asegura los datos del único dueño predefinido. */
    override suspend fun asegurarDueno() = withContext(Dispatchers.Default) {
        val telDueno = "5576214366"
        val dueno = dao.porTelefono(telDueno)
        val sal = dueno?.salt ?: Seguridad.nuevaSal()
        val duenoUsuario = Usuario(
            id = dueno?.id ?: java.util.UUID.randomUUID().toString(),
            nombre = "Jose Romulo Sosa Ortiz",
            telefono = telDueno,
            carrera = "Licenciatura en Tecnologías de la Información e Innovación Digital",
            edificio = "Queso",
            passwordHash = Seguridad.hash("180324", sal),
            salt = sal,
            rol = Rol.DUENO
        )

        if (dueno == null) {
            val viejo1 = dao.porTelefono("5500000000")
            val viejo2 = dao.porTelefono("5598765432")
            when {
                viejo2 != null -> dao.actualizar(duenoUsuario.copy(id = viejo2.id))
                viejo1 != null -> dao.actualizar(duenoUsuario.copy(id = viejo1.id))
                else -> dao.insertar(duenoUsuario)
            }
        } else {
            dao.actualizar(duenoUsuario)
        }
    }
}
