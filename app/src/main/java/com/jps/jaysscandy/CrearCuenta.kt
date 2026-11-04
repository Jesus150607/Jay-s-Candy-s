package com.jps.jaysscandy

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
            val password = etPassword.text.toString()
            val confirmar = etConfirmar.text.toString()

            when {
                nombre.isEmpty() -> etNombre.error = "Escribe tu nombre"
                telefono.length != 10 -> etTelefono.error = "Escribe tu número de 10 dígitos"
                spCarrera.selectedItemPosition == 0 ->
                    Toast.makeText(this, "Selecciona tu carrera", Toast.LENGTH_SHORT).show()
                spEdificio.selectedItemPosition == 0 ->
                    Toast.makeText(this, "Selecciona tu edificio", Toast.LENGTH_SHORT).show()
                password.length < 6 -> etPassword.error = "Mínimo 6 caracteres"
                password != confirmar -> etConfirmar.error = "Las contraseñas no coinciden"
                else -> {
                    val carrera = spCarrera.selectedItem.toString()
                    val edificio = spEdificio.selectedItem.toString()
                    // Aquí después guardamos en Firebase:
                    // nombre, telefono, carrera, edificio y password
                    Toast.makeText(this, "Datos válidos: $carrera · $edificio", Toast.LENGTH_SHORT).show()
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