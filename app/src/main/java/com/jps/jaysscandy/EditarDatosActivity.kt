package com.jps.jaysscandy

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarDatosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_datos)

        configurarEncabezado("Editar datos")

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        ajustarTeclado(findViewById(R.id.raiz), etNombre, etTelefono, etPassword)
        configurarVerPassword(etPassword, findViewById<ImageView>(R.id.ivVerPassword))

        findViewById<View>(R.id.btnGuardar).setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
    }
}