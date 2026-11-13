package com.jps.jaysscandy

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Pregunta antes de salir. Úsalo desde cualquier pantalla: confirmarCerrarSesion() */
fun AppCompatActivity.confirmarCerrarSesion() {
    AlertDialog.Builder(this)
        .setTitle("Cerrar sesión")
        .setMessage("¿Seguro que quieres salir de tu cuenta?")
        .setPositiveButton("Cerrar sesión") { _, _ -> cerrarSesion() }
        .setNegativeButton("Cancelar", null)
        .show()
}

/** Borra la sesión y vuelve al login sin dejar pantallas atrás (el botón Atrás ya no regresa). */
fun AppCompatActivity.cerrarSesion() {
    Graph.sesion.cerrar()
    startActivity(
        Intent(this, InicioSesion::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}
