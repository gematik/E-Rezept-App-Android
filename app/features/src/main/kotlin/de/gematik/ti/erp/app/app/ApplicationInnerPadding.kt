/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import de.gematik.ti.erp.app.theme.SizeDefaults

/**
 * Since we have a Scaffold for the app and we have multiple scaffolds inside it,
 * we use the [ApplicationInnerPadding] to combine them both
 */
data class ApplicationInnerPadding(
    val layoutDirection: LayoutDirection,
    val applicationScaffoldPadding: PaddingValues = PaddingValues()
) {
    fun combineWithInnerScaffold(
        innerScaffoldPaddingValues: PaddingValues
    ): PaddingValues {
        val top = innerScaffoldPaddingValues.calculateTopPadding() +
            applicationScaffoldPadding.calculateTopPadding()

        // in scenarios where the padding is not set, we use a default value
        val bottom: Dp by lazy {
            val calculatedPadding = innerScaffoldPaddingValues.calculateBottomPadding() + applicationScaffoldPadding.calculateBottomPadding()
            if (calculatedPadding == SizeDefaults.zero) {
                SizeDefaults.tenfold // 80.dp
            } else {
                calculatedPadding
            }
        }

        val start = innerScaffoldPaddingValues.calculateStartPadding(layoutDirection) +
            applicationScaffoldPadding.calculateStartPadding(layoutDirection)

        val end = innerScaffoldPaddingValues.calculateEndPadding(layoutDirection) +
            applicationScaffoldPadding.calculateEndPadding(layoutDirection)

        return PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottom
        )
    }
}
