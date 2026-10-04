package io.github.linxiks.pindex.ui.pokemon

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.data.repository.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val detail: PokemonDetail) : DetailUiState
    data object NotFound : DetailUiState
    data object Error : DetailUiState
}

class PokemonDetailViewModel(
    private val repo: PokemonRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val pokemonId: Int = checkNotNull(savedState.get<Int>(ARG_POKEMON_ID))

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                repo.detail(pokemonId)?.let { DetailUiState.Content(it) } ?: DetailUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("pindex", "detail load failed for $pokemonId", e)
                DetailUiState.Error
            }
        }
    }

    companion object {
        const val ARG_POKEMON_ID = "pokemonId"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                PokemonDetailViewModel(app.container.pokemonRepository, createSavedStateHandle())
            }
        }
    }
}
