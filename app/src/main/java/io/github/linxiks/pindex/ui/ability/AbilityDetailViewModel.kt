package io.github.linxiks.pindex.ui.ability

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.AbilityDetail
import io.github.linxiks.pindex.data.repository.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AbilityUiState {
    data object Loading : AbilityUiState
    data class Content(val detail: AbilityDetail) : AbilityUiState
    data object NotFound : AbilityUiState
    data object Error : AbilityUiState
}

class AbilityDetailViewModel(
    private val repo: PokemonRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val abilityId: Int = checkNotNull(savedState.get<Int>(ARG_ABILITY_ID))

    private val _uiState = MutableStateFlow<AbilityUiState>(AbilityUiState.Loading)
    val uiState: StateFlow<AbilityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                repo.ability(abilityId)?.let { AbilityUiState.Content(it) } ?: AbilityUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("pindex", "ability load failed for $abilityId", e)
                AbilityUiState.Error
            }
        }
    }

    companion object {
        const val ARG_ABILITY_ID = "abilityId"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                AbilityDetailViewModel(app.container.pokemonRepository, createSavedStateHandle())
            }
        }
    }
}
