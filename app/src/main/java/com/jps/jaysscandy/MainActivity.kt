package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var indiceAnuncio = 0
    private val anuncios = listOf(
        Triple("🍫 ¡Promoción Especial!", "Disfruta de los mejores chocolates artesanales en tu universidad.", "🍫"),
        Triple("🥤 ¡Combo del Día!", "Pide tu refresco + papas con un 15% de descuento.", "🥤"),
        Triple("⚡ Entregas Rápidas", "Tu pedido llega directamente a tu edificio escolar en minutos.", "⚡"),
        Triple("💙 Apoya el Proyecto", "Puedes realizar una donación desde el menú lateral.", "🎁")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.iniciar(this)
        setContentView(R.layout.activity_main)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(1)

        findViewById<View>(R.id.btnMenu).setOnClickListener { mostrarMenuLateral() }

        iniciarCarruselAnuncios()

        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        fun buscar() {
            val query = etBuscar.text.toString().trim()
            if (query.isNotEmpty()) {
                startActivity(Intent(this, CatalogoActivity::class.java).putExtra("busqueda", query))
            } else {
                abrirCatalogo("Galletas")
            }
        }

        findViewById<View>(R.id.btnBuscar).setOnClickListener { buscar() }
        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscar()
                true
            } else false
        }

        mapOf(
            R.id.cardGalletas to "Galletas",
            R.id.cardGolosinas to "Golosinas",
            R.id.cardFrituras to "Frituras",
            R.id.cardChocolates to "Chocolates"
        ).forEach { (id, categoria) ->
            findViewById<View>(id).setOnClickListener { abrirCatalogo(categoria) }
        }

        findViewById<View>(R.id.btnVerCatalogo).setOnClickListener {
            abrirCatalogo("TODOS")
        }
    }

    private fun iniciarCarruselAnuncios() {
        lifecycleScope.launch {
            while (!isFinishing && !isDestroyed) {
                val (titulo, desc, icono) = anuncios[indiceAnuncio]
                findViewById<TextView>(R.id.tvIconoAnuncio)?.text = icono
                findViewById<TextView>(R.id.tvTituloAnuncio)?.text = titulo
                findViewById<TextView>(R.id.tvDescAnuncio)?.text = desc
                indiceAnuncio = (indiceAnuncio + 1) % anuncios.size
                delay(4000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        etBuscar.clearFocus()
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            try {
                Graph.iniciar(this@MainActivity)
                val id = Graph.sesion.usuarioId()
                val usuario = id?.let { Graph.usuarios.obtenerPorId(it) }
                if (usuario != null) {
                    val tvSaludo = findViewById<TextView>(R.id.tvSaludo)
                    val primerNombre = usuario.nombre.split(" ").firstOrNull { it.isNotBlank() } ?: usuario.nombre
                    tvSaludo.text = "Hola, $primerNombre"
                }

                val productos = Graph.productos.obtenerTodos()
                findViewById<TextView>(R.id.tvCantGalletas)?.text = "${productos.count { it.categoria == "Galletas" }} productos"
                findViewById<TextView>(R.id.tvCantGolosinas)?.text = "${productos.count { it.categoria == "Golosinas" }} productos"
                findViewById<TextView>(R.id.tvCantFrituras)?.text = "${productos.count { it.categoria == "Frituras" }} productos"
                findViewById<TextView>(R.id.tvCantChocolates)?.text = "${productos.count { it.categoria == "Chocolates" }} productos"
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun abrirCatalogo(categoria: String) {
        startActivity(Intent(this, CatalogoActivity::class.java).putExtra("categoria", categoria))
    }
}