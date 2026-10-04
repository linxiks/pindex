package io.github.linxiks.pindex.ui.move

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.MoveDetail
import io.github.linxiks.pindex.data.repository.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MoveUiState {
    data object Loading : MoveUiState
    data class Content(val detail: MoveDetail) : MoveUiState
    data object NotFound : MoveUiState
    data object Error : MoveUiState
}

class MoveDetailViewModel(
    private val repo: PokemonRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val moveId: Int = checkNotNull(savedState.get<Int>(ARG_MOVE_ID))

    private val _uiState = MutableStateFlow<MoveUiState>(MoveUiState.Loading)
    val uiState: StateFlow<MoveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                repo.move(moveId)?.let { MoveUiState.Content(it) } ?: MoveUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("pindex", "move load failed for $moveId", e)
                MoveUiState.Error
            }
        }
    }

    companion object {
        const val ARG_MOVE_ID = "moveId"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                MoveDetailViewModel(app.container.pokemonRepository, createSavedStateHandle())
            }
        }
    }
}
