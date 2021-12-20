package de.gematik.ti.erp.app.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.common.theme.PaddingDefaults

@Composable
fun SpacerLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Large))

@Composable
fun SpacerMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Medium))

@Composable
fun SpacerSmall() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Small))

@Composable
fun SpacerTiny() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Tiny))
