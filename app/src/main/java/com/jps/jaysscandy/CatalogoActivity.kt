package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CatalogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(1)

        findViewById<View>(R.id.btnVerCarrito).setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarBarraCarrito()
        cargarCatalogo()
    }

    fun actualizarBarraCarrito() {
        val btnVerCarrito = findViewById<View>(R.id.btnVerCarrito)
        val tvCantCarrito = findViewById<TextView>(R.id.tvCantCarrito)
        val tvDescCarrito = findViewById<TextView>(R.id.tvDescCarrito)
        val tvPrecioCarrito = findViewById<TextView>(R.id.tvPrecioCarrito)

        val totalCant = Datos.itemsCarrito.sumOf { it.cantidad }
        val totalPrecio = Datos.itemsCarrito.sumOf { it.producto.precio * it.cantidad }

        if (totalCant > 0) {
            btnVerCarrito?.visibility = View.VISIBLE
            tvCantCarrito?.text = "$totalCant"
            tvDescCarrito?.text = "$totalCant ${if (totalCant == 1) "producto" else "productos"}"
            tvPrecioCarrito?.text = totalPrecio.pesos()
        } else {
            btnVerCarrito?.visibility = View.GONE
        }
    }

    private fun cargarCatalogo() {
        val busqueda = intent.getStringExtra("busqueda")?.trim()
        val categoria = intent.getStringExtra("categoria") ?: "Galletas"
        val grid = findViewById<GridLayout>(R.id.gridProductos)

        lifecycleScope.launch {
            try {
                Graph.iniciar(this@CatalogoActivity)
                val listaEntities = if (!busqueda.isNullOrEmpty()) {
                    val todos = Graph.productos.obtenerTodos()
                    todos.filter {
                        it.nombre.contains(busqueda, ignoreCase = true) ||
                        it.categoria.contains(busqueda, ignoreCase = true) ||
                        it.presentacion.contains(busqueda, ignoreCase = true)
                    }
                } else if (categoria.equals("TODOS", ignoreCase = true)) {
                    Graph.productos.obtenerTodos()
                } else {
                    Graph.productos.porCategoria(categoria)
                }

                val titulo = when {
                    !busqueda.isNullOrEmpty() -> "Búsqueda: \"$busqueda\""
                    categoria.equals("TODOS", ignoreCase = true) -> "Catálogo completo"
                    else -> categoria
                }
                configurarEncabezado(titulo, "${listaEntities.size} productos disponibles")

                val lista = listaEntities.map { it.toProducto() }
                llenarProductos(grid, lista) { abrirDetalle(it) }
            } catch (e: Exception) {
                Toast.makeText(this@CatalogoActivity, "Error al cargar catálogo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}