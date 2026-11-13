package com.jps.jaysscandy

import android.animation.ValueAnimator
import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PantallaCarga : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.iniciar(this)
        setContentView(R.layout.activity_pantalla_carga)

        val pbCarga = findViewById<SeekBar>(R.id.pbCarga)
        val tvPorcentaje = findViewById<TextView>(R.id.tvPorcentaje)

        // La barra es solo visual: no se puede arrastrar
        pbCarga.setOnTouchListener { _, _ -> true }

        ValueAnimator.ofInt(0, 100).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val valor = it.animatedValue as Int
                pbCarga.progress = valor
                tvPorcentaje.text = "$valor%"
            }
            doOnEnd { irSiguientePantalla() }
            start()
        }
    }

    private fun irSiguientePantalla() {
        if (isFinishing || isDestroyed) return
        lifecycleScope.launch {
            try {
                Graph.iniciar(this@PantallaCarga)
                val id = Graph.sesion.usuarioId()
                val usuario = id?.let { Graph.usuarios.obtenerPorId(it) }
                val destino = when {
                    usuario?.rol == Rol.DUENO -> PanelVendedorActivity::class.java
                    usuario != null -> MainActivity::class.java
                    else -> InicioSesion::class.java
                }
                startActivity(Intent(this@PantallaCarga, destino))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                startActivity(Intent(this@PantallaCarga, InicioSesion::class.java))
                finish()
            }
        }
    }
}