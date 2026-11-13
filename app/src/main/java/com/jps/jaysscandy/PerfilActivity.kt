package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(2)

        findViewById<View>(R.id.btnEditarPerfil).setOnClickListener {
            startActivity(Intent(this, EditarDatosActivity::class.java))
        }
        findViewById<View>(R.id.llBoxPedidos)?.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
        findViewById<View>(R.id.llBoxFavoritos)?.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }
        findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            Graph.sesion.cerrar()
            startActivity(
                Intent(this, InicioSesion::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        // TEMPORAL: dejar presionado el avatar abre el panel del vendedor
        findViewById<View>(R.id.tvAvatarGrande).setOnLongClickListener {
            startActivity(Intent(this, PanelVendedorActivity::class.java))
            true
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val tvNombrePerfil = findViewById<TextView>(R.id.tvNombrePerfil)
        val tvSubtituloPerfil = findViewById<TextView>(R.id.tvSubtituloPerfil)
        val tvAvatarGrande = findViewById<TextView>(R.id.tvAvatarGrande)
        val ivAvatarFoto = findViewById<ImageView>(R.id.ivAvatarFoto)
        val tvNumPedidos = findViewById<TextView>(R.id.tvNumPedidos)
        val tvNumFavoritos = findViewById<TextView>(R.id.tvNumFavoritos)
        val llDatos = findViewById<LinearLayout>(R.id.llDatos)

        lifecycleScope.launch {
            try {
                Graph.iniciar(this@PerfilActivity)
                val id = Graph.sesion.usuarioId()
                if (id != null) {
                    val bd = AppDatabase.obtener(this@PerfilActivity)
                    val pedidosCount = bd.pedidoDao().contarPorUsuario(id)
                    tvNumPedidos?.text = "$pedidosCount"
                } else {
                    tvNumPedidos?.text = "0"
                }
                val favoritosCount = Graph.productos.obtenerTodos().count { it.favorito }
                tvNumFavoritos?.text = "$favoritosCount"
            } catch (e: Exception) {
                tvNumPedidos?.text = "0"
                tvNumFavoritos?.text = "0"
            }

            val id = Graph.sesion.usuarioId()
            val usuario = id?.let { Graph.usuarios.obtenerPorId(it) }

            if (usuario != null) {
                tvNombrePerfil.text = usuario.nombre

                if (!usuario.fotoUri.isNullOrEmpty()) {
                    try {
                        ivAvatarFoto?.setImageURI(android.net.Uri.parse(usuario.fotoUri))
                        ivAvatarFoto?.visibility = View.VISIBLE
                        tvAvatarGrande.visibility = View.GONE
                    } catch (e: Exception) {
                        ivAvatarFoto?.visibility = View.GONE
                        tvAvatarGrande.visibility = View.VISIBLE
                    }
                } else {
                    ivAvatarFoto?.visibility = View.GONE
                    tvAvatarGrande.visibility = View.VISIBLE
                }

                val iniciales = usuario.nombre.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it[0].uppercaseChar() }
                    .joinToString("")
                tvAvatarGrande.text = if (iniciales.isNotEmpty()) iniciales else "U"

                val tipo = if (usuario.rol == Rol.DUENO) "Vendedor / Dueño" else "Alumno"
                tvSubtituloPerfil.text = "$tipo · Jay's Candy's"

                llDatos.removeAllViews()
                listOf(
                    Triple("Número", usuario.telefono, R.drawable.ic_telefono),
                    Triple("Carrera", usuario.carrera.ifBlank { "N/A" }, R.drawable.ic_birrete),
                    Triple("Edificio", usuario.edificio.ifBlank { "N/A" }, R.drawable.ic_edificio)
                ).forEach { (etiqueta, valor, icono) ->
                    val v = layoutInflater.inflate(R.layout.item_dato, llDatos, false)
                    v.findViewById<ImageView>(R.id.ivIconoDato).setImageResource(icono)
                    v.findViewById<TextView>(R.id.tvEtiquetaDato).text = etiqueta
                    v.findViewById<TextView>(R.id.tvValorDato).text = valor
                    v.setOnClickListener { startActivity(Intent(this@PerfilActivity, EditarDatosActivity::class.java)) }
                    llDatos.addView(v)
                }
            }
        }
    }
}