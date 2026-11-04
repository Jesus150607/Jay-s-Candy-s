package com.jps.jaysscandy

import android.app.Activity
import android.graphics.Rect
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

// Hace que el teclado no tape los campos
fun Activity.ajustarTeclado(raiz: View, vararg campos: EditText) {
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
        val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updatePadding(bottom = maxOf(teclado.bottom, barras.bottom))
        insets
    }

    campos.forEach { campo ->
        campo.setOnFocusChangeListener { v, tieneFoco ->
            if (tieneFoco) {
                v.postDelayed({
                    v.requestRectangleOnScreen(Rect(0, 0, v.width, v.height + 200), false)
                }, 300)
            }
        }
    }
}

// Dulce que se envuelve/desenvuelve para ver la contraseña
fun configurarVerPassword(campo: EditText, icono: ImageView) {
    var visible = false
    icono.setImageResource(R.drawable.ic_dulce_cerrado)

    icono.setOnClickListener {
        visible = !visible
        val posicion = campo.selectionEnd

        campo.transformationMethod = if (visible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()
        campo.setSelection(posicion)

        // Animación: el dulce se "voltea" y cambia de imagen
        icono.animate().scaleX(0f).setDuration(120).withEndAction {
            icono.setImageResource(
                if (visible) R.drawable.ic_dulce_abierto else R.drawable.ic_dulce_cerrado
            )
            icono.contentDescription =
                if (visible) "Ocultar contraseña" else "Mostrar contraseña"
            icono.animate().scaleX(1f).setDuration(120).start()
        }.start()
    }
}