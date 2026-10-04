package io.github.linxiks.pindex.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.linxiks.pindex.PindexApp
import io.github.linxiks.pindex.data.model.DataVersion
import io.github.linxiks.pindex.data.repository.MetaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val metaRepo: MetaRepository) : ViewModel() {
    private val _dataVersion = MutableStateFlow<DataVersion?>(null)
    val dataVersion: StateFlow<DataVersion?> = _dataVersion.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _dataVersion.value = metaRepo.dataVersion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("pindex", "meta load failed", e)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PindexApp
                SettingsViewModel(app.container.metaRepository)
            }
        }
    }
}
