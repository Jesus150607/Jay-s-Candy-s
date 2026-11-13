package com.jps.jaysscandy

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

import java.util.UUID

// ---------- Datos ----------
data class Producto(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val presentacion: String,
    val precio: Double,
    val emoji: String,
    val fondo: Int,
    val categoria: String,
    var favorito: Boolean = false,
    val fotoUri: String? = null,
    var stock: Int = 10,
    val descripcion: String = ""
)

fun Producto.toEntity() = ProductoEntity(
    id = id,
    nombre = nombre,
    presentacion = presentacion,
    precio = precio,
    emoji = emoji,
    fondo = fondo,
    categoria = categoria,
    fotoUri = fotoUri,
    favorito = favorito,
    stock = stock,
    descripcion = descripcion
)

data class ItemCarrito(val producto: Producto, var cantidad: Int)

object Datos {
    val catalogo: Map<String, List<Producto>> = emptyMap()
    val favoritos = emptyList<Producto>()
    val itemsCarrito = mutableListOf<ItemCarrito>()
}

fun Double.pesos(): String = "\$" + String.format(Locale.US, "%.2f", this)

// ---------- Pantalla ----------
fun Activity.ajustarBarraSistema(raiz: View) {
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
        v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
        insets
    }
}

fun Activity.configurarEncabezado(titulo: String, subtitulo: String? = null, atras: Boolean = true) {
    findViewById<TextView>(R.id.tvTitulo).text = titulo
    val sub = findViewById<TextView>(R.id.tvSubtitulo)
    if (subtitulo == null) sub.visibility = View.GONE else sub.text = subtitulo
    val btn = findViewById<View>(R.id.btnAtras)
    if (atras) btn.setOnClickListener { finish() } else btn.visibility = View.INVISIBLE
}

// ---------- Productos ----------
fun Activity.llenarProductos(grid: GridLayout, lista: List<Producto>, alAbrir: (Producto) -> Unit) {
    grid.removeAllViews()
    val m = (6 * resources.displayMetrics.density).toInt()
    lista.forEach { p ->
        val v = layoutInflater.inflate(R.layout.item_producto, grid, false)
        val flFoto = v.findViewById<View>(R.id.flFoto)
        val tvEmoji = v.findViewById<TextView>(R.id.tvEmoji)
        val ivFoto = v.findViewById<ImageView>(R.id.ivFotoProducto)

        flFoto.setBackgroundResource(p.fondo)
        if (!p.fotoUri.isNullOrEmpty()) {
            try {
                ivFoto.setImageURI(android.net.Uri.parse(p.fotoUri))
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
        v.findViewById<TextView>(R.id.tvPresentacion).text = p.presentacion
        v.findViewById<TextView>(R.id.tvPrecio).text = p.precio.pesos()

        val corazon = v.findViewById<ImageView>(R.id.ivCorazon)
        fun pintar() {
            corazon.setImageResource(if (p.favorito) R.drawable.ic_corazon else R.drawable.ic_corazon_borde)
        }
        pintar()
        corazon.setOnClickListener {
            p.favorito = !p.favorito
            pintar()
            (this as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
                try {
                    Graph.iniciar(this@llenarProductos)
                    Graph.productos.guardar(p.toEntity())
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

        v.findViewById<View>(R.id.btnMas).setOnClickListener {
            if (p.stock <= 0) {
                Toast.makeText(this, "Producto agotado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val existente = Datos.itemsCarrito.find { it.producto.nombre == p.nombre }
            val cantActual = existente?.cantidad ?: 0
            if (cantActual + 1 > p.stock) {
                Toast.makeText(this, "Llegaste al límite del stock disponible (${p.stock})", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (existente != null) {
                existente.cantidad++
            } else {
                Datos.itemsCarrito.add(ItemCarrito(p, 1))
            }
            Toast.makeText(this, "${p.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
            (this as? CatalogoActivity)?.actualizarBarraCarrito()
        }
        v.setOnClickListener { alAbrir(p) }

        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        lp.width = 0
        lp.setMargins(m, m, m, m)
        grid.addView(v, lp)
    }
}

fun Activity.abrirDetalle(p: Producto) {
    startActivity(Intent(this, DetalleActivity::class.java).apply {
        putExtra("nombre", p.nombre)
        putExtra("presentacion", p.presentacion)
        putExtra("precio", p.precio)
        putExtra("emoji", p.emoji)
        putExtra("fondo", p.fondo)
        putExtra("categoria", p.categoria)
        putExtra("favorito", p.favorito)
        putExtra("fotoUri", p.fotoUri)
        putExtra("stock", p.stock)
        putExtra("descripcion", p.descripcion)
    })
}

// ---------- Listas desplegables ----------
fun Activity.crearAdaptadorSpinner(opciones: Array<String>): ArrayAdapter<String> {
    val act = this
    return object : ArrayAdapter<String>(act, R.layout.item_spinner, opciones) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            pintar(super.getView(position, convertView, parent) as TextView, position)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
            pintar(super.getDropDownView(position, convertView, parent) as TextView, position)

        private fun pintar(v: TextView, position: Int): View {
            v.setTextColor(act.getColor(if (position == 0) R.color.texto_hint else R.color.azul_oscuro))
            return v
        }
    }
}

// ---------- Diálogo de eliminar ----------
fun Activity.mostrarDialogoEliminar(nombre: String, alConfirmar: () -> Unit) {
    val d = Dialog(this)
    d.requestWindowFeature(Window.FEATURE_NO_TITLE)
    d.setContentView(R.layout.dialog_eliminar)
    d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    d.window?.setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    d.findViewById<TextView>(R.id.tvMensajeEliminar).text = "$nombre se quitará del catálogo."
    d.findViewById<View>(R.id.btnCancelarEliminar).setOnClickListener { d.dismiss() }
    d.findViewById<View>(R.id.btnConfirmarEliminar).setOnClickListener {
        d.dismiss()
        alConfirmar()
    }
    d.show()
}

// ---------- Menú lateral ----------
private data class FilaMenu(val titulo: String, val sub: String, val icono: Int, val fondo: Int, val color: Int)

fun Activity.mostrarMenuLateral() {
    val d = Dialog(this)
    d.requestWindowFeature(Window.FEATURE_NO_TITLE)
    d.setContentView(R.layout.layout_menu_lateral)
    d.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout((resources.displayMetrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.START)
        setWindowAnimations(R.style.AnimMenu)
    }
    d.findViewById<View>(R.id.btnCerrarMenu).setOnClickListener { d.dismiss() }

    val tvNombre = d.findViewById<TextView>(R.id.tvNombreMenu)
    val tvEdificio = d.findViewById<TextView>(R.id.tvEdificioMenu)
    val tvCarrera = d.findViewById<TextView>(R.id.tvCarreraMenu)

    val id = Graph.sesion.usuarioId()
    if (id != null) {
        CoroutineScope(Dispatchers.Main).launch {
            val usuario = Graph.usuarios.obtenerPorId(id)
            if (usuario != null) {
                tvNombre?.text = usuario.nombre
                tvEdificio?.text = usuario.edificio.ifBlank { "N/A" }
                tvCarrera?.text = usuario.carrera.ifBlank { "N/A" }
            }
        }
    }

    val contenedor = d.findViewById<LinearLayout>(R.id.llFilasMenu)
    val filas = listOf(
        FilaMenu("Favoritos", "Tus productos guardados", R.drawable.ic_corazon, R.drawable.bg_icono_rojo, R.color.rojo),
        FilaMenu("Donaciones", "Apoya el desarrollo del proyecto", R.drawable.ic_info, R.drawable.bg_icono_amarillo, R.color.amarillo_oscuro)
    )
    filas.forEach { f ->
        val v = layoutInflater.inflate(R.layout.item_menu_fila, contenedor, false)
        v.findViewById<TextView>(R.id.tvTituloFila).text = f.titulo
        v.findViewById<TextView>(R.id.tvSubFila).text = f.sub
        v.findViewById<View>(R.id.flIconoFila).setBackgroundResource(f.fondo)
        val ic = v.findViewById<ImageView>(R.id.ivIconoFila)
        ic.setImageResource(f.icono)
        ic.setColorFilter(getColor(f.color))
        v.setOnClickListener {
            d.dismiss()
            when (f.titulo) {
                "Favoritos" -> startActivity(Intent(this, FavoritosActivity::class.java))
                "Donaciones" -> mostrarDialogoDonaciones()
                else -> Toast.makeText(this, "${f.titulo}: en desarrollo", Toast.LENGTH_SHORT).show()
            }
        }
        contenedor.addView(v)
    }
    d.show()
}

fun Activity.mostrarDialogoDonaciones() {
    val d = Dialog(this)
    d.requestWindowFeature(Window.FEATURE_NO_TITLE)
    d.setContentView(R.layout.dialog_eliminar)
    d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    d.window?.setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

    val tvMensaje = d.findViewById<TextView>(R.id.tvMensajeEliminar)
    val btnCancelar = d.findViewById<View>(R.id.btnCancelarEliminar)
    val btnConfirmar = d.findViewById<TextView>(R.id.btnConfirmarEliminar)

    tvMensaje?.text = "🎁 Donaciones para el Proyecto\n\nTu apoyo nos ayuda a seguir manteniendo la aplicación activa para la comunidad universitaria.\n\n• Fundación: Dulzura Escolar A.C.\n• Banco: Banamex\n• CLABE: 0021 8001 5576 2143 66"
    btnCancelar?.visibility = View.GONE
    btnConfirmar?.text = "Entendido"
    btnConfirmar?.setOnClickListener { d.dismiss() }
    d.show()
}