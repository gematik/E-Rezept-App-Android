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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.shimmer.LimitedTextShimmer
import de.gematik.ti.erp.app.shimmer.TinyTextShimmer
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
private fun AuditEventsLoadingItem() {
    Column(
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        LimitedTextShimmer(modifier = Modifier.fillMaxWidth())
        LimitedTextShimmer(modifier = Modifier.fillMaxWidth())
        TinyTextShimmer()
        LimitedTextShimmer()
        SpacerLarge()
    }
}

@Composable
fun AuditEventsLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
            .shimmer()
    ) {
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
        AuditEventsLoadingItem()
    }
}

@LightDarkPreview
@Composable
fun AuditEventsLoadingPreview() {
    PreviewAppTheme {
        AuditEventsLoading()
    }
}
