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
import io.github.linxiks.pindex.domain.MoveMethodGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val detail: PokemonDetail) : DetailUiState
    data object NotFound : DetailUiState
    data object Error : DetailUiState
}

/** User's move-section choice; null / unavailable values fall back in [resolveMoveView]. */
data class MoveSelection(val versionGroupId: Int? = null, val group: MoveMethodGroup = MoveMethodGroup.LevelUp)

class PokemonDetailViewModel(
    private val repo: PokemonRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    // Selected form lives in SavedStateHandle: switching replaces this page instead of pushing a new one.
    private val selectedPokemonId: StateFlow<Int> =
        savedState.getStateFlow(KEY_SELECTED_POKEMON_ID, checkNotNull(savedState.get<Int>(ARG_POKEMON_ID)))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DetailUiState> = selectedPokemonId
        .mapLatest { load(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DetailUiState.Loading)

    // Kept in SavedStateHandle so the choice survives returning to this page.
    val moveSelection: StateFlow<MoveSelection> = combine(
        savedState.getStateFlow<Int?>(KEY_MOVE_VERSION_GROUP, null),
        savedState.getStateFlow(KEY_MOVE_GROUP, MoveMethodGroup.LevelUp.name),
    ) { vg, g -> MoveSelection(vg, MoveMethodGroup.valueOf(g)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MoveSelection())

    fun selectForm(pokemonId: Int) {
        savedState[KEY_SELECTED_POKEMON_ID] = pokemonId
    }

    private suspend fun load(pokemonId: Int): DetailUiState = try {
        repo.detail(pokemonId)?.let { DetailUiState.Content(it) } ?: DetailUiState.NotFound
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("pindex", "detail load failed for $pokemonId", e)
        DetailUiState.Error
    }

    fun selectVersionGroup(id: Int) {
        savedState[KEY_MOVE_VERSION_GROUP] = id
    }

    fun selectMoveGroup(group: MoveMethodGroup) {
        savedState[KEY_MOVE_GROUP] = group.name
    }

    companion object {
        const val ARG_POKEMON_ID = "pokemonId"
        private const val KEY_MOVE_VERSION_GROUP = "moveVersionGroup"
        private const val KEY_MOVE_GROUP = "moveGroup"
        private const val KEY_SELECTED_POKEMON_ID = "selectedPokemonId"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                PokemonDetailViewModel(app.container.pokemonRepository, createSavedStateHandle())
            }
        }
    }
}
