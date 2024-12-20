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
@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData.AvatarDimensions
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData.AvatarDimensions.Default
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData.AvatarDimensions.Small
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.components.ChooseAvatar
import de.gematik.ti.erp.app.profiles.ui.components.color
import de.gematik.ti.erp.app.profiles.ui.components.profileColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.OutlinedIconButton
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.days

@Composable
fun ProfileConnectionSection(
    activeProfile: UiState<ProfilesUseCaseData.Profile>,
    onClickAvatar: () -> Unit,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit
) {
    UiStateMachine(activeProfile) { profile ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Column(modifier = Modifier.weight(0.6f), horizontalAlignment = Alignment.Start) {
                MainScreenAvatar(
                    activeProfile = profile,
                    Small(),
                    onClickAvatar = onClickAvatar
                )
            }
            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                ConnectionHelper(
                    isProfileWithValidSsoTokenScope = profile.isSSOTokenValid(),
                    onClickLogin = onClickLogin,
                    onClickRefresh = onClickRefresh
                )
            }
        }
    }
}

@Composable
fun MainScreenAvatar(
    activeProfile: ProfilesUseCaseData.Profile,
    avatarDimension: AvatarDimensions = Default(),
    onClickAvatar: () -> Unit
) {
    val isTokenValid = activeProfile.isSSOTokenValid()
    val isRegistered = activeProfile.lastAuthenticated != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (avatarDimension) {
            is Small -> {
                AvatarScreen(
                    activeProfile,
                    avatarDimension.dimension,
                    onClickAvatar
                )
                if (isRegistered) {
                    SpacerMedium()
                    var fontColor = AppTheme.colors.neutral600
                    var statusText = stringResource(R.string.not_logged_in)
                    if (isTokenValid) {
                        fontColor = AppTheme.colors.green800
                        statusText = stringResource(R.string.logged_in)
                    }
                    Text(modifier = Modifier.padding(end = PaddingDefaults.Medium), text = statusText, color = fontColor, style = AppTheme.typography.subtitle2)
                }
            }

            is Default -> AvatarScreen(
                activeProfile,
                avatarDimension.dimension,
                onClickAvatar
            )
        }
    }
}

@Composable
fun AvatarScreen(
    profile: ProfilesUseCaseData.Profile,
    avatarDimension: PrescriptionScreenData.AvatarDimension,
    onClickAvatar: () -> Unit
) {
    val selectedColor = profileColor(profileColorNames = profile.color)
    val isTokenValid = profile.isSSOTokenValid()
    Box(
        modifier = Modifier.padding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(avatarDimension.avatarSize),
            shape = CircleShape,
            color = selectedColor.backGroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClickAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    modifier = Modifier.size(avatarDimension.chooseSize),
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    image = profile.image,
                    profileColor = profile.color.color(),
                    avatar = profile.avatar
                )
            }
        }
        if (profile.lastAuthenticated != null) {
            Box(
                modifier = Modifier
                    .size(avatarDimension.statusSize)
                    .align(Alignment.BottomEnd)
                    .offset(avatarDimension.statusOffset.x, avatarDimension.statusOffset.y)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .background(if (isTokenValid) AppTheme.colors.green200 else AppTheme.colors.neutral200)
                    .border(
                        avatarDimension.statusBorder,
                        AppTheme.colors.neutral000,
                        CircleShape
                    )
            ) {
                Icon(
                    if (isTokenValid) Icons.Rounded.Check else Icons.Rounded.Close,
                    null,
                    tint = if (isTokenValid) AppTheme.colors.green500 else AppTheme.colors.neutral600,
                    modifier = Modifier
                        .size(avatarDimension.iconSize)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Requirement(
    "A_24857#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Refreshing the prescription list happens only if the user is authenticated. " +
        "If the user is not authenticated, the user is prompted to authenticate."
)
@Composable
fun ConnectionHelper(
    isProfileWithValidSsoTokenScope: Boolean,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit
) {
    if (isProfileWithValidSsoTokenScope) {
        OutlinedIconButton(
            onClick = onClickRefresh,
            imageVector = Icons.Default.Replay,
            contentDescription = stringResource(R.string.a11y_refresh)
        )
    } else {
        TertiaryButton(onClickLogin) {
            Text(stringResource(R.string.mainscreen_login))
        }
    }
}

@Preview
@Composable
fun ProfileConnectionSectionPreview() {
    PreviewAppTheme {
        ProfileConnectionSection(
            activeProfile = UiState.Data(
                ProfilesUseCaseData.Profile(
                    id = "1",
                    name = "Max Mustermann",
                    insurance = ProfileInsuranceInformation(
                        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                    ),
                    isActive = true,
                    color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                    lastAuthenticated = null,
                    ssoTokenScope = null,
                    avatar = ProfilesData.Avatar.PersonalizedImage,
                    image = null
                )
            ),
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    PreviewAppTheme {
        MainScreenAvatar(
            activeProfile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = null,
                ssoTokenScope = null,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null
            ),
            Small(),
            {}
        )
    }
}

@Preview
@Composable
fun SmallMainScreenAvatarPreview() {
    val singleSignOnToken = IdpData.SingleSignOnToken(
        token = UUID.randomUUID().toString(),
        expiresOn = Clock.System.now().plus(200.days),
        validOn = Clock.System.now().plus(20.days)
    )
    val can = "123123"
    val byteArray = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    val healthCertificate = X509CertificateHolder(byteArray)

    PreviewAppTheme {
        MainScreenAvatar(
            activeProfile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
                ssoTokenScope = IdpData.DefaultToken(
                    token = singleSignOnToken,
                    cardAccessNumber = can,
                    healthCardCertificate = healthCertificate
                ),
                avatar = ProfilesData.Avatar.ManWithPhone,
                image = null
            ),
            Small(),
            {}
        )
    }
}

@Preview
@Composable
fun MainScreenAvatarPreview() {
    val singleSignOnToken = IdpData.SingleSignOnToken(
        token = UUID.randomUUID().toString(),
        expiresOn = Clock.System.now().plus(200.days),
        validOn = Clock.System.now().plus(20.days)
    )
    val can = "123123"
    val byteArray = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    val healthCertificate = X509CertificateHolder(byteArray)

    PreviewAppTheme {
        MainScreenAvatar(
            activeProfile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
                ssoTokenScope = IdpData.DefaultToken(
                    token = singleSignOnToken,
                    cardAccessNumber = can,
                    healthCardCertificate = healthCertificate
                ),
                avatar = ProfilesData.Avatar.ManWithPhone,
                image = null
            ),
            Default(),
            {}
        )
    }
}
