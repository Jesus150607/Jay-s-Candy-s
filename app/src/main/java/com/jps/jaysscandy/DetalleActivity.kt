package com.jps.jaysscandy

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class DetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        ajustarBarraSistema(findViewById(R.id.raiz))

        val nombre = intent.getStringExtra("nombre") ?: ""
        val precio = intent.getDoubleExtra("precio", 0.0)
        var favorito = intent.getBooleanExtra("favorito", false)
        var cantidad = 1

        findViewById<View>(R.id.flImagen).setBackgroundResource(
            intent.getIntExtra("fondo", R.drawable.bg_cat_galletas)
        )
        findViewById<TextView>(R.id.tvEmoji).text = intent.getStringExtra("emoji")
        findViewById<TextView>(R.id.tvCategoria).text = intent.getStringExtra("categoria")
        findViewById<TextView>(R.id.tvNombre).text = nombre
        findViewById<TextView>(R.id.tvPresentacion).text = intent.getStringExtra("presentacion")
        findViewById<TextView>(R.id.tvPrecio).text = precio.pesos()

        val tvCantidad = findViewById<TextView>(R.id.tvCantidad)
        val btnAgregar = findViewById<AppCompatButton>(R.id.btnAgregar)
        val corazon = findViewById<ImageView>(R.id.ivCorazon)

        fun actualizar() {
            tvCantidad.text = "$cantidad"
            btnAgregar.text = "Agregar al carrito · ${(precio * cantidad).pesos()}"
            corazon.setImageResource(if (favorito) R.drawable.ic_corazon else R.drawable.ic_corazon_borde)
        }
        actualizar()

        findViewById<View>(R.id.btnAtras).setOnClickListener { finish() }
        corazon.setOnClickListener { favorito = !favorito; actualizar() }
        findViewById<View>(R.id.btnMenos).setOnClickListener { if (cantidad > 1) { cantidad--; actualizar() } }
        findViewById<View>(R.id.btnMasCant).setOnClickListener { cantidad++; actualizar() }
        btnAgregar.setOnClickListener {
            Toast.makeText(this, "$cantidad × $nombre agregado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}