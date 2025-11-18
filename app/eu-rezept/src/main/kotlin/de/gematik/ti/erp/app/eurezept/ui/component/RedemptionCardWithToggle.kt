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
 */

package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled._123
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.button.GemIconButtonDefaults
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.ui.model.RedemptionCodeActions
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.datetime.Instant

@Composable
internal fun TogglableRedemptionCodeCard(
    redemptionData: EuRedemptionDetails,
    isQrCodeVisible: Boolean,
    countrySpecificLabels: CountrySpecificLabels,
    actions: RedemptionCodeActions,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isQrCodeVisible) {
            EuRedemptionQrCodeCard(
                redemptionData = redemptionData,
                onRenewCode = actions.onRenewCode
            )
        } else {
            EuRedemptionCodeCard(
                redemptionData = redemptionData,
                countrySpecificLabels = countrySpecificLabels,
                onPlayInsuranceAudio = actions.onPlayInsuranceAudio,
                onPlayCodeAudio = actions.onPlayCodeAudio,
                onRenewCode = actions.onRenewCode
            )
        }

        QrToggleButton(
            isQrCodeVisible = isQrCodeVisible,
            onToggle = actions.onToggleQrCode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = SizeDefaults.oneHalf, y = -SizeDefaults.triple)
        )
    }
}

@Composable
private fun QrToggleButton(
    isQrCodeVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // val iconSize = if (isQrCodeVisible) SizeDefaults.fourfold else SizeDefaults.doubleHalf
    IconButton(
        onClick = onToggle,
        colors = GemIconButtonDefaults.gemPrimaryIconButtonColors(),
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isQrCodeVisible) Icons.Default._123 else Icons.Default.QrCode,
            contentDescription = stringResource(
                if (isQrCodeVisible) {
                    R.string.eu_redemption_hide_qr_code
                } else {
                    R.string.eu_redemption_show_qr_code
                }
            ),
            modifier = Modifier
        )
    }
}

@LightDarkPreview(name = "Card View")
@Composable
private fun RedemptionCardWithToggleCardPreview() {
    PreviewTheme {
        Box(
            modifier = Modifier.padding(SizeDefaults.tripleHalf)
        ) {
            TogglableRedemptionCodeCard(
                redemptionData = EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_FUTURE,
                        createdAt = Instant.DISTANT_FUTURE,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR 123",
                    qrCodeBitmap = null
                ),
                isQrCodeVisible = false,
                countrySpecificLabels = CountrySpecificLabels(
                    codeLabel = stringResource(R.string.eu_redemption_code_label),
                    insuranceNumberLabel = stringResource(R.string.eu_redemption_insurance_number_label)
                ),
                actions = RedemptionCodeActions(
                    onRenewCode = {},
                    onPlayInsuranceAudio = {},
                    onPlayCodeAudio = {},
                    onToggleQrCode = {}
                )
            )
        }
    }
}

@LightDarkPreview(name = "Card View - QR")
@Composable
private fun RedemptionQRCardWithToggleCardPreview() {
    PreviewTheme {
        Box(
            modifier = Modifier.padding(SizeDefaults.tripleHalf)
        ) {
            TogglableRedemptionCodeCard(
                redemptionData = EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_FUTURE,
                        createdAt = Instant.DISTANT_FUTURE,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR 123",
                    qrCodeBitmap = null
                ),
                isQrCodeVisible = true,
                countrySpecificLabels = CountrySpecificLabels(
                    codeLabel = stringResource(R.string.eu_redemption_code_label),
                    insuranceNumberLabel = stringResource(R.string.eu_redemption_insurance_number_label)
                ),
                actions = RedemptionCodeActions(
                    onRenewCode = {},
                    onPlayInsuranceAudio = {},
                    onPlayCodeAudio = {},
                    onToggleQrCode = {}
                )
            )
        }
    }
}
