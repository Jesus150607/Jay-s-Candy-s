package com.jps.jaysscandy

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import java.util.UUID

import androidx.room.Delete
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update

enum class Rol { DUENO, USUARIO }

class Converters {
    @TypeConverter
    fun fromRol(rol: Rol?): String = rol?.name ?: Rol.USUARIO.name

    @TypeConverter
    fun toRol(value: String?): Rol = try {
        if (value != null) Rol.valueOf(value) else Rol.USUARIO
    } catch (e: Exception) {
        Rol.USUARIO
    }
}

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["telefono"], unique = true)]
)
data class Usuario(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val telefono: String,      // solo dígitos (10); es el "usuario" para iniciar sesión
    val carrera: String,
    val edificio: String,
    val passwordHash: String,
    val salt: String,
    val fotoUri: String? = null,
    val rol: Rol = Rol.USUARIO,
    val creadoEn: Long = System.currentTimeMillis()
)

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val presentacion: String,
    val precio: Double,
    val emoji: String = "🍬",
    val fondo: Int = R.drawable.bg_cat_golosinas,
    val categoria: String,
    val fotoUri: String? = null,
    val favorito: Boolean = false,
    val stock: Int = 10,
    val descripcion: String = "",
    val creadoEn: Long = System.currentTimeMillis()
) {
    fun toProducto() = Producto(
        id = id,
        nombre = nombre,
        presentacion = presentacion,
        precio = precio,
        emoji = emoji,
        fondo = fondo,
        categoria = categoria,
        favorito = favorito,
        fotoUri = fotoUri,
        stock = stock,
        descripcion = descripcion
    )
}

@Dao
interface UsuarioDao {
    // ABORT: si el teléfono ya existe, lanza SQLiteConstraintException
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE telefono = :telefono LIMIT 1")
    suspend fun porTelefono(telefono: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios WHERE rol = :rol")
    suspend fun contarPorRol(rol: Rol): Int
}

@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoEntity)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("SELECT * FROM productos ORDER BY creadoEn DESC")
    suspend fun obtenerTodos(): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE categoria = :categoria ORDER BY creadoEn DESC")
    suspend fun porCategoria(categoria: String): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): ProductoEntity?

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val total: Double,
    val resumen: String,
    val fecha: Long = System.currentTimeMillis()
)

@Dao
interface PedidoDao {
    @Insert
    suspend fun insertar(pedido: PedidoEntity)

    @Query("SELECT COUNT(*) FROM pedidos WHERE usuarioId = :usuarioId")
    suspend fun contarPorUsuario(usuarioId: String): Int

    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    suspend fun porUsuario(usuarioId: String): List<PedidoEntity>
}

@Database(entities = [Usuario::class, ProductoEntity::class, PedidoEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jays_candys.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
