package com.jps.jaysscandy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
        val puntoEntrega = intent.getStringExtra("puntoEntrega") ?: "Edificio 1"
        val metodoPago = intent.getStringExtra("metodoPago") ?: "Efectivo"

        var usuarioObj: Usuario? = null

        fun construirMensaje() {
            mensajePedido = buildString {
                append("Hola, quiero hacer un pedido:\n\n")
                items.forEach {
                    append("${it.cantidad} × ${it.producto.nombre} — ${(it.producto.precio * it.cantidad).pesos()}\n")
                }
                append("Punto de entrega: $puntoEntrega\n")
                append("Total: ${total.pesos()}\n")
                append("Pago: $metodoPago\n")
                val clienteNombre = usuarioObj?.nombre ?: "Cliente"
                val clienteTel = usuarioObj?.telefono ?: "N/A"
                append("Cliente: $clienteNombre · $clienteTel")
            }
            tvMensaje.text = mensajePedido
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