package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class InicioSesion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivVerPassword = findViewById<ImageView>(R.id.ivVerPassword)
        val btnIniciar = findViewById<AppCompatButton>(R.id.btnIniciarSesion)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        ajustarTeclado(findViewById(R.id.raiz), etTelefono, etPassword)
        configurarVerPassword(etPassword, ivVerPassword)

        // Sin validaciones por ahora: entra directo al Inicio
        btnIniciar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, CrearCuenta::class.java))
        }
    }
}