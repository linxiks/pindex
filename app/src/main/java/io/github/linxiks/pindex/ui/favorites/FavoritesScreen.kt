package io.github.linxiks.pindex.ui.favorites

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.theme.PindexTheme

/** Phase 2 placeholder: favorites need user.db (phase 3). */
@Composable
fun FavoritesScreen() {
    EmptyState(
        title = stringResource(R.string.favorites_empty_title),
        message = stringResource(R.string.favorites_empty_message),
        modifier = Modifier.statusBarsPadding(),
    )
}

@PreviewLightDark
@Composable
private fun FavoritesScreenPreview() {
    PindexTheme { Surface(color = MaterialTheme.colorScheme.background) { FavoritesScreen() } }
}
