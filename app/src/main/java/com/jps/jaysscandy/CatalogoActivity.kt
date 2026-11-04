package com.jps.jaysscandy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity

class CatalogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(1)

        val categoria = intent.getStringExtra("categoria") ?: "Galletas"
        val lista = Datos.catalogo[categoria] ?: Datos.catalogo.getValue("Galletas")

        configurarEncabezado(categoria, "${lista.size} productos")
        llenarProductos(findViewById<GridLayout>(R.id.gridProductos), lista) { abrirDetalle(it) }

        findViewById<View>(R.id.btnVerCarrito).setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
    }
}