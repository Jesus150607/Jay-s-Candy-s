package com.jps.jaysscandy

import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(2)
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        val grid = findViewById<GridLayout>(R.id.gridProductos)
        lifecycleScope.launch {
            try {
                Graph.iniciar(this@FavoritosActivity)
                val favoritosEntities = Graph.productos.obtenerTodos().filter { it.favorito }
                val lista = favoritosEntities.map { it.toProducto() }

                configurarEncabezado("Mis favoritos", "${lista.size} productos guardados")
                llenarProductos(grid, lista) { abrirDetalle(it) }
            } catch (e: Exception) {
                Toast.makeText(this@FavoritosActivity, "Error al cargar favoritos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}