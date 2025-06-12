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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.redeem.model.RedeemEventModel
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge

// A composable button that can handle the redemption process and gives the user feedback about the process
@Composable
fun RedeemButton(
    isEnabled: Boolean,
    processStateEvent: RedeemEventModel.ProcessStateEvent,
    onClickRedeem: () -> Unit
) {
    var uploadInProgress by remember { mutableStateOf(false) }

    processStateEvent.processStartedEvent.listen {
        uploadInProgress = true
        processStateEvent.onProcessStarted()
    }

    processStateEvent.processEndEvent.listen {
        uploadInProgress = false
        processStateEvent.onProcessEnded()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = SizeDefaults.half
    ) {
        Column(Modifier.navigationBarsPadding()) {
            SpacerMedium()
            PrimaryButtonLarge(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton),
                enabled = isEnabled && !uploadInProgress,
                onClick = onClickRedeem
            ) {
                Text(stringResource(R.string.pharmacy_order_send))
            }
            SpacerXLarge()
        }
    }
}
