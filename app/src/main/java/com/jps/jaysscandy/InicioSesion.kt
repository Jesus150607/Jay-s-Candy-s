package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class InicioSesion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.iniciar(this)
        setContentView(R.layout.activity_inicio_sesion)

        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivVerPassword = findViewById<ImageView>(R.id.ivVerPassword)
        val btnIniciar = findViewById<AppCompatButton>(R.id.btnIniciarSesion)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        ajustarTeclado(findViewById(R.id.raiz), etTelefono, etPassword)
        configurarVerPassword(etPassword, ivVerPassword)

        btnIniciar.setOnClickListener {
            val telefono = etTelefono.text.toString().trim()
            val password = etPassword.text.toString()

            when {
                telefono.isEmpty() -> {
                    etTelefono.error = "Escribe tu número de teléfono"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    etPassword.error = "Escribe tu contraseña"
                    return@setOnClickListener
                }
            }

            btnIniciar.isEnabled = false

            lifecycleScope.launch {
                try {
                    Graph.iniciar(this@InicioSesion)
                    when (val resultado = Graph.usuarios.iniciarSesion(telefono, password)) {
                        is ResultadoLogin.Error -> {
                            btnIniciar.isEnabled = true
                            Toast.makeText(this@InicioSesion, resultado.mensaje, Toast.LENGTH_LONG).show()
                        }
                        is ResultadoLogin.Exito -> {
                            Graph.sesion.guardar(resultado.usuario.id)

                            // Borrar el formulario
                            etTelefono.text.clear()
                            etPassword.text.clear()

                            Toast.makeText(
                                this@InicioSesion,
                                "¡Bienvenido, ${resultado.usuario.nombre}!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val destino = if (resultado.usuario.rol == Rol.DUENO) {
                                PanelVendedorActivity::class.java
                            } else {
                                MainActivity::class.java
                            }
                            startActivity(Intent(this@InicioSesion, destino))
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    btnIniciar.isEnabled = true
                    Toast.makeText(this@InicioSesion, "Error al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, CrearCuenta::class.java))
        }
    }
}