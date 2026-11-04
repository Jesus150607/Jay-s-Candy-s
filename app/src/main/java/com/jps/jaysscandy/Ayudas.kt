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
import java.util.Locale

// ---------- Datos de ejemplo ----------
data class Producto(
    val nombre: String,
    val presentacion: String,
    val precio: Double,
    val emoji: String,
    val fondo: Int,
    val categoria: String,
    var favorito: Boolean = false
)

data class ItemCarrito(val producto: Producto, var cantidad: Int)

object Datos {
    val chispas = Producto("Galletas con chispas", "Paquete 6 pzas", 18.0, "🍪", R.drawable.bg_cat_galletas, "Galletas", true)
    val avena = Producto("Galletas de avena", "Paquete 8 pzas", 20.0, "🍪", R.drawable.bg_cat_frituras, "Galletas")
    val vainilla = Producto("Sándwich de vainilla", "Tubo 12 pzas", 15.0, "🍪", R.drawable.bg_cat_golosinas, "Galletas")
    val maria = Producto("Galletas María", "Paquete 10 pzas", 12.0, "🍪", R.drawable.bg_cat_menta, "Galletas")
    val barra = Producto("Barra de chocolate", "Chocolate con leche", 25.0, "🍫", R.drawable.bg_cat_chocolates, "Chocolates", true)
    val papas = Producto("Papas sabor queso", "Bolsa mediana", 22.0, "🍟", R.drawable.bg_cat_frituras, "Frituras", true)
    val gomitas = Producto("Gomitas de osito", "Bolsa 100 g", 14.0, "🧸", R.drawable.bg_cat_golosinas, "Golosinas", true)

    val catalogo: Map<String, List<Producto>> = mapOf(
        "Galletas" to listOf(chispas, avena, vainilla, maria),
        "Golosinas" to listOf(
            gomitas,
            Producto("Paleta gigante", "Pieza", 10.0, "🍭", R.drawable.bg_cat_golosinas, "Golosinas"),
            Producto("Gomitas ácidas", "Bolsa 80 g", 12.0, "🍬", R.drawable.bg_cat_menta, "Golosinas"),
            Producto("Malvaviscos", "Bolsa 120 g", 16.0, "🍡", R.drawable.bg_cat_frituras, "Golosinas")
        ),
        "Frituras" to listOf(
            papas,
            Producto("Nachos con queso", "Bolsa 90 g", 18.0, "🌮", R.drawable.bg_cat_galletas, "Frituras"),
            Producto("Palomitas", "Bolsa chica", 15.0, "🍿", R.drawable.bg_cat_frituras, "Frituras"),
            Producto("Churritos", "Bolsa 70 g", 12.0, "🥨", R.drawable.bg_cat_menta, "Frituras")
        ),
        "Chocolates" to listOf(
            barra,
            Producto("Bombones", "Caja 8 pzas", 30.0, "🍫", R.drawable.bg_cat_chocolates, "Chocolates"),
            Producto("Chocolate blanco", "Barra 40 g", 18.0, "🍫", R.drawable.bg_cat_golosinas, "Chocolates"),
            Producto("Trufas", "Caja 6 pzas", 35.0, "🍬", R.drawable.bg_cat_galletas, "Chocolates")
        )
    )

    val favoritos = listOf(barra, papas, chispas, gomitas)

    val itemsCarrito = mutableListOf(
        ItemCarrito(chispas, 2),
        ItemCarrito(barra, 1),
        ItemCarrito(papas, 1)
    )
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
        v.findViewById<View>(R.id.flFoto).setBackgroundResource(p.fondo)
        v.findViewById<TextView>(R.id.tvEmoji).text = p.emoji
        v.findViewById<TextView>(R.id.tvNombre).text = p.nombre
        v.findViewById<TextView>(R.id.tvPresentacion).text = p.presentacion
        v.findViewById<TextView>(R.id.tvPrecio).text = p.precio.pesos()

        val corazon = v.findViewById<ImageView>(R.id.ivCorazon)
        fun pintar() {
            corazon.setImageResource(if (p.favorito) R.drawable.ic_corazon else R.drawable.ic_corazon_borde)
        }
        pintar()
        corazon.setOnClickListener { p.favorito = !p.favorito; pintar() }

        v.findViewById<View>(R.id.btnMas).setOnClickListener {
            Toast.makeText(this, "${p.nombre} agregado", Toast.LENGTH_SHORT).show()
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

    val contenedor = d.findViewById<LinearLayout>(R.id.llFilasMenu)
    val filas = listOf(
        FilaMenu("Favoritos", "Tus productos guardados", R.drawable.ic_corazon, R.drawable.bg_icono_rojo, R.color.rojo),
        FilaMenu("Idioma", "Español", R.drawable.ic_idioma, R.drawable.bg_icono_azul, R.color.azul_principal),
        FilaMenu("Información", "Acerca de la app y privacidad", R.drawable.ic_info, R.drawable.bg_icono_amarillo, R.color.amarillo_oscuro),
        FilaMenu("Contáctanos", "Conoce al equipo", R.drawable.ic_telefono, R.drawable.bg_icono_verde, R.color.verde_texto),
        FilaMenu("Buzón", "Sugerencias y comentarios", R.drawable.ic_buzon, R.drawable.bg_icono_azul, R.color.texto_secundario)
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
            if (f.titulo == "Favoritos") {
                startActivity(Intent(this, FavoritosActivity::class.java))
            } else {
                Toast.makeText(this, "${f.titulo}: próximamente", Toast.LENGTH_SHORT).show()
            }
        }
        contenedor.addView(v)
    }
    d.show()
}