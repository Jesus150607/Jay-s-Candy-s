package com.jps.jaysscandy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ProductoRepository {
    suspend fun obtenerTodos(): List<ProductoEntity>
    suspend fun porCategoria(categoria: String): List<ProductoEntity>
    suspend fun obtenerPorId(id: String): ProductoEntity?
    suspend fun guardar(producto: ProductoEntity)
    suspend fun eliminar(id: String)
}

class ProductoRepositoryRoom(private val dao: ProductoDao) : ProductoRepository {
    override suspend fun obtenerTodos(): List<ProductoEntity> = withContext(Dispatchers.IO) {
        dao.obtenerTodos()
    }

    override suspend fun porCategoria(categoria: String): List<ProductoEntity> = withContext(Dispatchers.IO) {
        dao.porCategoria(categoria)
    }

    override suspend fun obtenerPorId(id: String): ProductoEntity? = withContext(Dispatchers.IO) {
        dao.porId(id)
    }

    override suspend fun guardar(producto: ProductoEntity) = withContext(Dispatchers.IO) {
        dao.insertar(producto)
    }

    override suspend fun eliminar(id: String) = withContext(Dispatchers.IO) {
        dao.eliminarPorId(id)
    }
}
