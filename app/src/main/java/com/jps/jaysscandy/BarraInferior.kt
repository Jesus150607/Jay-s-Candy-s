package com.jps.jaysscandy

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView

// 0 = Carrito, 1 = Inicio, 2 = Perfil
fun Activity.marcarBarra(actual: Int) {
    val iconos = listOf(R.id.ivNavCarrito, R.id.ivNavInicio, R.id.ivNavPerfil)
    val textos = listOf(R.id.tvNavCarrito, R.id.tvNavInicio, R.id.tvNavPerfil)

    iconos.forEachIndexed { i, id ->
        val icono = findViewById<ImageView>(id)
        val texto = findViewById<TextView>(textos[i])
        val activo = i == actual

        icono.setBackgroundResource(if (activo) R.drawable.bg_nav_seleccionado else 0)
        icono.setColorFilter(getColor(if (activo) R.color.blanco else R.color.texto_secundario))
        texto.setTextColor(getColor(if (activo) R.color.azul_principal else R.color.texto_secundario))
    }
}

fun Activity.configurarBarra(actual: Int) {
    marcarBarra(actual)
    findViewById<View>(R.id.navCarrito).setOnClickListener { irA(CarritoActivity::class.java, actual) }
    findViewById<View>(R.id.navPerfil).setOnClickListener { irA(PerfilActivity::class.java, actual) }
    findViewById<View>(R.id.navInicio).setOnClickListener {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }
}

private fun Activity.irA(destino: Class<*>, actual: Int) {
    if (this::class.java == destino) return
    startActivity(Intent(this, destino))
    if (actual != 1) finish() // desde Carrito/Perfil/Favoritos no apilamos pantallas
}