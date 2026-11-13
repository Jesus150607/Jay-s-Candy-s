package com.jps.jaysscandy

import android.net.Uri
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
        val fotoUri = intent.getStringExtra("fotoUri")
        val emoji = intent.getStringExtra("emoji") ?: "🍬"
        val presentacion = intent.getStringExtra("presentacion") ?: ""
        val categoria = intent.getStringExtra("categoria") ?: ""
        val stock = intent.getIntExtra("stock", 10)
        var favorito = intent.getBooleanExtra("favorito", false)
        var cantidad = 1

        findViewById<View>(R.id.flImagen).setBackgroundResource(
            intent.getIntExtra("fondo", R.drawable.bg_cat_galletas)
        )

        val ivFoto = findViewById<ImageView>(R.id.ivFotoProducto)
        val tvEmoji = findViewById<TextView>(R.id.tvEmoji)

        if (!fotoUri.isNullOrEmpty()) {
            try {
                ivFoto.setImageURI(Uri.parse(fotoUri))
                ivFoto.visibility = View.VISIBLE
                tvEmoji.visibility = View.GONE
            } catch (e: Exception) {
                ivFoto.visibility = View.GONE
                tvEmoji.visibility = View.VISIBLE
                tvEmoji.text = emoji
            }
        } else {
            ivFoto.visibility = View.GONE
            tvEmoji.visibility = View.VISIBLE
            tvEmoji.text = emoji
        }

        val descripcion = intent.getStringExtra("descripcion") ?: ""
        findViewById<TextView>(R.id.tvCategoria).text = categoria
        findViewById<TextView>(R.id.tvNombre).text = nombre
        findViewById<TextView>(R.id.tvPresentacion).text = if (stock > 0) "$presentacion · Stock: $stock" else "$presentacion · Agotado"
        findViewById<TextView>(R.id.tvPrecio).text = precio.pesos()
        findViewById<TextView>(R.id.tvDescripcion).text = descripcion.ifBlank { "Deliciosos dulces disponibles para entrega inmediata en tu universidad." }

        val tvCantidad = findViewById<TextView>(R.id.tvCantidad)
        val btnAgregar = findViewById<AppCompatButton>(R.id.btnAgregar)
        val corazon = findViewById<ImageView>(R.id.ivCorazon)

        fun actualizar() {
            tvCantidad.text = "$cantidad"
            if (stock <= 0) {
                btnAgregar.text = "Producto agotado"
                btnAgregar.isEnabled = false
            } else {
                btnAgregar.isEnabled = true
                btnAgregar.text = "Agregar al carrito · ${(precio * cantidad).pesos()}"
            }
            corazon.setImageResource(if (favorito) R.drawable.ic_corazon else R.drawable.ic_corazon_borde)
        }
        actualizar()

        findViewById<View>(R.id.btnAtras).setOnClickListener { finish() }
        corazon.setOnClickListener { favorito = !favorito; actualizar() }
        findViewById<View>(R.id.btnMenos).setOnClickListener { if (cantidad > 1) { cantidad--; actualizar() } }
        findViewById<View>(R.id.btnMasCant).setOnClickListener {
            if (cantidad < stock) {
                cantidad++
                actualizar()
            } else {
                Toast.makeText(this, "Límite del stock disponible ($stock)", Toast.LENGTH_SHORT).show()
            }
        }
        btnAgregar.setOnClickListener {
            if (stock <= 0) return@setOnClickListener
            val producto = Producto(
                nombre = nombre,
                presentacion = presentacion,
                precio = precio,
                emoji = emoji,
                fondo = intent.getIntExtra("fondo", R.drawable.bg_cat_galletas),
                categoria = categoria,
                favorito = favorito,
                fotoUri = fotoUri,
                stock = stock
            )
            val existente = Datos.itemsCarrito.find { it.producto.nombre == nombre }
            val cantActual = existente?.cantidad ?: 0
            if (cantActual + cantidad > stock) {
                Toast.makeText(this, "No hay suficiente stock disponible para agregar esa cantidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (existente != null) {
                existente.cantidad += cantidad
            } else {
                Datos.itemsCarrito.add(ItemCarrito(producto, cantidad))
            }
            Toast.makeText(this, "¡$cantidad × $nombre agregado al carrito!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}