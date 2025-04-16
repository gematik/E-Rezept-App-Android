/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import de.gematik.ti.erp.app.animated.AnimationTime
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.delay

@Composable
fun DataMatrixCodesWithSelfPayerWarning(
    dmCode: DMCode,
    showSingleCodes: Boolean,
    sharedWarningHeight: Int,
    onSharedWarningHeightUpdated: (Int) -> Unit
) {
    var heightIsVisible by remember { mutableStateOf(true) }

    // Animate the height of the spacer
    val animatedSpacerHeight by animateDpAsState(
        label = "DataMatrixCodesWithSelfPayerWarningSpacerHeight",
        targetValue = if (dmCode.selfPayerPrescriptionNames.isEmpty() && heightIsVisible) {
            with(LocalDensity.current) { sharedWarningHeight.toDp() }
        } else {
            SizeDefaults.zero
        },
        animationSpec = tween()
    )

    LaunchedEffect(Unit) {
        if (dmCode.selfPayerPrescriptionNames.isEmpty()) {
            delay(AnimationTime.SHORT_DELAY)
            heightIsVisible = false
            onSharedWarningHeightUpdated(0)
        }
    }

    Column(
        modifier = Modifier.animateContentSize()
    ) {
        AnimatedVisibility(
            visible = dmCode.selfPayerPrescriptionNames.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            SelfPayerPrescriptionWarning(
                modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                    val height = layoutCoordinates.size.height
                    // Update the shared state with the new height if it's larger
                    if (height > sharedWarningHeight) {
                        onSharedWarningHeightUpdated(height)
                    }
                },
                selfPayerPrescriptionNames = dmCode.selfPayerPrescriptionNames,
                showSingleCodes = showSingleCodes,
                nrOfDMCodes = dmCode.nrOfCodes
            )
        }
        Spacer(modifier = Modifier.height(animatedSpacerHeight))
        DataMatrixCode(
            modifier = Modifier
                .padding(PaddingDefaults.Medium)
                .fillMaxWidth(),
            code = dmCode
        )
    }
}

@LightDarkPreview
@Composable
fun DataMatrixCodesWithSelfPayerWarningPreview() {
    PreviewAppTheme {
        DataMatrixCodesWithSelfPayerWarning(
            dmCode = DMCode(
                payload = "1213233647678679789790",
                nrOfCodes = 1,
                name = "Medication",
                selfPayerPrescriptionNames = listOf("Medication"),
                containsScanned = false
            ),
            showSingleCodes = false,
            sharedWarningHeight = 0,
            onSharedWarningHeightUpdated = {}
        )
    }
}
