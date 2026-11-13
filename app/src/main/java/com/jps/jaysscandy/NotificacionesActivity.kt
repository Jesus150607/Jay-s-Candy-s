package com.jps.jaysscandy

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class Aviso(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val hora: String,
    val icono: Int,
    val fondo: Int,
    val color: Int,
    var sinLeer: Boolean
)

object NotificacionesManager {
    val lista = mutableListOf(
        Aviso("1", "Tu pedido fue enviado", "Quedó listo para acordar la entrega por WhatsApp.", "Hace 5 min",
            R.drawable.ic_check, R.drawable.bg_icono_verde, R.color.verde_texto, true),
        Aviso("2", "Combo dulce del día", "Arma tu pedido con el combo de hoy.", "Hace 1 h",
            R.drawable.ic_campana, R.drawable.bg_icono_amarillo, R.color.amarillo_oscuro, true),
        Aviso("3", "Nuevo producto en catálogo", "Hay nuevos dulces disponibles en la tienda.", "Ayer",
            R.drawable.ic_corazon, R.drawable.bg_icono_rojo, R.color.rojo, true),
        Aviso("4", "Bienvenido a Jay's Candy's", "Ya puedes explorar y hacer tus pedidos.", "Hace 2 días",
            R.drawable.ic_info, R.drawable.bg_icono_azul, R.color.azul_principal, false)
    )

    fun sinLeerCount(): Int = lista.count { it.sinLeer }
}

class NotificacionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        ajustarBarraSistema(findViewById(R.id.raiz))
        renderNotificaciones()
    }

    private fun renderNotificaciones() {
        val avisos = NotificacionesManager.lista
        val sinLeerCount = NotificacionesManager.sinLeerCount()

        configurarEncabezado("Notificaciones", "$sinLeerCount sin leer")

        val ll = findViewById<LinearLayout>(R.id.llAvisos)
        ll.removeAllViews()

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

            v.setOnClickListener {
                if (a.sinLeer) {
                    a.sinLeer = false
                    Toast.makeText(this, "Notificación leída", Toast.LENGTH_SHORT).show()
                    renderNotificaciones()
                }
            }
            ll.addView(v)
        }
    }
}