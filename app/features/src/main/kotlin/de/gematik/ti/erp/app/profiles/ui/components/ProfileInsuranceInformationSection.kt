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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.semantics.semanticsMergeDescendants
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall

@Composable
fun ProfileInsuranceInformationSection(
    selectedProfile: ProfilesUseCaseData.Profile,
    onClickLogIn: () -> Unit
) {
    SpacerLarge()
    val ssoTokenScope = selectedProfile.ssoTokenScope
    val insuranceInformation = selectedProfile.insurance
    val lastAuthenticated = selectedProfile.lastAuthenticated

    val cardAccessNumber =
        if (ssoTokenScope is IdpData.TokenWithHealthCardScope) {
            ssoTokenScope.cardAccessNumber
        } else {
            null
        }

    Column {
        Text(
            stringResource(
                id = R.string.insurance_information_header
            ),
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .semanticsHeading(),
            style = AppTheme.typography.h6
        )
        SpacerSmall()

        if (lastAuthenticated != null) {
            LabeledText(
                stringResource(R.string.insurance_information_insurant_name),
                insuranceInformation.insurantName
            )
            LabeledText(
                stringResource(R.string.insurance_information_insurance_name),
                insuranceInformation.insuranceName
            )
            cardAccessNumber?.let {
                LabeledText(stringResource(R.string.insurance_information_insurant_can), it)
            }
            LabeledText(
                stringResource(R.string.insurance_information_insurance_identifier),
                insuranceInformation.insuranceIdentifier,
                Modifier.testTag(TestTag.Profile.InsuranceId)
            )

            LabeledText(
                stringResource(R.string.profile_insurance_information_connected_label),
                when {
                    ssoTokenScope is IdpData.DefaultToken -> stringResource(
                        R.string.profile_insurance_information_connected_health_card
                    )
                    ssoTokenScope is IdpData.ExternalAuthenticationToken -> ssoTokenScope.authenticatorName
                    ssoTokenScope is IdpData.AlternateAuthenticationToken || ssoTokenScope is IdpData.AlternateAuthenticationWithoutToken -> stringResource(
                        R.string.profile_insurance_information_connected_biometrics
                    )
                    !selectedProfile.isSSOTokenValid() -> stringResource(R.string.profile_insurance_information_not_connected)
                    else -> "" // can't be reached
                }
            )

            if (!selectedProfile.isSSOTokenValid()) {
                PrimaryButtonLarge(
                    modifier = Modifier
                        .padding(
                            top = PaddingDefaults.Medium,
                            start = PaddingDefaults.Medium,
                            end = PaddingDefaults.Medium
                        )
                        .fillMaxWidth(),
                    onClick = { onClickLogIn() }
                ) {
                    Text(text = stringResource(id = R.string.profile_screen_login_button))
                }
            }
        } else {
            LoginHintCard {
                onClickLogIn()
            }
        }
        SpacerLarge()
        Divider()
        SpacerLarge()
    }
}

/**
 * Shows the given content if != null labeled with a description as described in design guide for ProfileScreen.
 */
@Composable
private fun LabeledText(
    description: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .padding(PaddingDefaults.Medium)
            .semanticsMergeDescendants { }
    ) {
        Text(description, style = AppTheme.typography.body2l)
        Text(content, style = AppTheme.typography.body1)
    }
}

@Composable
private fun LoginHintCard(
    onClickLogIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(
                top = PaddingDefaults.Small,
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
            .clip(RoundedCornerShape(SizeDefaults.double))
            .border(
                SizeDefaults.eighth,
                AppTheme.colors.neutral300,
                RoundedCornerShape(SizeDefaults.double)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.profile_screen_login_hint),
            style = AppTheme.typography.body1,
            modifier = Modifier.padding(PaddingDefaults.Medium)
        )
        PrimaryButtonSmall(
            onClick = { onClickLogIn() }
        ) {
            Text(text = stringResource(id = R.string.profile_screen_login_button))
        }
        SpacerMedium()
    }
}
