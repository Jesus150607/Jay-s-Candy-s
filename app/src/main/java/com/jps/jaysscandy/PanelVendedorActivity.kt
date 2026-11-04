package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PanelVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_vendedor)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarEncabezado("Panel del vendedor", "", atras = false)

        val ll = findViewById<LinearLayout>(R.id.llProductos)
        val tvSub = findViewById<TextView>(R.id.tvSubtitulo)
        fun contar() { tvSub.text = "Mis productos publicados · ${ll.childCount}" }

        listOf(Datos.chispas, Datos.barra, Datos.papas, Datos.gomitas, Datos.avena).forEach { p ->
            val v = layoutInflater.inflate(R.layout.item_producto_vendedor, ll, false)
            v.findViewById<View>(R.id.flMiniatura).setBackgroundResource(p.fondo)
            v.findViewById<TextView>(R.id.tvEmoji).text = p.emoji
            v.findViewById<TextView>(R.id.tvNombre).text = p.nombre
            v.findViewById<TextView>(R.id.tvSub).text = "${p.categoria} · ${p.precio.pesos()}"

            v.findViewById<View>(R.id.ivEditar).setOnClickListener {
                startActivity(
                    Intent(this, PublicarActivity::class.java)
                        .putExtra("editar", true)
                        .putExtra("nombre", p.nombre)
                        .putExtra("precio", p.precio)
                        .putExtra("presentacion", p.presentacion)
                        .putExtra("categoria", p.categoria)
                )
            }
            v.findViewById<View>(R.id.ivEliminar).setOnClickListener {
                mostrarDialogoEliminar(p.nombre) { ll.removeView(v); contar() }
            }
            ll.addView(v)
        }
        contar()

        findViewById<View>(R.id.btnPublicar).setOnClickListener {
            startActivity(Intent(this, PublicarActivity::class.java))
        }
    }
}