package com.jps.jaysscandy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.UUID

class PublicarActivity : AppCompatActivity() {

    private var productoId: String? = null
    private var selectedFotoUri: String? = null

    private val seleccionarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFotoUri = uri.toString()
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permiso persistente no requerido para la vista previa
            }
            val ivPreviewFoto = findViewById<ImageView>(R.id.ivPreviewFoto)
            ivPreviewFoto.setImageURI(uri)
            ivPreviewFoto.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar)

        productoId = intent.getStringExtra("id")
        val editar = productoId != null || intent.getBooleanExtra("editar", false)
        configurarEncabezado(if (editar) "Editar dulce" else "Publicar dulce")

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)
        val etPresentacion = findViewById<EditText>(R.id.etPresentacion)
        val etStock = findViewById<EditText>(R.id.etStock)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val spCategoria = findViewById<Spinner>(R.id.spCategoria)
        val btnPublicar = findViewById<AppCompatButton>(R.id.btnPublicar)
        val ivPreviewFoto = findViewById<ImageView>(R.id.ivPreviewFoto)

        val categorias = resources.getStringArray(R.array.categorias)
        spCategoria.adapter = crearAdaptadorSpinner(categorias)
        findViewById<View>(R.id.llCategoria).setOnClickListener { spCategoria.performClick() }

        ajustarTeclado(findViewById(R.id.raiz), etNombre, etPrecio, etPresentacion, etStock, etDescripcion)

        if (editar) {
            btnPublicar.text = "Guardar cambios"
            if (intent.hasExtra("nombre")) {
                etNombre.setText(intent.getStringExtra("nombre"))
                val precio = intent.getDoubleExtra("precio", 0.0)
                if (precio > 0) etPrecio.setText(precio.toString())
                etPresentacion.setText(intent.getStringExtra("presentacion"))
                val cat = intent.getStringExtra("categoria") ?: ""
                spCategoria.setSelection(categorias.indexOf(cat).coerceAtLeast(0))
            }

            if (productoId != null) {
                lifecycleScope.launch {
                    val p = Graph.productos.obtenerPorId(productoId!!)
                    if (p != null) {
                        etNombre.setText(p.nombre)
                        etPrecio.setText(p.precio.toString())
                        etPresentacion.setText(p.presentacion)
                        etStock.setText(p.stock.toString())
                        etDescripcion.setText(p.descripcion)
                        spCategoria.setSelection(categorias.indexOf(p.categoria).coerceAtLeast(0))
                        selectedFotoUri = p.fotoUri
                        if (!selectedFotoUri.isNullOrEmpty()) {
                            try {
                                ivPreviewFoto.setImageURI(Uri.parse(selectedFotoUri))
                                ivPreviewFoto.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                // Ignorar si URI no es válida
                            }
                        }
                    }
                }
            }
        }

        findViewById<View>(R.id.btnFoto).setOnClickListener {
            seleccionarFotoLauncher.launch("image/*")
        }

        btnPublicar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val precioStr = etPrecio.text.toString().trim()
            val presentacion = etPresentacion.text.toString().trim()
            val stockStr = etStock.text.toString().trim()
            val descripcionStr = etDescripcion.text.toString().trim()
            val posCat = spCategoria.selectedItemPosition

            when {
                nombre.isEmpty() -> etNombre.error = "Escribe el nombre del dulce"
                precioStr.isEmpty() || (precioStr.toDoubleOrNull() ?: 0.0) <= 0 ->
                    etPrecio.error = "Ingresa un precio válido mayor a 0"
                presentacion.isEmpty() -> etPresentacion.error = "Escribe la presentación"
                posCat == 0 -> Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                else -> {
                    val categoria = spCategoria.selectedItem.toString()
                    val precio = precioStr.toDouble()
                    val stockInt = stockStr.toIntOrNull() ?: 10

                    val (emoji, fondo) = when (categoria) {
                        "Galletas" -> Pair("🍪", R.drawable.bg_cat_galletas)
                        "Golosinas" -> Pair("🧸", R.drawable.bg_cat_golosinas)
                        "Frituras" -> Pair("🍟", R.drawable.bg_cat_frituras)
                        "Chocolates" -> Pair("🍫", R.drawable.bg_cat_chocolates)
                        else -> Pair("🍬", R.drawable.bg_cat_golosinas)
                    }

                    val nuevoId = productoId ?: UUID.randomUUID().toString()
                    val producto = ProductoEntity(
                        id = nuevoId,
                        nombre = nombre,
                        presentacion = presentacion,
                        precio = precio,
                        emoji = emoji,
                        fondo = fondo,
                        categoria = categoria,
                        fotoUri = selectedFotoUri,
                        stock = stockInt,
                        descripcion = descripcionStr
                    )

                    btnPublicar.isEnabled = false
                    lifecycleScope.launch {
                        try {
                            Graph.productos.guardar(producto)
                            Toast.makeText(
                                this@PublicarActivity,
                                if (editar) "Dulce actualizado" else "Dulce publicado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } catch (e: Exception) {
                            btnPublicar.isEnabled = true
                            Toast.makeText(
                                this@PublicarActivity,
                                "Error al guardar dulce: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
    }
}