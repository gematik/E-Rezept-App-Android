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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.button.GemButtonDefaults
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall

@Composable
fun EuConsentBottomBar(
    isGrantingConsent: Boolean = false,
    isRevokingConsent: Boolean = false,
    isConsentActive: Boolean? = null,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = isGrantingConsent || isRevokingConsent
    val isAcceptButtonEnabled = !isLoading && isConsentActive != true
    val isDeclineButtonEnabled = !isLoading && isConsentActive != false

    val statusText = when (isConsentActive) {
        true -> stringResource(R.string.eu_consent_status_agreed)
        false -> stringResource(R.string.eu_consent_status_declined)
        null -> stringResource(R.string.eu_consent_profile_settings)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        color = AppTheme.colors.neutral000
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = PaddingDefaults.Medium,
                vertical = PaddingDefaults.Small
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Text(
                text = statusText,
                style = AppTheme.typography.body1,
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.neutral600,
                textAlign = TextAlign.Center
            )

            PrimaryButtonSmall(
                onClick = onAccept,
                enabled = isAcceptButtonEnabled,
                modifier = Modifier
                    .widthIn(min = SizeDefaults.twentyfivefold)
                    .heightIn(min = SizeDefaults.sixfold),
                shape = RoundedCornerShape(SizeDefaults.triple),
                colors = GemButtonDefaults.primaryButtonColors()
            ) {
                if (isGrantingConsent) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(SizeDefaults.double),
                            color = AppTheme.colors.neutral000,
                            strokeWidth = SizeDefaults.quarter
                        )
                        Text(
                            text = stringResource(R.string.eu_consent_accept),
                            style = AppTheme.typography.button
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.eu_consent_accept),
                        style = AppTheme.typography.button
                    )
                }
            }

            PrimaryButtonSmall(
                onClick = onDecline,
                enabled = isDeclineButtonEnabled,
                modifier = Modifier
                    .widthIn(min = SizeDefaults.twentyfivefold)
                    .heightIn(min = SizeDefaults.sixfold),
                shape = RoundedCornerShape(SizeDefaults.triple),
                colors = GemButtonDefaults.primaryButtonColors()
            ) {
                if (isRevokingConsent) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(SizeDefaults.double),
                            color = AppTheme.colors.neutral000,
                            strokeWidth = SizeDefaults.quarter
                        )
                        Text(
                            text = stringResource(R.string.eu_consent_decline),
                            style = AppTheme.typography.button
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.eu_consent_decline),
                        style = AppTheme.typography.button
                    )
                }
            }
            SpacerSmall()
        }
    }
}

@LightDarkPreview
@Composable
fun EuConsentBottomBarPreview() {
    PreviewTheme {
        EuConsentBottomBar(
            isGrantingConsent = false,
            onAccept = {},
            onDecline = {}
        )
    }
}

@LightDarkPreview
@Composable
fun EuConsentBottomBarActivePreview() {
    PreviewTheme {
        EuConsentBottomBar(
            isGrantingConsent = false,
            isConsentActive = true,
            onAccept = {},
            onDecline = {}
        )
    }
}

@LightDarkPreview
@Composable
fun EuConsentBottomBarInactivePreview() {
    PreviewTheme {
        EuConsentBottomBar(
            isGrantingConsent = false,
            isConsentActive = false,
            onAccept = {},
            onDecline = {}
        )
    }
}
