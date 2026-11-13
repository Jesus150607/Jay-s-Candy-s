package com.jps.jaysscandy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface EstadoAuth {
    object Inactivo : EstadoAuth
    object Cargando : EstadoAuth
    data class SesionIniciada(val rol: Rol) : EstadoAuth
    data class ErroresCampos(val porCampo: Map<Campo, String>) : EstadoAuth
    data class Error(val mensaje: String) : EstadoAuth
}

class AuthViewModel(
    private val repo: UsuarioRepository,
    private val sesion: SesionManager
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoAuth>(EstadoAuth.Inactivo)
    val estado: StateFlow<EstadoAuth> = _estado

    fun registrar(datos: DatosRegistro) {
        if (_estado.value is EstadoAuth.Cargando) return // evita doble toque
        _estado.value = EstadoAuth.Cargando
        viewModelScope.launch {
            _estado.value = when (val r = repo.registrar(datos)) {
                is ResultadoRegistro.Errores -> EstadoAuth.ErroresCampos(r.porCampo)
                is ResultadoRegistro.Exito -> {
                    sesion.guardar(r.usuario.id) // tras registrarse, entra directo
                    EstadoAuth.SesionIniciada(r.usuario.rol)
                }
            }
        }
    }

    fun iniciarSesion(telefono: String, password: String) {
        if (_estado.value is EstadoAuth.Cargando) return
        _estado.value = EstadoAuth.Cargando
        viewModelScope.launch {
            _estado.value = when (val r = repo.iniciarSesion(telefono, password)) {
                is ResultadoLogin.Error -> EstadoAuth.Error(r.mensaje)
                is ResultadoLogin.Exito -> {
                    sesion.guardar(r.usuario.id)
                    EstadoAuth.SesionIniciada(r.usuario.rol)
                }
            }
        }
    }

    /** La pantalla lo llama después de mostrar un error, para no repetirlo al rotar. */
    fun consumirEstado() {
        _estado.value = EstadoAuth.Inactivo
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(Graph.usuarios, Graph.sesion) as T
    }
}
