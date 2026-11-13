package com.jps.jaysscandy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ResumenActivity : AppCompatActivity() {

    private var mensajePedido: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarEncabezado("Resumen del pedido")

        val items = Datos.itemsCarrito
        val total = items.sumOf { it.producto.precio * it.cantidad }
        val tvMensaje = findViewById<TextView>(R.id.tvMensaje)
        val rgMetodoPago = findViewById<RadioGroup>(R.id.rgMetodoPago)
        val rbEfectivo = findViewById<RadioButton>(R.id.rbEfectivo)
        val llInfoTransferencia = findViewById<View>(R.id.llInfoTransferencia)

        var usuarioObj: Usuario? = null

        fun construirMensaje() {
            val esEfectivo = rbEfectivo.isChecked
            val metodoPagoStr = if (esEfectivo) "Efectivo 💵" else "Transferencia 💳"

            mensajePedido = buildString {
                append("🍬 ¡Hola! Me gustaría hacer el siguiente pedido en Jay's Candy's: 🍭\n\n")
                append("🛍️ *Productos:*\n")
                items.forEach {
                    append("• ${it.cantidad} × ${it.producto.nombre} (${it.producto.presentacion}) — ${(it.producto.precio * it.cantidad).pesos()}\n")
                }
                append("\n💰 *Total a pagar:* ${total.pesos()}")
                append("\n💳 *Método de Pago:* $metodoPagoStr")
                append("\n\n👤 *Datos del Cliente:*")
                append("\n• Nombre: ${usuarioObj?.nombre ?: "Cliente"}")
                append("\n• Teléfono: ${usuarioObj?.telefono ?: "N/A"}")
                append("\n• Edificio: ${usuarioObj?.edificio?.ifBlank { "N/A" } ?: "N/A"}")
                append("\n• Carrera: ${usuarioObj?.carrera?.ifBlank { "N/A" } ?: "N/A"}")
                append("\n\n✨ ¡Quedo atento a la confirmación! Gracias.")
            }
            tvMensaje.text = mensajePedido
        }

        rgMetodoPago.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbTransferencia) {
                llInfoTransferencia.visibility = View.VISIBLE
            } else {
                llInfoTransferencia.visibility = View.GONE
            }
            construirMensaje()
        }

        lifecycleScope.launch {
            try {
                Graph.iniciar(this@ResumenActivity)
                val id = Graph.sesion.usuarioId()
                usuarioObj = id?.let { Graph.usuarios.obtenerPorId(it) }
                construirMensaje()
            } catch (e: Exception) {
                tvMensaje.text = "Error al generar resumen del pedido"
            }
        }

        findViewById<View>(R.id.btnAbrirWhatsApp).setOnClickListener {
            if (mensajePedido.isEmpty()) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    Graph.iniciar(this@ResumenActivity)
                    val usuarioId = Graph.sesion.usuarioId() ?: ""

                    // Guardar registro de pedido completado en la BD
                    val bd = AppDatabase.obtener(this@ResumenActivity)
                    bd.pedidoDao().insertar(
                        PedidoEntity(
                            usuarioId = usuarioId,
                            total = total,
                            resumen = mensajePedido
                        )
                    )

                    // Descontar del stock en la BD para cada producto comprado
                    items.forEach { item ->
                        val p = Graph.productos.obtenerPorId(item.producto.id)
                        if (p != null) {
                            val nuevoStock = maxOf(0, p.stock - item.cantidad)
                            Graph.productos.guardar(p.copy(stock = nuevoStock))
                        }
                    }
                    // Limpiar carrito
                    Datos.itemsCarrito.clear()

                    val url = "https://api.whatsapp.com/send?phone=525576214366&text=" + Uri.encode(mensajePedido)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val url = "https://api.whatsapp.com/send?phone=525576214366&text=" + Uri.encode(mensajePedido)
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (ex: Exception) {
                        Toast.makeText(this@ResumenActivity, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        findViewById<View>(R.id.btnVolverCarrito).setOnClickListener { finish() }
    }
}