package com.jps.jaysscandy

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Hash de contraseñas con PBKDF2 + sal aleatoria. Nunca se guarda la contraseña en claro. */
object Seguridad {
    private const val ALGORITMO = "PBKDF2WithHmacSHA256" // tu minSdk 33 lo soporta
    private const val ITERACIONES = 100_000
    private const val BITS = 256

    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salBase64: String): String {
        val sal = Base64.decode(salBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), sal, ITERACIONES, BITS)
        val bytes = SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun verificar(password: String, salBase64: String, hashGuardado: String): Boolean =
        MessageDigest.isEqual(
            hash(password, salBase64).toByteArray(),
            hashGuardado.toByteArray()
        )
}

enum class Campo { NOMBRE, TELEFONO, CARRERA, EDIFICIO, PASSWORD, CONFIRMAR }

/** carrera y edificio llegan vacíos ("") si el usuario dejó la opción "Selecciona..." */
data class DatosRegistro(
    val nombre: String,
    val telefono: String,
    val carrera: String,
    val edificio: String,
    val password: String,
    val confirmar: String
)

object Validador {
    private val NOMBRE = Regex("^[\\p{L}][\\p{L} .'-]*$")
    private const val DIGITOS_TELEFONO = 10
    const val MIN_PASSWORD = 6 // igual que el hint de tu pantalla ("Mínimo 6 caracteres")

    fun normalizarTelefono(telefono: String) = telefono.filter { it.isDigit() }

    /** Devuelve un mapa Campo -> mensaje. Si está vacío, los datos son válidos. */
    fun validarRegistro(d: DatosRegistro): Map<Campo, String> {
        val e = mutableMapOf<Campo, String>()

        val nombre = d.nombre.trim()
        when {
            nombre.isEmpty() -> e[Campo.NOMBRE] = "Escribe tu nombre"
            nombre.length < 3 -> e[Campo.NOMBRE] = "El nombre es muy corto"
            !NOMBRE.matches(nombre) -> e[Campo.NOMBRE] = "El nombre solo puede llevar letras"
        }

        val telCrudo = d.telefono.trim()
        when {
            telCrudo.isEmpty() -> e[Campo.TELEFONO] = "Escribe tu número de teléfono"
            telCrudo.any { !it.isDigit() && it != ' ' && it != '-' } ->
                e[Campo.TELEFONO] = "Solo números"
            normalizarTelefono(telCrudo).length != DIGITOS_TELEFONO ->
                e[Campo.TELEFONO] = "Escribe tu número de $DIGITOS_TELEFONO dígitos"
        }

        if (d.carrera.isBlank()) e[Campo.CARRERA] = "Selecciona tu carrera"
        if (d.edificio.isBlank()) e[Campo.EDIFICIO] = "Selecciona tu edificio"

        val p = d.password
        when {
            p.isEmpty() -> e[Campo.PASSWORD] = "Escribe una contraseña"
            p.length < MIN_PASSWORD -> e[Campo.PASSWORD] = "Mínimo $MIN_PASSWORD caracteres"
            p.any { it.isWhitespace() } -> e[Campo.PASSWORD] = "No puede llevar espacios"
        }

        when {
            d.confirmar.isEmpty() -> e[Campo.CONFIRMAR] = "Confirma tu contraseña"
            d.confirmar != p -> e[Campo.CONFIRMAR] = "Las contraseñas no coinciden"
        }

        return e
    }
}
