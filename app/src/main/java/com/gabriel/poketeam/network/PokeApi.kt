package com.gabriel.poketeam.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class PokemonPage(val results: List<PokemonRef>)
data class PokemonRef(val name: String, val url: String)
data class PokemonDetail(val id: Int, val name: String, val height: Int, val weight: Int, val types: List<TypeSlot>, val abilities: List<AbilitySlot>)
data class TypeSlot(val type: NamedResource)
data class AbilitySlot(val ability: NamedResource)
data class NamedResource(val name: String)
interface PokeApi {
 @GET("pokemon") suspend fun list(@Query("limit") limit: Int = 151): PokemonPage
 @GET("pokemon/{id}") suspend fun detail(@Path("id") id: Int): PokemonDetail
}
object ApiClient {
 val api: PokeApi = Retrofit.Builder().baseUrl("https://pokeapi.co/api/v2/")
  .addConverterFactory(GsonConverterFactory.create()).build().create(PokeApi::class.java)
}
