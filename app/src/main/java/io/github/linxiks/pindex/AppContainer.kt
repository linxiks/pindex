package io.github.linxiks.pindex

import android.content.Context
import androidx.room.Room
import io.github.linxiks.pindex.data.local.PokedexDatabase
import io.github.linxiks.pindex.data.repository.MetaRepository
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.data.repository.SearchRepository

class AppContainer(context: Context) {
    // Destructive fallback: a newer schema_version replaces the whole database from the asset.
    val database: PokedexDatabase = Room.databaseBuilder(context, PokedexDatabase::class.java, "pokedex.db")
        .createFromAsset("pokedex.db")
        .fallbackToDestructiveMigration()
        .build()

    val pokemonRepository = PokemonRepository(database.pokemonDao())
    val searchRepository = SearchRepository(database.searchDao())
    val metaRepository = MetaRepository(database.metaDao())
}
