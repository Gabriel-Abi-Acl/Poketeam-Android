package com.gabriel.poketeam.userinterface

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.gabriel.poketeam.network.PokemonDetail
import com.gabriel.poketeam.viewmodel.PokeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable data object Login
@Serializable data object Cadastro
@Serializable data object Pokedex
@Serializable data object MeuTime
@Serializable data object Perfil
@Serializable data class Detalhe(val id: Int)

private val red = Color(0xFFEF5350)
private fun art(id: Int) = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
private fun label(s: String) = s.replaceFirstChar { it.uppercase() }

@Composable
fun PokeApp(vm: PokeViewModel, prefs: SharedPreferences) {
 val scope = rememberCoroutineScope()
 var ready by remember { mutableStateOf(false) }
 var logged by remember { mutableStateOf(false) }
 LaunchedEffect(Unit) {
  val id = prefs.getLong("userId", 0)
  if (id != 0L) {
   vm.ativar(id)
   logged = vm.email.value.isNotEmpty()
   if (!logged) prefs.edit().remove("userId").apply()
  }
  ready = true
 }
 if (!ready) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
 val nav = rememberNavController()
 val back by nav.currentBackStackEntryAsState()
 val route = back?.destination?.route.orEmpty()
 fun activate(id: Long) {
  scope.launch {
   vm.ativar(id)
   prefs.edit().putLong("userId", id).apply()
   logged = true
   nav.navigate(Pokedex) { popUpTo(0) }
   vm.carregar()
  }
 }
 LaunchedEffect(logged) { if (logged && vm.lista.value.isEmpty()) vm.carregar() }
 Scaffold(bottomBar = {
  if (logged && !route.contains("Detalhe")) NavigationBar {
   listOf(Triple("Pokédex", Pokedex, Icons.Default.Search),
    Triple("Meu time", MeuTime, Icons.Default.Favorite),
    Triple("Perfil", Perfil, Icons.Default.Person)).forEach { (name, dest, icon) ->
    NavigationBarItem(selected = route.contains(dest::class.simpleName.orEmpty()),
     onClick = { nav.navigate(dest) { launchSingleTop = true; popUpTo(Pokedex) { saveState = true }; restoreState = true } },
     icon = { Icon(icon, null) }, label = { Text(name) })
   }
  }
 }) { pad ->
  NavHost(nav, startDestination = if (logged) Pokedex else Login, modifier = Modifier.padding(pad)) {
   composable<Login> { Auth(false, { email, pass -> activate(vm.entrar(email, pass)) }, { nav.navigate(Cadastro) }) }
   composable<Cadastro> { Auth(true, { email, pass -> activate(vm.cadastrar(email, pass)) }, { nav.popBackStack() }) }
   composable<Pokedex> {
    val list by vm.lista.collectAsState()
    val loading by vm.carregando.collectAsState()
    val error by vm.erro.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
     Text("Explore Pokémon", fontSize = 28.sp, fontWeight = FontWeight.Bold)
     var search by remember { mutableStateOf("") }
     OutlinedTextField(search, { search = it }, label = { Text("Buscar por nome") }, modifier = Modifier.fillMaxWidth())
     if (loading && list.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
     else if (error != null && list.isEmpty()) ErrorView(error!!, { vm.carregar() })
     else LazyColumn {
      items(list.filter { it.second.contains(search, true) }) { (id, name) -> PokemonRow(id, name) { nav.navigate(Detalhe(id)) } }
     }
    }
   }
   composable<MeuTime> {
    val fav by vm.favoritos.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
     Text("Meu time", fontSize = 28.sp, fontWeight = FontWeight.Bold)
     Text("${fav.size} de 6 Pokémon", color = red)
     if (fav.isEmpty()) Text("Seu time está vazio. Explore a Pokédex.")
     LazyColumn { items(fav) { item -> PokemonRow(item.pokemonId, "Pokémon #${item.pokemonId}") { nav.navigate(Detalhe(item.pokemonId)) } } }
    }
   }
   composable<Perfil> {
    val email by vm.email.collectAsState()
    val fav by vm.favoritos.collectAsState()
    Column(Modifier.fillMaxSize().padding(24.dp)) {
     Text("Meu perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold)
     Spacer(Modifier.height(24.dp))
     Text(email)
     Text("Pokémon no time: ${fav.size} / 6")
     Spacer(Modifier.height(28.dp))
     Button(onClick = {
      prefs.edit().remove("userId").apply()
      vm.sair(); logged = false
      nav.navigate(Login) { popUpTo(0) }
     }) { Text("Sair da conta") }
    }
   }
   composable<Detalhe> { entry ->
    val id = entry.toRoute<Detalhe>().id
    LaunchedEffect(id) { vm.abrir(id) }
    val detail by vm.detalhe.collectAsState()
    val loading by vm.carregando.collectAsState()
    val error by vm.erro.collectAsState()
    val fav by vm.favoritos.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
     TextButton(onClick = { nav.popBackStack() }) { Text("‹ Voltar") }
     if (loading && detail?.id != id) CircularProgressIndicator()
     else if (detail?.id != id && error != null) ErrorView(error!!, { vm.abrir(id) })
     else if (detail?.id == id) {
      val pokemon = detail!!
      AsyncImage(model = art(id), contentDescription = pokemon.name, modifier = Modifier.fillMaxWidth().height(260.dp))
      Text(label(pokemon.name), fontSize = 30.sp, fontWeight = FontWeight.Bold)
      Text("#%03d".format(id))
      Text("Tipos: ${pokemon.types.joinToString { label(it.type.name) }}")
      Text("Altura: ${pokemon.height / 10.0} m")
      Text("Peso: ${pokemon.weight / 10.0} kg")
      Text("Habilidades: ${pokemon.abilities.joinToString { label(it.ability.name) }}")
      Spacer(Modifier.height(22.dp))
      val saved = fav.any { it.pokemonId == id }
      Button(onClick = { vm.alternar(id, saved) }, modifier = Modifier.fillMaxWidth()) {
       Text(if (saved) "Remover do meu time" else "Adicionar ao meu time")
      }
      if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
     }
    }
   }
  }
 }
}
@Composable
private fun Auth(signup: Boolean, submit: suspend (String, String) -> Unit, switch: () -> Unit) {
 var email by remember { mutableStateOf("") }
 var pass by remember { mutableStateOf("") }
 var error by remember { mutableStateOf<String?>(null) }
 var busy by remember { mutableStateOf(false) }
 val scope = rememberCoroutineScope()
 Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
  Text("◉ PokéTeam", fontSize = 32.sp, color = red, fontWeight = FontWeight.Bold)
  Spacer(Modifier.height(20.dp))
  Text(if (signup) "Criar conta" else "Bem-vindo!", fontSize = 26.sp, fontWeight = FontWeight.Bold)
  Text(if (signup) "Comece sua jornada Pokémon." else "Entre para montar seu time Pokémon.")
  Spacer(Modifier.height(24.dp))
  OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
  OutlinedTextField(pass, { pass = it }, label = { Text("Senha") }, singleLine = true,
   visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
  if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
  Spacer(Modifier.height(16.dp))
  Button(enabled = !busy, onClick = {
   scope.launch {
    busy = true; error = null
    try { submit(email, pass) } catch (e: Exception) { error = e.message ?: "Erro ao entrar" }
    finally { busy = false }
   }
  }, modifier = Modifier.fillMaxWidth()) { Text(if (signup) "Criar conta" else "Entrar") }
  TextButton(onClick = switch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
   Text(if (signup) "Já tenho uma conta" else "Não tem conta? Cadastre-se")
  }
 }
}
@Composable
private fun PokemonRow(id: Int, name: String, open: () -> Unit) {
 Card(onClick = open, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
  Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
   AsyncImage(model = art(id), contentDescription = name, modifier = Modifier.size(70.dp))
   Spacer(Modifier.width(16.dp))
   Column { Text("#%03d".format(id), color = Color.Gray); Text(label(name), fontWeight = FontWeight.Bold) }
   Spacer(Modifier.weight(1f))
   Icon(Icons.Default.ChevronRight, null)
  }
 }
}
@Composable
private fun ErrorView(message: String, retry: () -> Unit) {
 Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
  Text(message)
  Button(onClick = retry) { Text("Tentar novamente") }
 }
}
