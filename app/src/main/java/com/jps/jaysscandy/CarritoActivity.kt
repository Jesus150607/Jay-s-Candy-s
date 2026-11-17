package com.jps.jaysscandy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CarritoActivity : AppCompatActivity() {

    private val items = Datos.itemsCarrito
    private var puntoEntregaSeleccionado: String = "Edificio 1"
    private var metodoPagoSeleccionado: String = "Efectivo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(0)
        configurarEncabezado("Mi carrito", "", atras = false)

        configurarPuntosEntrega()
        configurarMetodosPago()

        findViewById<View>(R.id.btnCopiarClabe)?.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("CLABE", "012345678901234567")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "CLABE copiada al portapapeles", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnVaciar).setOnClickListener {
            items.clear()
            renderCarrito()
        }

        findViewById<View>(R.id.btnPedirWhatsApp).setOnClickListener {
            if (items.isEmpty()) {
                Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(
                    Intent(this, ResumenActivity::class.java)
                        .putExtra("puntoEntrega", puntoEntregaSeleccionado)
                        .putExtra("metodoPago", metodoPagoSeleccionado)
                )
            }
        }
    }

    private fun configurarPuntosEntrega() {
        val btnE1 = findViewById<TextView>(R.id.btnEdif1)
        val btnE2 = findViewById<TextView>(R.id.btnEdif2)
        val btnE3 = findViewById<TextView>(R.id.btnEdif3)
        val btnQueso = findViewById<TextView>(R.id.btnDelQueso)

        val chips = listOf(
            btnE1 to "Edificio 1",
            btnE2 to "Edificio 2",
            btnE3 to "Edificio 3",
            btnQueso to "El Queso"
        )

        fun seleccionar(chipText: String) {
            puntoEntregaSeleccionado = chipText
            chips.forEach { (view, text) ->
                val activo = text == chipText
                view?.setBackgroundResource(if (activo) R.drawable.bg_boton_azul else R.drawable.bg_tarjeta)
                view?.setTextColor(getColor(if (activo) R.color.blanco else R.color.azul_oscuro))
            }
        }

        chips.forEach { (view, text) ->
            view?.setOnClickListener { seleccionar(text) }
        }
        seleccionar("Edificio 1")
    }

    private fun configurarMetodosPago() {
        val btnEfectivo = findViewById<View>(R.id.btnPagoEfectivo)
        val btnTransferencia = findViewById<View>(R.id.btnPagoTransferencia)
        val llCardTransferencia = findViewById<View>(R.id.llCardTransferencia)

        fun seleccionarPago(pago: String) {
            metodoPagoSeleccionado = pago
            if (pago == "Efectivo") {
                btnEfectivo?.setBackgroundResource(R.drawable.bg_boton_azul)
                btnTransferencia?.setBackgroundResource(R.drawable.bg_tarjeta)
                llCardTransferencia?.visibility = View.GONE
            } else {
                btnEfectivo?.setBackgroundResource(R.drawable.bg_tarjeta)
                btnTransferencia?.setBackgroundResource(R.drawable.bg_boton_azul)
                llCardTransferencia?.visibility = View.VISIBLE
            }
        }

        btnEfectivo?.setOnClickListener { seleccionarPago("Efectivo") }
        btnTransferencia?.setOnClickListener { seleccionarPago("Transferencia") }
        seleccionarPago("Efectivo")
    }

    override fun onResume() {
        super.onResume()
        renderCarrito()
    }

    private fun renderCarrito() {
        val llItems = findViewById<LinearLayout>(R.id.llItems)
        llItems.removeAllViews()

        val cantidadTotal = items.sumOf { it.cantidad }
        val total = items.sumOf { it.producto.precio * it.cantidad }

        findViewById<TextView>(R.id.tvSubtitulo).text = "$cantidadTotal productos listos para pedir"
        findViewById<TextView>(R.id.tvSubtotalLabel).text = "Subtotal ($cantidadTotal productos)"
        findViewById<TextView>(R.id.tvSubtotal).text = total.pesos()
        findViewById<TextView>(R.id.tvTotal).text = total.pesos()

        if (items.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "Tu carrito está vacío.\nExplora el catálogo para agregar productos."
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 64, 0, 64)
                setTextColor(getColor(R.color.texto_secundario))
            }
            llItems.addView(tvVacio)
            return
        }

        items.toList().forEach { item ->
            val v = layoutInflater.inflate(R.layout.item_carrito, llItems, false)
            val p = item.producto

            val flMiniatura = v.findViewById<View>(R.id.flMiniatura)
            val tvEmoji = v.findViewById<TextView>(R.id.tvEmoji)

            flMiniatura.setBackgroundResource(p.fondo)
            if (!p.fotoUri.isNullOrEmpty()) {
                try {
                    val ivFoto = ImageView(this).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageURI(Uri.parse(p.fotoUri))
                    }
                    (flMiniatura as? android.widget.FrameLayout)?.addView(ivFoto)
                    tvEmoji.visibility = View.GONE
                } catch (e: Exception) {
                    tvEmoji.text = p.emoji
                }
            } else {
                tvEmoji.text = p.emoji
            }

            v.findViewById<TextView>(R.id.tvNombre).text = p.nombre
            v.findViewById<TextView>(R.id.tvPresentacion).text = p.presentacion
            v.findViewById<TextView>(R.id.tvPrecio).text = (p.precio * item.cantidad).pesos()

            val tvCantidad = v.findViewById<TextView>(R.id.tvCantidad)
            tvCantidad.text = "${item.cantidad}"

            v.findViewById<View>(R.id.btnMenos).setOnClickListener {
                if (item.cantidad > 1) {
                    item.cantidad--
                } else {
                    items.remove(item)
                }
                renderCarrito()
            }
            v.findViewById<View>(R.id.btnMasCant).setOnClickListener {
                item.cantidad++
                renderCarrito()
            }
            v.findViewById<View>(R.id.ivBasura).setOnClickListener {
                items.remove(item)
                renderCarrito()
            }
            llItems.addView(v)
        }
    }
}