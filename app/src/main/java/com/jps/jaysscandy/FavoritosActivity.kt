package com.jps.jaysscandy

import android.os.Bundle
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity

class FavoritosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        ajustarBarraSistema(findViewById(R.id.raiz))
        configurarBarra(2)
        configurarEncabezado("Mis favoritos", "${Datos.favoritos.size} productos guardados")
        llenarProductos(findViewById<GridLayout>(R.id.gridProductos), Datos.favoritos) { abrirDetalle(it) }
    }
}