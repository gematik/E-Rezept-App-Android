package de.gematik.ti.erp.app.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults

@Composable
fun ClosablePopupScaffold(
    onClose: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) =
    Scaffold(
        topBar = {
            Box(Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.padding(PaddingDefaults.Small).size(28.dp).align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Cancel, null, tint = AppTheme.colors.neutral400)
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface,
        content = content
    )
