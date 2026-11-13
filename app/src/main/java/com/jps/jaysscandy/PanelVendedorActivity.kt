package com.jps.jaysscandy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PanelVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.iniciar(this)
        setContentView(R.layout.activity_panel_vendedor)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarEncabezado("Panel del vendedor", "", atras = true)

        findViewById<View>(R.id.btnPublicar).setOnClickListener {
            startActivity(Intent(this, PublicarActivity::class.java))
        }
        findViewById<View>(R.id.tvCerrarSesion)?.setOnClickListener {
            confirmarCerrarSesion()
        }
    }

    private fun confirmarCerrarSesion() {
        Graph.sesion.cerrar()
        startActivity(
            Intent(this, InicioSesion::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }


    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    private fun cargarProductos() {
        val ll = findViewById<LinearLayout>(R.id.llProductos)
        val tvSub = findViewById<TextView>(R.id.tvSubtitulo)

        lifecycleScope.launch {
            try {
                Graph.iniciar(this@PanelVendedorActivity)
                val lista = Graph.productos.obtenerTodos()
                ll.removeAllViews()
                tvSub.text = "Mis productos publicados · ${lista.size}"

                if (lista.isEmpty()) {
                    val tvVacio = TextView(this@PanelVendedorActivity).apply {
                        text = "Aún no has publicado productos.\nPulsa '+ Publicar dulce' para empezar."
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setPadding(0, 48, 0, 48)
                        setTextColor(getColor(R.color.texto_secundario))
                    }
                    ll.addView(tvVacio)
                    return@launch
                }

                lista.forEach { p ->
                    val v = layoutInflater.inflate(R.layout.item_producto_vendedor, ll, false)
                    val flMiniatura = v.findViewById<View>(R.id.flMiniatura)
                    val tvEmoji = v.findViewById<TextView>(R.id.tvEmoji)
                    val ivFoto = v.findViewById<ImageView>(R.id.ivFotoProducto)

                    flMiniatura.setBackgroundResource(p.fondo)
                    if (!p.fotoUri.isNullOrEmpty()) {
                        try {
                            ivFoto.setImageURI(Uri.parse(p.fotoUri))
                            ivFoto.visibility = View.VISIBLE
                            tvEmoji.visibility = View.GONE
                        } catch (e: Exception) {
                            ivFoto.visibility = View.GONE
                            tvEmoji.visibility = View.VISIBLE
                            tvEmoji.text = p.emoji
                        }
                    } else {
                        ivFoto.visibility = View.GONE
                        tvEmoji.visibility = View.VISIBLE
                        tvEmoji.text = p.emoji
                    }

                    v.findViewById<TextView>(R.id.tvNombre).text = p.nombre
                    v.findViewById<TextView>(R.id.tvSub).text = "${p.categoria} · ${p.presentacion} · ${p.precio.pesos()}"

                    v.findViewById<View>(R.id.ivEditar).setOnClickListener {
                        startActivity(
                            Intent(this@PanelVendedorActivity, PublicarActivity::class.java)
                                .putExtra("id", p.id)
                        )
                    }
                    v.findViewById<View>(R.id.ivEliminar).setOnClickListener {
                        mostrarDialogoEliminar(p.nombre) {
                            lifecycleScope.launch {
                                Graph.productos.eliminar(p.id)
                                Toast.makeText(this@PanelVendedorActivity, "${p.nombre} eliminado", Toast.LENGTH_SHORT).show()
                                cargarProductos()
                            }
                        }
                    }
                    ll.addView(v)
                }
            } catch (e: Exception) {
                Toast.makeText(this@PanelVendedorActivity, "Error al cargar productos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}