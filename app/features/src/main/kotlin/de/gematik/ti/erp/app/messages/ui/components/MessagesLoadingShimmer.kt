/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.shimmer.MessageOverviewItemShimmer
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun MessagesLoadingShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shimmer()
    ) {
        SpacerLarge()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
        MessageOverviewItemShimmer()
    }
}

@LightDarkPreview
@Composable
fun MessagesLoadingPreview() {
    PreviewAppTheme {
        MessagesLoadingShimmer()
    }
}
