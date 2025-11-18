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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import de.gematik.ti.erp.app.button.GemIconButtonDefaults
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.extensions.dashedBorder
import de.gematik.ti.erp.app.fhir.temporal.toHourMinuteString
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLargeMedium
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun EuRedemptionCodeCard(
    redemptionData: EuRedemptionDetails,
    countrySpecificLabels: CountrySpecificLabels,
    onPlayInsuranceAudio: () -> Unit,
    onPlayCodeAudio: () -> Unit,
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
                .padding(PaddingDefaults.Medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedIconButton(
                    shape = CircleShape,
                    onClick = onPlayInsuranceAudio,
                    border = GemIconButtonDefaults.gemOutlinedIconButtonBorder(),
                    colors = GemIconButtonDefaults.gemIconButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.eu_redemption_play_audio)
                    )
                }
                SpacerSmall()
                Text(
                    text = countrySpecificLabels.insuranceNumberLabel,
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.primary900
                )
            }

            SpacerMedium()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = AppTheme.colors.neutral100,
                        shape = RoundedCornerShape(SizeDefaults.one)
                    )
                    .padding(vertical = PaddingDefaults.Tiny),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = redemptionData.insuranceNumber,
                    letterSpacing = SizeDefaults.one.value.sp,
                    lineHeight = SizeDefaults.doubleHalf.value.sp,
                    style = AppTheme.typography.h5,
                    color = AppTheme.colors.neutral900,
                    textAlign = TextAlign.End
                )
            }

            SpacerXXLargeMedium()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val redemptionCodeDeactivateText = stringResource(R.string.eu_redemption_play_audio_disabled)

                OutlinedIconButton(
                    shape = CircleShape,
                    enabled = !redemptionData.isExpired,
                    onClick = onPlayCodeAudio,
                    border = GemIconButtonDefaults.gemOutlinedIconButtonBorder(!redemptionData.isExpired),
                    colors = GemIconButtonDefaults.gemIconButtonColors(),
                    modifier = Modifier.also {
                        if (redemptionData.isExpired) {
                            modifier.clearAndSetSemantics {
                                contentDescription = redemptionCodeDeactivateText
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.eu_redemption_play_audio)
                    )
                }

                SpacerSmall()

                Text(
                    text = countrySpecificLabels.codeLabel,
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.primary900
                )
            }

            SpacerMedium()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (redemptionData.isExpired) AppTheme.colors.red100 else AppTheme.colors.primary100,
                        shape = RoundedCornerShape(SizeDefaults.one)
                    )
                    .run {
                        if (redemptionData.isExpired) {
                            dashedBorder(
                                color = AppTheme.colors.red700,
                                shape = RoundedCornerShape(SizeDefaults.one)
                            )
                        } else {
                            this
                        }
                    }
                    .padding(vertical = PaddingDefaults.Tiny),
                contentAlignment = Alignment.Center
            ) {
                if (redemptionData.isExpired) {
                    Text(
                        modifier = modifier.padding(PaddingDefaults.Tiny),
                        text = stringResource(R.string.eu_redemption_code_expired_text),
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.red900,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = redemptionData.euAccessCode.accessCode,
                        letterSpacing = SizeDefaults.doubleHalf.value.sp,
                        lineHeight = SizeDefaults.sixfoldAndQuarter.value.sp,
                        style = AppTheme.typography.h4,
                        color = AppTheme.colors.neutral900,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SpacerTiny()

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status message - only show text when active
                when {
                    !redemptionData.isExpired -> {
                        // Code is active - show remaining time
                        Text(
                            text = stringResource(
                                R.string.eu_redemption_code_remaining_time_message,
                                redemptionData.euAccessCode.validUntil.toLocalDateTime(
                                    TimeZone.currentSystemDefault()
                                ).time.toHourMinuteString()
                            ),
                            style = AppTheme.typography.subtitle2,
                            color = AppTheme.colors.neutral600,
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
}

@LightDarkPreview
@Composable
private fun EuRedemptionCardActivePreview() {
    PreviewTheme {
        EuRedemptionCodeCard(
            redemptionData = EuRedemptionDetails(
                euAccessCode = EuAccessCode(
                    countryCode = "IT",
                    accessCode = "123456",
                    validUntil = Instant.DISTANT_FUTURE,
                    createdAt = Instant.DISTANT_FUTURE,
                    profileIdentifier = "profile1"
                ),
                insuranceNumber = "KVNR123",
                qrCodeBitmap = null
            ),
            countrySpecificLabels = CountrySpecificLabels(
                codeLabel = stringResource(R.string.eu_redemption_code_label),
                insuranceNumberLabel = stringResource(R.string.eu_redemption_insurance_number_label)
            ),
            onPlayInsuranceAudio = {},
            onPlayCodeAudio = {},
            onRenewCode = {}
        )
    }
}
