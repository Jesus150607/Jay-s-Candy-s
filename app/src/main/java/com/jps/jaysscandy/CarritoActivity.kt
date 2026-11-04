package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CarritoActivity : AppCompatActivity() {

    private val items = Datos.itemsCarrito

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(0)
        configurarEncabezado("Mi carrito", "", atras = false)

        val llItems = findViewById<LinearLayout>(R.id.llItems)

        fun totales() {
            val cantidad = items.sumOf { it.cantidad }
            val total = items.sumOf { it.producto.precio * it.cantidad }
            findViewById<TextView>(R.id.tvSubtitulo).text = "$cantidad productos listos para pedir"
            findViewById<TextView>(R.id.tvSubtotalLabel).text = "Subtotal ($cantidad productos)"
            findViewById<TextView>(R.id.tvSubtotal).text = total.pesos()
            findViewById<TextView>(R.id.tvTotal).text = total.pesos()
        }

        items.toList().forEach { item ->
            val v = layoutInflater.inflate(R.layout.item_carrito, llItems, false)
            val p = item.producto
            v.findViewById<View>(R.id.flMiniatura).setBackgroundResource(p.fondo)
            v.findViewById<TextView>(R.id.tvEmoji).text = p.emoji
            v.findViewById<TextView>(R.id.tvNombre).text = p.nombre
            v.findViewById<TextView>(R.id.tvPresentacion).text = p.presentacion
            v.findViewById<TextView>(R.id.tvPrecio).text = p.precio.pesos()
            val tvCantidad = v.findViewById<TextView>(R.id.tvCantidad)
            tvCantidad.text = "${item.cantidad}"

            v.findViewById<View>(R.id.btnMenos).setOnClickListener {
                if (item.cantidad > 1) { item.cantidad--; tvCantidad.text = "${item.cantidad}"; totales() }
            }
            v.findViewById<View>(R.id.btnMasCant).setOnClickListener {
                item.cantidad++; tvCantidad.text = "${item.cantidad}"; totales()
            }
            v.findViewById<View>(R.id.ivBasura).setOnClickListener {
                items.remove(item); llItems.removeView(v); totales()
            }
            llItems.addView(v)
        }
        totales()

        findViewById<View>(R.id.btnVaciar).setOnClickListener {
            items.clear(); llItems.removeAllViews(); totales()
        }
        findViewById<View>(R.id.btnPedirWhatsApp).setOnClickListener {
            startActivity(Intent(this, ResumenActivity::class.java))
        }
    }
}