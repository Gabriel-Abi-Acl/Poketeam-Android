package com.gabriel.poketeam.data

import android.util.Patterns
import com.gabriel.poketeam.network.*
import java.security.MessageDigest

class PokeRepository(private val db: AppDatabase, private val api: PokeApi = ApiClient.api) {
 private fun hash(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
 suspend fun cadastrar(email: String, senha: String): Long {
  val e = email.trim().lowercase()
  require(Patterns.EMAIL_ADDRESS.matcher(e).matches()) { "E-mail inválido" }
  require(senha.length >= 6) { "A senha deve ter pelo menos 6 caracteres" }
  require(db.usuarios().buscar(e) == null) { "E-mail já cadastrado" }
  return db.usuarios().inserir(Usuario(email = e, senhaHash = hash(senha)))
 }
 suspend fun entrar(email: String, senha: String): Long {
  val u = db.usuarios().buscar(email.trim().lowercase())
  require(u != null && u.senhaHash == hash(senha)) { "E-mail ou senha inválidos" }
  return u.id
 }
 suspend fun usuario(id: Long) = db.usuarios().porId(id)
 suspend fun listar() = api.list().results.mapIndexed { i, p -> (i + 1) to p.name }
 suspend fun detalhe(id: Int) = api.detail(id)
 fun favoritos(id: Long) = db.favoritos().observar(id)
 suspend fun alternar(id: Long, pokemon: Int, existe: Boolean) {
  if (existe) db.favoritos().remover(id, pokemon)
  else { require(db.favoritos().contar(id) < 6) { "Seu time já tem 6 Pokémon" }; db.favoritos().inserir(Favorito(id, pokemon)) }
 }
}
