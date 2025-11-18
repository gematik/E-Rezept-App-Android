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

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.PrimaryOutlinedButton

@Composable
fun ProfileInsuranceInformationSection(
    selectedProfile: ProfilesUseCaseData.Profile,
    isKVNRCopied: Boolean,
    onClickLogIn: () -> Unit,
    onClickLogOut: () -> Unit,
    onClickChangeInsuranceType: () -> Unit,
    onClickCopy: (ClipData) -> Unit
) {
    val ssoTokenScope = selectedProfile.ssoTokenScope
    val insuranceInformation = selectedProfile.insurance
    val lastAuthenticated = selectedProfile.lastAuthenticated
    val cardAccessNumber =
        if (ssoTokenScope is IdpData.TokenWithHealthCardScope) {
            ssoTokenScope.cardAccessNumber
        } else {
            null
        }

    Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
        Text(
            stringResource(
                id = R.string.insurance_information_header
            ),
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .semanticsHeading(),
            style = AppTheme.typography.h6
        )
        if (lastAuthenticated != null) {
            ProfileWasAuthenticatedBeforeSection(
                insuranceInformation,
                selectedProfile,
                isKVNRCopied,
                cardAccessNumber,
                ssoTokenScope,
                onClickChangeInsuranceType,
                onClickCopy
            )
            if (!selectedProfile.isSSOTokenValid()) {
                PrimaryButtonLarge(
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = PaddingDefaults.Medium),
                    onClick = { onClickLogIn() }
                ) {
                    Text(text = stringResource(id = R.string.profile_screen_login_button))
                }
            } else {
                PrimaryOutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = PaddingDefaults.Medium),
                    onClick = { onClickLogOut() },
                    border = BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.red700)
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_screen_logout_button),
                        color = AppTheme.colors.red700
                    )
                }
            }
        } else {
            ProfileWasNeverAuthenticatedSection(
                insuranceInformation = insuranceInformation,
                onClickChangeInsuranceType = onClickChangeInsuranceType,
                onClickLogIn = onClickLogIn
            )
        }
    }
}

@Composable
private fun ProfileWasAuthenticatedBeforeSection(
    insuranceInformation: ProfileInsuranceInformation,
    selectedProfile: ProfilesUseCaseData.Profile,
    isKVNRCopied: Boolean,
    cardAccessNumber: String?,
    ssoTokenScope: IdpData.SingleSignOnTokenScope?,
    onClickChangeInsuranceType: () -> Unit,
    onClickCopy: (ClipData) -> Unit
) {
    Column {
        ListItem(
            colors = GemListItemDefaults.gemListItemColors(),
            overlineContent = {
                Text(
                    stringResource(R.string.insurance_information_insurant_name),
                    style = AppTheme.typography.body2l
                )
            },
            headlineContent = {
                Text(
                    insuranceInformation.insurantName,
                    style = AppTheme.typography.body1
                )
            }
        )
        InsuranceNameListItem(
            insuranceInformation = insuranceInformation,
            onClickChangeInsuranceType = onClickChangeInsuranceType
        )
        val kvnrString = stringResource(R.string.insurance_information_insurance_identifier)
        val clipData = ClipData.newPlainText(kvnrString, insuranceInformation.insuranceIdentifier)
        ListItem(
            colors = GemListItemDefaults.gemListItemColors(),
            overlineContent = {
                Text(
                    kvnrString,
                    style = AppTheme.typography.body2l
                )
            },
            headlineContent = {
                Text(
                    insuranceInformation.insuranceIdentifier,
                    style = AppTheme.typography.body1
                )
            },
            trailingContent = {
                TextButton(
                    modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        onClickCopy(
                            clipData
                        )
                    }
                ) {
                    if (!isKVNRCopied) {
                        Icon(Icons.Rounded.ContentCopy, null)
                        SpacerTiny()
                        Text(stringResource(R.string.profile_copy_kvnr))
                    } else {
                        Icon(Icons.Rounded.Check, null)
                        SpacerTiny()
                        Text(stringResource(R.string.profile_copied_kvnr))
                    }
                }
            }
        )
        cardAccessNumber?.let {
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                overlineContent = {
                    Text(
                        stringResource(R.string.insurance_information_insurant_can),
                        style = AppTheme.typography.body2l
                    )
                },
                headlineContent = {
                    Text(
                        it,
                        style = AppTheme.typography.body1
                    )
                }
            )
        }
        ListItem(
            colors = GemListItemDefaults.gemListItemColors(),
            overlineContent = {
                Text(
                    stringResource(R.string.profile_insurance_information_connected_label),
                    style = AppTheme.typography.body2l
                )
            },
            headlineContent = {
                Text(
                    when {
                        ssoTokenScope is IdpData.DefaultToken -> stringResource(
                            R.string.profile_insurance_information_connected_health_card
                        )
                        ssoTokenScope is IdpData.ExternalAuthenticationToken -> ssoTokenScope.authenticatorName
                        ssoTokenScope is IdpData.AlternateAuthenticationToken || ssoTokenScope is IdpData.AlternateAuthenticationWithoutToken ->
                            stringResource(
                                R.string.profile_insurance_information_connected_biometrics
                            )
                        !selectedProfile.isSSOTokenValid() -> stringResource(R.string.profile_insurance_information_not_connected)
                        else -> "" // can't be reached
                    },
                    style = AppTheme.typography.body1
                )
            }
        )
    }
}

@Composable
private fun ProfileWasNeverAuthenticatedSection(
    insuranceInformation: ProfileInsuranceInformation,
    onClickChangeInsuranceType: () -> Unit,
    onClickLogIn: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
        InsuranceNameListItem(
            insuranceInformation = insuranceInformation,
            onClickChangeInsuranceType = onClickChangeInsuranceType
        )
        PrimaryButtonLarge(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = PaddingDefaults.Medium),
            onClick = { onClickLogIn() }
        ) {
            Text(text = stringResource(id = R.string.profile_screen_login_button))
        }
    }
}

@Composable
private fun InsuranceNameListItem(
    insuranceInformation: ProfileInsuranceInformation,
    onClickChangeInsuranceType: () -> Unit
) {
    ListItem(
        colors = GemListItemDefaults.gemListItemColors(),
        overlineContent = {
            Text(
                stringResource(R.string.insurance_information_insurance_name),
                style = AppTheme.typography.body2l
            )
        },
        headlineContent = {
            Text(
                when {
                    insuranceInformation.insuranceName.isNotBlank() -> insuranceInformation.insuranceName
                    insuranceInformation.insuranceType == ProfilesUseCaseData.InsuranceType.GKV -> stringResource(
                        R.string.profile_change_insurance_type_drawer_public_insurance_button
                    )
                    insuranceInformation.insuranceType == ProfilesUseCaseData.InsuranceType.PKV -> stringResource(
                        R.string.profile_change_insurance_type_drawer_private_insurance_button
                    )
                    insuranceInformation.insuranceType == ProfilesUseCaseData.InsuranceType.BUND -> stringResource(
                        R.string.profile_change_insurance_type_drawer_bund_insurance_button
                    )
                    else -> stringResource(R.string.profile_change_insurance_type_drawer_no_insurance_selected_button)
                },
                style = AppTheme.typography.body1
            )
        },
        trailingContent = {
            TextButton(onClick = onClickChangeInsuranceType) {
                Icon(Icons.Rounded.Edit, null)
                SpacerTiny()
                Text(stringResource(R.string.edit_profile_insurance_type))
            }
        }
    )
}
