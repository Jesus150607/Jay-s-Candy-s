package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CrearCuenta : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val spCarrera = findViewById<Spinner>(R.id.spCarrera)
        val spEdificio = findViewById<Spinner>(R.id.spEdificio)
        val llCarrera = findViewById<LinearLayout>(R.id.llCarrera)
        val llEdificio = findViewById<LinearLayout>(R.id.llEdificio)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmar = findViewById<EditText>(R.id.etConfirmar)
        val ivVerPassword = findViewById<ImageView>(R.id.ivVerPassword)
        val ivVerConfirmar = findViewById<ImageView>(R.id.ivVerConfirmar)
        val btnRegistrarme = findViewById<AppCompatButton>(R.id.btnRegistrarme)
        val btnAtras = findViewById<ImageView>(R.id.btnAtras)
        val tvIniciarSesion = findViewById<TextView>(R.id.tvIniciarSesion)

        // Listas desplegables
        spCarrera.adapter = crearAdaptador(resources.getStringArray(R.array.carreras))
        spEdificio.adapter = crearAdaptador(resources.getStringArray(R.array.edificios))

        // Que todo el campo abra la lista, no solo el texto
        llCarrera.setOnClickListener { spCarrera.performClick() }
        llEdificio.setOnClickListener { spEdificio.performClick() }

        // Teclado y dulces para ver contraseñas
        ajustarTeclado(findViewById(R.id.raiz), etNombre, etTelefono, etPassword, etConfirmar)
        configurarVerPassword(etPassword, ivVerPassword)
        configurarVerPassword(etConfirmar, ivVerConfirmar)

        btnAtras.setOnClickListener { finish() }
        tvIniciarSesion.setOnClickListener { finish() }

        btnRegistrarme.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val carrera = if (spCarrera.selectedItemPosition == 0) "" else spCarrera.selectedItem.toString()
            val edificio = if (spEdificio.selectedItemPosition == 0) "" else spEdificio.selectedItem.toString()
            val password = etPassword.text.toString()
            val confirmar = etConfirmar.text.toString()

            val datos = DatosRegistro(
                nombre = nombre,
                telefono = telefono,
                carrera = carrera,
                edificio = edificio,
                password = password,
                confirmar = confirmar
            )

            val errores = Validador.validarRegistro(datos)
            if (errores.isNotEmpty()) {
                errores[Campo.NOMBRE]?.let { etNombre.error = it }
                errores[Campo.TELEFONO]?.let { etTelefono.error = it }
                errores[Campo.CARRERA]?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                errores[Campo.EDIFICIO]?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                errores[Campo.PASSWORD]?.let { etPassword.error = it }
                errores[Campo.CONFIRMAR]?.let { etConfirmar.error = it }
                return@setOnClickListener
            }

            btnRegistrarme.isEnabled = false

            lifecycleScope.launch {
                try {
                    Graph.iniciar(this@CrearCuenta)
                    when (val resultado = Graph.usuarios.registrar(datos)) {
                        is ResultadoRegistro.Errores -> {
                            btnRegistrarme.isEnabled = true
                            resultado.porCampo[Campo.NOMBRE]?.let { etNombre.error = it }
                            resultado.porCampo[Campo.TELEFONO]?.let { etTelefono.error = it }
                            resultado.porCampo[Campo.CARRERA]?.let { Toast.makeText(this@CrearCuenta, it, Toast.LENGTH_SHORT).show() }
                            resultado.porCampo[Campo.EDIFICIO]?.let { Toast.makeText(this@CrearCuenta, it, Toast.LENGTH_SHORT).show() }
                            resultado.porCampo[Campo.PASSWORD]?.let { etPassword.error = it }
                            resultado.porCampo[Campo.CONFIRMAR]?.let { etConfirmar.error = it }
                        }
                        is ResultadoRegistro.Exito -> {
                            // Borrar el formulario
                            etNombre.text.clear()
                            etTelefono.text.clear()
                            spCarrera.setSelection(0)
                            spEdificio.setSelection(0)
                            etPassword.text.clear()
                            etConfirmar.text.clear()

                            Toast.makeText(
                                this@CrearCuenta,
                                "¡Cuenta creada exitosamente! Por favor inicia sesión.",
                                Toast.LENGTH_LONG
                            ).show()

                            // Mandar a iniciar sesión
                            val intent = Intent(this@CrearCuenta, InicioSesion::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    btnRegistrarme.isEnabled = true
                    Toast.makeText(this@CrearCuenta, "Error al crear cuenta: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // El primer elemento ("Selecciona...") se ve en gris como texto guía
    private fun crearAdaptador(opciones: Array<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, R.layout.item_spinner, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val vista = super.getView(position, convertView, parent) as TextView
                pintar(vista, position)
                return vista
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val vista = super.getDropDownView(position, convertView, parent) as TextView
                pintar(vista, position)
                return vista
            }

            private fun pintar(vista: TextView, position: Int) {
                vista.setTextColor(
                    getColor(if (position == 0) R.color.texto_hint else R.color.azul_oscuro)
                )
            }
        }
    }
}