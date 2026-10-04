package io.github.linxiks.pindex.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.data.repository.SearchRepository
import io.github.linxiks.pindex.domain.SearchQuery
import io.github.linxiks.pindex.domain.normalizeQuery
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Content(val items: List<PokemonListItem>) : SearchUiState
    data object Empty : SearchUiState
    data object Error : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchRepo: SearchRepository,
    private val pokemonRepo: PokemonRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // No Loading state: the previous result stays visible while the debounce runs.
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(DEBOUNCE_MS)
        .mapLatest { search(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState.Idle)

    fun onQueryChange(text: String) {
        _query.value = text
    }

    private suspend fun search(text: String): SearchUiState {
        val query = normalizeQuery(text)
        if (query == SearchQuery.Blank) return SearchUiState.Idle
        return try {
            val ids = searchRepo.searchSpecies(query)
            val bySpecies = pokemonRepo.list().associateBy { it.speciesId }
            val items = ids.mapNotNull { bySpecies[it] }
            if (items.isEmpty()) SearchUiState.Empty else SearchUiState.Content(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("pindex", "search failed", e)
            SearchUiState.Error
        }
    }

    companion object {
        const val DEBOUNCE_MS = 150L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                SearchViewModel(app.container.searchRepository, app.container.pokemonRepository)
            }
        }
    }
}
