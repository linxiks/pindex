package io.github.linxiks.pindex.ui.pokedex

import androidx.lifecycle.SavedStateHandle
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.testutil.FakePokemonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokedexViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsThenFiltersByGeneration() = runTest(dispatcher) {
        val savedState = SavedStateHandle()
        val vm = PokedexViewModel(PokemonRepository(FakePokemonDao()), savedState)
        // WhileSubscribed: keep a collector alive so the combine runs.
        val collector = backgroundScope.launch { vm.uiState.collect() }

        assertEquals(PokedexUiState.Loading, vm.uiState.value)

        advanceUntilIdle()
        val all = vm.uiState.value as PokedexUiState.Content
        assertEquals(listOf(1, 25, 152), all.items.map { it.speciesId })
        assertEquals(0, all.selectedGeneration)
        assertEquals(listOf(1, 2), all.generations.map { it.id })

        vm.selectGeneration(1)
        advanceUntilIdle()
        val gen1 = vm.uiState.value as PokedexUiState.Content
        assertEquals(listOf(1, 25), gen1.items.map { it.speciesId })
        assertEquals(1, gen1.selectedGeneration)
        // Selection lives in SavedStateHandle so it survives process death.
        assertEquals(1, savedState.get<Int>("generation"))

        vm.selectGeneration(0)
        advanceUntilIdle()
        assertTrue((vm.uiState.value as PokedexUiState.Content).items.size == 3)
        collector.cancel()
    }

    @Test
    fun restoresSelectedGenerationFromSavedState() = runTest(dispatcher) {
        val vm = PokedexViewModel(
            PokemonRepository(FakePokemonDao()),
            SavedStateHandle(mapOf("generation" to 2)),
        )
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        val state = vm.uiState.value as PokedexUiState.Content
        assertEquals(2, state.selectedGeneration)
        assertEquals(listOf(152), state.items.map { it.speciesId })
    }
}
