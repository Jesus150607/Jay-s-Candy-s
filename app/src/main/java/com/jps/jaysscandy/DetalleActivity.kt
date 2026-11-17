package com.jps.jaysscandy

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        ajustarBarraSistema(findViewById(R.id.raiz))

        val productId = intent.getStringExtra("id")
        var nombre = intent.getStringExtra("nombre") ?: ""
        var precio = intent.getDoubleExtra("precio", 0.0)
        var fotoUri = intent.getStringExtra("fotoUri")
        var emoji = intent.getStringExtra("emoji") ?: "🍬"
        var presentacion = intent.getStringExtra("presentacion") ?: ""
        var categoria = intent.getStringExtra("categoria") ?: ""
        var stock = intent.getIntExtra("stock", 10)
        var descripcion = intent.getStringExtra("descripcion") ?: ""
        var favorito = intent.getBooleanExtra("favorito", false)
        var fondo = intent.getIntExtra("fondo", R.drawable.bg_cat_galletas)
        var cantidad = 1

        val flImagen = findViewById<View>(R.id.flImagen)
        val ivFoto = findViewById<ImageView>(R.id.ivFotoProducto)
        val tvEmoji = findViewById<TextView>(R.id.tvEmoji)
        val tvCategoria = findViewById<TextView>(R.id.tvCategoria)
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvPresentacion = findViewById<TextView>(R.id.tvPresentacion)
        val tvPrecio = findViewById<TextView>(R.id.tvPrecio)
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)
        val tvCantidad = findViewById<TextView>(R.id.tvCantidad)
        val btnAgregar = findViewById<AppCompatButton>(R.id.btnAgregar)
        val corazon = findViewById<ImageView>(R.id.ivCorazon)

        fun renderUI() {
            flImagen.setBackgroundResource(fondo)
            if (stock <= 0) {
                flImagen.alpha = 0.5f
            } else {
                flImagen.alpha = 1.0f
            }

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

            tvCategoria.text = categoria
            tvNombre.text = nombre
            tvPresentacion.text = if (stock > 0) "$presentacion · Stock: $stock" else "$presentacion · Agotado"
            tvPrecio.text = precio.pesos()
            tvDescripcion.text = descripcion.ifBlank { "Deliciosos dulces disponibles para entrega inmediata en tu universidad." }

            tvCantidad.text = "$cantidad"
            if (stock <= 0) {
                btnAgregar.text = "Stock insuficiente"
                btnAgregar.isEnabled = false
            } else {
                btnAgregar.isEnabled = true
                btnAgregar.text = "Agregar al carrito · ${(precio * cantidad).pesos()}"
            }
            corazon.setImageResource(if (favorito) R.drawable.ic_corazon else R.drawable.ic_corazon_borde)
        }

        renderUI()

        if (!productId.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    Graph.iniciar(this@DetalleActivity)
                    val dbProduct = Graph.productos.obtenerPorId(productId)
                    if (dbProduct != null) {
                        nombre = dbProduct.nombre
                        precio = dbProduct.precio
                        presentacion = dbProduct.presentacion
                        categoria = dbProduct.categoria
                        stock = dbProduct.stock
                        descripcion = dbProduct.descripcion
                        fotoUri = dbProduct.fotoUri
                        favorito = dbProduct.favorito
                        emoji = dbProduct.emoji
                        fondo = dbProduct.fondo
                        renderUI()
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

        findViewById<View>(R.id.btnAtras).setOnClickListener { finish() }
        corazon.setOnClickListener {
            favorito = !favorito
            renderUI()
            if (!productId.isNullOrEmpty()) {
                lifecycleScope.launch {
                    try {
                        Graph.iniciar(this@DetalleActivity)
                        val dbProduct = Graph.productos.obtenerPorId(productId)
                        if (dbProduct != null) {
                            Graph.productos.guardar(dbProduct.copy(favorito = favorito))
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }

        findViewById<View>(R.id.btnMenos).setOnClickListener { if (cantidad > 1) { cantidad--; renderUI() } }
        findViewById<View>(R.id.btnMasCant).setOnClickListener {
            if (stock <= 0) {
                Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show()
            } else if (cantidad < stock) {
                cantidad++
                renderUI()
            } else {
                Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show()
            }
        }

        btnAgregar.setOnClickListener {
            if (stock <= 0) {
                Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val producto = Producto(
                id = productId ?: java.util.UUID.randomUUID().toString(),
                nombre = nombre,
                presentacion = presentacion,
                precio = precio,
                emoji = emoji,
                fondo = fondo,
                categoria = categoria,
                favorito = favorito,
                fotoUri = fotoUri,
                stock = stock,
                descripcion = descripcion
            )
            val existente = Datos.itemsCarrito.find { it.producto.id == producto.id || it.producto.nombre == nombre }
            val cantActual = existente?.cantidad ?: 0
            if (cantActual + cantidad > stock) {
                Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show()
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