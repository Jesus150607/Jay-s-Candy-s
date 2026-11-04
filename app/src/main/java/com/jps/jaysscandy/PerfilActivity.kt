package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(2)

        val llDatos = findViewById<LinearLayout>(R.id.llDatos)
        listOf(
            Triple("Número", "55 1234 5678", R.drawable.ic_telefono),
            Triple("Carrera", "Ing. en Sistemas", R.drawable.ic_birrete),
            Triple("Edificio", "Edificio B", R.drawable.ic_edificio)
        ).forEach { (etiqueta, valor, icono) ->
            val v = layoutInflater.inflate(R.layout.item_dato, llDatos, false)
            v.findViewById<ImageView>(R.id.ivIconoDato).setImageResource(icono)
            v.findViewById<TextView>(R.id.tvEtiquetaDato).text = etiqueta
            v.findViewById<TextView>(R.id.tvValorDato).text = valor
            v.setOnClickListener { startActivity(Intent(this, EditarDatosActivity::class.java)) }
            llDatos.addView(v)
        }

        findViewById<View>(R.id.btnEditarPerfil).setOnClickListener {
            startActivity(Intent(this, EditarDatosActivity::class.java))
        }
        findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            startActivity(
                Intent(this, InicioSesion::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        // TEMPORAL: dejar presionado el avatar abre el panel del vendedor
        findViewById<View>(R.id.tvAvatarGrande).setOnLongClickListener {
            startActivity(Intent(this, PanelVendedorActivity::class.java))
            true
        }
    }
}