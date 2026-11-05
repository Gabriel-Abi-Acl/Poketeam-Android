package com.gabriel.poketeam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.poketeam.data.PokeRepository
import com.gabriel.poketeam.network.PokemonDetail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PokeViewModel(private val repo: PokeRepository) : ViewModel() {
 val lista = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
 val carregando = MutableStateFlow(false)
 val erro = MutableStateFlow<String?>(null)
 val detalhe = MutableStateFlow<PokemonDetail?>(null)
 val usuarioId = MutableStateFlow(0L)
 val email = MutableStateFlow("")
 val favoritos = usuarioId.flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else repo.favoritos(id) }
  .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 fun carregar() = viewModelScope.launch {
  carregando.value = true; erro.value = null
  try { lista.value = repo.listar() } catch (_: Exception) { erro.value = "Não foi possível carregar os Pokémon" }
  finally { carregando.value = false }
 }
 fun abrir(id: Int) = viewModelScope.launch {
  detalhe.value = null; carregando.value = true; erro.value = null
  try { detalhe.value = repo.detalhe(id) } catch (_: Exception) { erro.value = "Não foi possível carregar os detalhes" }
  finally { carregando.value = false }
 }
 suspend fun entrar(email: String, senha: String) = repo.entrar(email, senha)
 suspend fun cadastrar(email: String, senha: String) = repo.cadastrar(email, senha)
 suspend fun ativar(id: Long) { usuarioId.value = id; email.value = repo.usuario(id)?.email.orEmpty() }
 fun sair() { usuarioId.value = 0L; email.value = ""; detalhe.value = null }
 fun alternar(pokemon: Int, existe: Boolean) = viewModelScope.launch {
  try { repo.alternar(usuarioId.value, pokemon, existe); erro.value = null } catch (e: Exception) { erro.value = e.message }
 }
}
