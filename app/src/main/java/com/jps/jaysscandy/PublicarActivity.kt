package com.jps.jaysscandy

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PublicarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar)

        val editar = intent.getBooleanExtra("editar", false)
        configurarEncabezado(if (editar) "Editar dulce" else "Publicar dulce")

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)
        val etPresentacion = findViewById<EditText>(R.id.etPresentacion)
        val spCategoria = findViewById<Spinner>(R.id.spCategoria)
        val btnPublicar = findViewById<AppCompatButton>(R.id.btnPublicar)

        val categorias = resources.getStringArray(R.array.categorias)
        spCategoria.adapter = crearAdaptadorSpinner(categorias)
        findViewById<View>(R.id.llCategoria).setOnClickListener { spCategoria.performClick() }

        ajustarTeclado(findViewById(R.id.raiz), etNombre, etPrecio, etPresentacion)

        if (editar) {
            etNombre.setText(intent.getStringExtra("nombre"))
            etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
            etPresentacion.setText(intent.getStringExtra("presentacion"))
            spCategoria.setSelection(
                categorias.indexOf(intent.getStringExtra("categoria")).coerceAtLeast(0)
            )
            btnPublicar.text = "Guardar cambios"
        }

        findViewById<View>(R.id.btnFoto).setOnClickListener {
            Toast.makeText(this, "Subir foto: próximamente", Toast.LENGTH_SHORT).show()
        }
        btnPublicar.setOnClickListener {
            Toast.makeText(
                this,
                if (editar) "Cambios guardados" else "Dulce publicado",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
    }
}