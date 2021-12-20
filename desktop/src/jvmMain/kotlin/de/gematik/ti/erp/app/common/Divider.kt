package de.gematik.ti.erp.app.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.theme.AppTheme

@Composable
fun HorizontalDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.neutral300))
}

@Composable
fun VerticalDivider() {
    Box(Modifier.fillMaxHeight().width(1.dp).background(AppTheme.colors.neutral300))
}
