package com.jps.jaysscandy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditarDatosActivity : AppCompatActivity() {

    private var usuarioActual: Usuario? = null
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
            val ivFotoPerfil = findViewById<ImageView>(R.id.ivFotoPerfil)
            val tvAvatarLetra = findViewById<TextView>(R.id.tvAvatarLetra)
            ivFotoPerfil.setImageURI(uri)
            ivFotoPerfil.visibility = View.VISIBLE
            tvAvatarLetra.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_datos)

        configurarEncabezado("Editar datos")

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val spEdificio = findViewById<Spinner>(R.id.spEdificio)
        val llEdificio = findViewById<LinearLayout>(R.id.llEdificio)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val edificios = resources.getStringArray(R.array.edificios)
        spEdificio.adapter = crearAdaptadorSpinner(edificios)
        llEdificio.setOnClickListener { spEdificio.performClick() }

        findViewById<View>(R.id.btnCambiarFoto)?.setOnClickListener {
            seleccionarFotoLauncher.launch("image/*")
        }

        ajustarTeclado(findViewById(R.id.raiz), etNombre, etTelefono, etPassword)
        configurarVerPassword(etPassword, findViewById<ImageView>(R.id.ivVerPassword))

        lifecycleScope.launch {
            val id = Graph.sesion.usuarioId()
            usuarioActual = id?.let { Graph.usuarios.obtenerPorId(it) }
            usuarioActual?.let { u ->
                etNombre.setText(u.nombre)
                etTelefono.setText(u.telefono)
                val pos = edificios.indexOf(u.edificio)
                if (pos >= 0) spEdificio.setSelection(pos)

                selectedFotoUri = u.fotoUri
                if (!selectedFotoUri.isNullOrEmpty()) {
                    try {
                        val ivFotoPerfil = findViewById<ImageView>(R.id.ivFotoPerfil)
                        val tvAvatarLetra = findViewById<TextView>(R.id.tvAvatarLetra)
                        ivFotoPerfil?.setImageURI(Uri.parse(selectedFotoUri))
                        ivFotoPerfil?.visibility = View.VISIBLE
                        tvAvatarLetra?.visibility = View.GONE
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }

        findViewById<View>(R.id.btnGuardar).setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoTel = etTelefono.text.toString().trim()
            val nuevoEdificio = if (spEdificio.selectedItemPosition > 0) spEdificio.selectedItem.toString() else (usuarioActual?.edificio ?: "")
            val nuevaPass = etPassword.text.toString()

            when {
                nuevoNombre.isEmpty() -> etNombre.error = "Escribe tu nombre"
                nuevoTel.length != 10 -> etTelefono.error = "Escribe tu número de 10 dígitos"
                nuevaPass.isNotEmpty() && nuevaPass.length < 6 -> etPassword.error = "Mínimo 6 caracteres"
                else -> {
                    val u = usuarioActual
                    if (u != null) {
                        val (passHash, sal) = if (nuevaPass.isNotEmpty()) {
                            val nuevaSal = Seguridad.nuevaSal()
                            Pair(Seguridad.hash(nuevaPass, nuevaSal), nuevaSal)
                        } else {
                            Pair(u.passwordHash, u.salt)
                        }

                        val actualizado = u.copy(
                            nombre = nuevoNombre,
                            telefono = Validador.normalizarTelefono(nuevoTel),
                            edificio = nuevoEdificio,
                            passwordHash = passHash,
                            salt = sal,
                            fotoUri = selectedFotoUri
                        )

                        lifecycleScope.launch {
                            Graph.usuarios.actualizar(actualizado)
                            Toast.makeText(this@EditarDatosActivity, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        finish()
                    }
                }
            }
        }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
    }
}