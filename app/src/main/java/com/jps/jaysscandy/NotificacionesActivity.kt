package com.jps.jaysscandy

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private data class Aviso(
    val titulo: String,
    val descripcion: String,
    val hora: String,
    val icono: Int,
    val fondo: Int,
    val color: Int,
    val sinLeer: Boolean
)

class NotificacionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        ajustarBarraSistema(findViewById(R.id.raiz))

        val avisos = listOf(
            Aviso("Tu pedido fue enviado", "Quedó listo para acordar la entrega por WhatsApp.", "Hace 5 min",
                R.drawable.ic_check, R.drawable.bg_icono_verde, R.color.verde_texto, true),
            Aviso("Combo dulce del día", "Arma tu pedido con el combo de hoy.", "Hace 1 h",
                R.drawable.ic_campana, R.drawable.bg_icono_amarillo, R.color.amarillo_oscuro, true),
            Aviso("Nuevo producto", "Llegaron las Galletas María al catálogo.", "Ayer",
                R.drawable.ic_corazon, R.drawable.bg_icono_rojo, R.color.rojo, true),
            Aviso("Bienvenido a Jay's Candy's", "Ya puedes hacer tus pedidos.", "Hace 2 días",
                R.drawable.ic_info, R.drawable.bg_icono_azul, R.color.azul_principal, false)
        )
        configurarEncabezado("Notificaciones", "${avisos.count { it.sinLeer }} sin leer")

        val ll = findViewById<LinearLayout>(R.id.llAvisos)
        avisos.forEach { a ->
            val v = layoutInflater.inflate(R.layout.item_notificacion, ll, false)
            val ic = v.findViewById<ImageView>(R.id.ivIconoAviso)
            ic.setBackgroundResource(a.fondo)
            ic.setImageResource(a.icono)
            ic.setColorFilter(getColor(a.color))
            v.findViewById<TextView>(R.id.tvTituloAviso).text = a.titulo
            v.findViewById<TextView>(R.id.tvDescAviso).text = a.descripcion
            v.findViewById<TextView>(R.id.tvHoraAviso).text = a.hora
            v.findViewById<View>(R.id.vPunto).visibility = if (a.sinLeer) View.VISIBLE else View.INVISIBLE
            ll.addView(v)
        }
    }
}