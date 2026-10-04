package io.github.linxiks.pindex.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.DataVersion

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val dataVersion by viewModel.dataVersion.collectAsStateWithLifecycle()
    SettingsContent(dataVersion)
}

@Composable
fun SettingsContent(dataVersion: DataVersion?) {
    val itemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(Spacing.l),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_data_version)) },
            supportingContent = {
                Text(
                    dataVersion?.let {
                        stringResource(R.string.settings_data_version_value, it.dataVersion, it.buildDate)
                    } ?: stringResource(R.string.value_missing),
                )
            },
            colors = itemColors,
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_about)) },
            supportingContent = { Text(stringResource(R.string.settings_about_text)) },
            colors = itemColors,
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsContent(DataVersion("1", "2026-10-03"))
        }
    }
}
