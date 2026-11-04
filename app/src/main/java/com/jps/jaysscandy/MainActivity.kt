package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(1)

        findViewById<View>(R.id.btnMenu).setOnClickListener { mostrarMenuLateral() }
        findViewById<View>(R.id.btnNotificaciones).setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        mapOf(
            R.id.cardGalletas to "Galletas",
            R.id.cardGolosinas to "Golosinas",
            R.id.cardFrituras to "Frituras",
            R.id.cardChocolates to "Chocolates",
            R.id.btnVerCatalogo to "Galletas",
            R.id.btnBuscar to "Galletas"
        ).forEach { (id, categoria) ->
            findViewById<View>(id).setOnClickListener { abrirCatalogo(categoria) }
        }
    }

    private fun abrirCatalogo(categoria: String) {
        startActivity(Intent(this, CatalogoActivity::class.java).putExtra("categoria", categoria))
    }
}