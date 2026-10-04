package io.github.linxiks.pindex.ui.pokemon

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.localizedAnnotated
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.FormLink
import io.github.linxiks.pindex.ui.preview.SampleData

/** Up to this many forms show as chips; more use a dropdown. 217 of 224 multi-form species fit. */
private const val FORM_CHIP_MAX = 4

/** Form label, then chips or a dropdown; [onSelect] receives the chosen pokemon id. */
@Composable
internal fun FormSelector(forms: List<FormLink>, selectedId: Int, onSelect: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(
            text = stringResource(R.string.form_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (forms.size <= FORM_CHIP_MAX) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                forms.forEach { form ->
                    FilterChip(
                        selected = form.pokemonId == selectedId,
                        onClick = { onSelect(form.pokemonId) },
                        label = { Text(formLabel(form)) },
                        shape = MaterialTheme.shapes.small,
                    )
                }
            }
        } else {
            FormDropdown(forms, selectedId, onSelect)
        }
    }
}

@Composable
private fun FormDropdown(forms: List<FormLink>, selectedId: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = MaterialTheme.shapes.small) {
            Text(forms.firstOrNull { it.pokemonId == selectedId }?.let { formLabel(it) } ?: AnnotatedString(""))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            forms.forEach { form ->
                DropdownMenuItem(
                    text = { Text(formLabel(form)) },
                    onClick = {
                        expanded = false
                        onSelect(form.pokemonId)
                    },
                )
            }
        }
    }
}

/** Localized form name; the unnamed default form reads "普通"; otherwise the identifier. */
@Composable
private fun formLabel(form: FormLink): AnnotatedString = when {
    form.name != null -> localizedAnnotated(form.name)
    form.isDefault -> AnnotatedString(stringResource(R.string.form_default))
    else -> AnnotatedString(form.identifier)
}

@PreviewLightDark
@Composable
private fun FormSelectorPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FormSelector(SampleData.pikachu.forms, selectedId = 25, onSelect = {})
        }
    }
}
