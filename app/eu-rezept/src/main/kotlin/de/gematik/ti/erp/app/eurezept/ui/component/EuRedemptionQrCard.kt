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

package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.extensions.dashedBorder
import de.gematik.ti.erp.app.extensions.roundedCornerShape
import de.gematik.ti.erp.app.fhir.temporal.toHourMinuteString
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun EuRedemptionQrCodeCard(
    redemptionData: EuRedemptionDetails,
    onRenewCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SizeDefaults.oneHalf),
        elevation = SizeDefaults.threeSeventyFifth
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpacerMedium()
            Box(
                modifier = Modifier.wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(SizeDefaults.fifteenfoldAndHalf)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(SizeDefaults.one)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    redemptionData.qrCodeBitmap?.let { qrCode ->
                        Image(
                            bitmap = qrCode,
                            contentDescription = stringResource(R.string.eu_redemption_qr_code_content_description),
                            modifier = Modifier
                                .size(SizeDefaults.fifteenfoldAndHalf)
                                .roundedCornerShape()
                        )
                    } ?: Text(
                        text = stringResource(R.string.eu_redemption_qr_code_generating),
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral600
                    )
                }

                // Expired overlay banner
                if (redemptionData.isExpired) {
                    Box(modifier = modifier.width(SizeDefaults.twentyfivefold).rotate(-30f)) {
                        Surface(
                            color = AppTheme.colors.red100,
                            shape = RoundedCornerShape(SizeDefaults.one),
                            modifier = Modifier
                                .fillMaxWidth()
                                .dashedBorder(
                                    color = AppTheme.colors.red700,
                                    shape = RoundedCornerShape(SizeDefaults.one)
                                )
                        ) {
                            Text(
                                modifier = modifier.padding(PaddingDefaults.Tiny),
                                text = stringResource(R.string.eu_redemption_code_expired_text),
                                style = AppTheme.typography.h6,
                                color = AppTheme.colors.red900,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            SpacerMedium()

            // Status message - only show text when active or renewal error
            when {
                !redemptionData.isExpired -> {
                    // Code is active - show remaining time
                    Text(
                        text = stringResource(
                            R.string.eu_redemption_qr_code_remaining_time_message,
                            redemptionData.euAccessCode.validUntil.toLocalDateTime(
                                TimeZone.currentSystemDefault()
                            ).time.toHourMinuteString()
                        ),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.neutral700,
                        textAlign = TextAlign.Center
                    )
                }
            }

            TextButton(
                onClick = onRenewCode
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700,
                        modifier = Modifier.size(SizeDefaults.double)
                    )
                    Text(
                        text = stringResource(R.string.eu_redemption_code_renew),
                        color = AppTheme.colors.primary700,
                        style = AppTheme.typography.body1
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun EuRedemptionQrViewErrorPreview() {
    PreviewTheme {
        EuRedemptionQrCodeCard(
            redemptionData = EuRedemptionDetails(
                euAccessCode = EuAccessCode(
                    countryCode = "IT",
                    accessCode = "123456",
                    validUntil = Instant.DISTANT_PAST,
                    createdAt = Instant.DISTANT_FUTURE,
                    profileIdentifier = "profile1"
                ),
                insuranceNumber = "KVNR 123",
                qrCodeBitmap = ImageBitmap(50, 50)
            ),
            onRenewCode = {}
        )
    }
}
