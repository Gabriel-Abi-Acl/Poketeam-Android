package com.gabriel.poketeam.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(indices = [Index(value = ["email"], unique = true)])
data class Usuario(@PrimaryKey(autoGenerate = true) val id: Long = 0, val email: String, val senhaHash: String)
@Entity(primaryKeys = ["usuarioId", "pokemonId"])
data class Favorito(val usuarioId: Long, val pokemonId: Int)
@Dao interface UsuarioDao {
 @Insert suspend fun inserir(usuario: Usuario): Long
 @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1") suspend fun buscar(email: String): Usuario?
 @Query("SELECT * FROM Usuario WHERE id = :id LIMIT 1") suspend fun porId(id: Long): Usuario?
}
@Dao interface FavoritoDao {
 @Query("SELECT * FROM Favorito WHERE usuarioId = :id ORDER BY pokemonId") fun observar(id: Long): Flow<List<Favorito>>
 @Query("SELECT COUNT(*) FROM Favorito WHERE usuarioId = :id") suspend fun contar(id: Long): Int
 @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun inserir(favorito: Favorito)
 @Query("DELETE FROM Favorito WHERE usuarioId = :id AND pokemonId = :pokemon") suspend fun remover(id: Long, pokemon: Int)
}
@Database(entities = [Usuario::class, Favorito::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
 abstract fun usuarios(): UsuarioDao
 abstract fun favoritos(): FavoritoDao
}
