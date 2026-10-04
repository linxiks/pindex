package io.github.linxiks.pindex.ui.pokedex

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.domain.filterByGeneration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PokedexUiState {
    data object Loading : PokedexUiState
    data class Content(
        val items: List<PokemonListItem>,
        val generations: List<Generation>,
        val selectedGeneration: Int,
    ) : PokedexUiState
    data object Error : PokedexUiState
}

private sealed interface Loaded {
    data object Pending : Loaded
    data class Data(val items: List<PokemonListItem>, val generations: List<Generation>) : Loaded
    data object Failed : Loaded
}

class PokedexViewModel(
    private val repo: PokemonRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val loaded = MutableStateFlow<Loaded>(Loaded.Pending)

    /** 0 = all generations. */
    private val selected = savedState.getStateFlow(KEY_GENERATION, 0)

    val uiState: StateFlow<PokedexUiState> = combine(loaded, selected) { data, generation ->
        when (data) {
            Loaded.Pending -> PokedexUiState.Loading
            Loaded.Failed -> PokedexUiState.Error
            is Loaded.Data -> PokedexUiState.Content(
                items = filterByGeneration(data.items, generation) { it.generationId },
                generations = data.generations,
                selectedGeneration = generation,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PokedexUiState.Loading)

    init {
        viewModelScope.launch {
            loaded.value = try {
                Loaded.Data(repo.list(), repo.generations())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("pindex", "pokedex load failed", e)
                Loaded.Failed
            }
        }
    }

    fun selectGeneration(id: Int) {
        savedState[KEY_GENERATION] = id
    }

    companion object {
        private const val KEY_GENERATION = "generation"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                PokedexViewModel(app.container.pokemonRepository, createSavedStateHandle())
            }
        }
    }
}
