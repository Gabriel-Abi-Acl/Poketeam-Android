package com.gabriel.poketeam.userinterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.gabriel.poketeam.data.AppDatabase
import com.gabriel.poketeam.data.PokeRepository
import com.gabriel.poketeam.viewmodel.PokeViewModel

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "poketeam.db").build()
  val repo = PokeRepository(db)
  val prefs = getSharedPreferences("session", MODE_PRIVATE)
  setContent {
   val vm: PokeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PokeViewModel(repo) as T
   })
   MaterialTheme { PokeApp(vm, prefs) }
  }
 }
}
