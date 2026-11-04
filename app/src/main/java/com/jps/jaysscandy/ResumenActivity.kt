package com.jps.jaysscandy

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResumenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarEncabezado("Resumen del pedido")

        val items = Datos.itemsCarrito
        val total = items.sumOf { it.producto.precio * it.cantidad }
        findViewById<TextView>(R.id.tvMensaje).text = buildString {
            append("Hola, quiero hacer un pedido:\n\n")
            items.forEach {
                append("${it.cantidad} × ${it.producto.nombre} — ${(it.producto.precio * it.cantidad).pesos()}\n")
            }
            append("\nTotal: ${total.pesos()}")
            append("\nPago: se acuerda con el vendedor")
            append("\nCliente: Javier Trejo · 55 1234 5678")
        }

        findViewById<android.view.View>(R.id.btnAbrirWhatsApp).setOnClickListener {
            Toast.makeText(this, "WhatsApp: próximamente", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.btnVolverCarrito).setOnClickListener { finish() }
    }
}