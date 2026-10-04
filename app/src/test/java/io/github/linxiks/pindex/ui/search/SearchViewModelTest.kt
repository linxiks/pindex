package io.github.linxiks.pindex.ui.search

import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.data.repository.SearchRepository
import io.github.linxiks.pindex.testutil.FakePokemonDao
import io.github.linxiks.pindex.testutil.FakeSearchDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SearchViewModel(
        SearchRepository(FakeSearchDao()),
        PokemonRepository(FakePokemonDao()),
    )

    @Test
    fun debouncesThenShowsResults() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        vm.onQueryChange("皮卡")

        advanceTimeBy(149)
        runCurrent()
        assertEquals(SearchUiState.Idle, vm.uiState.value)

        advanceTimeBy(2)
        runCurrent()
        val content = vm.uiState.value as SearchUiState.Content
        assertEquals(listOf(25), content.results.pokemon.map { it.speciesId })
    }

    @Test
    fun noMatchIsEmpty() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onQueryChange("zzzz")
        advanceUntilIdle()
        assertEquals(SearchUiState.Empty, vm.uiState.value)
    }

    @Test
    fun numberQueryAndClearing() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onQueryChange("#025")
        advanceUntilIdle()
        assertEquals(listOf(25), (vm.uiState.value as SearchUiState.Content).results.pokemon.map { it.speciesId })

        vm.onQueryChange("")
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }
}
