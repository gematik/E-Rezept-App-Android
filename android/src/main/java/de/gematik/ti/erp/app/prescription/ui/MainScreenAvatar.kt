/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.profiles.ui.ChooseAvatar
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.profileColor
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.TertiaryButton

@Composable
fun SmallMainScreenAvatar(onClickAvatar: () -> Unit) {
    val profileHandler = LocalProfileHandler.current
    val profile = profileHandler.activeProfile
    val ssoTokenScope = profile.ssoTokenScope

    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClickAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    iconModifier = Modifier.size(16.dp),
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    profile = profile,
                    figure = profile.avatarFigure
                )
            }
        }
        if (profile.lastAuthenticated != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .offset(8.dp, 8.dp)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .border(
                        2.dp,
                        AppTheme.colors.neutral000,
                        CircleShape
                    )

            ) {
                when {
                    ssoTokenScope?.token?.isValid() == true -> {
                        Image(
                            painterResource(R.drawable.main_screen_erx_icon_small),
                            null,
                            modifier = Modifier.offset(2.dp, 2.dp)
                        )
                    }
                    else -> {
                        Image(
                            painterResource(R.drawable.main_screen_erx_icon_gray_small),
                            null,
                            modifier = Modifier.offset(2.dp, 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenAvatar(onClickAvatar: () -> Unit) {
    val profileHandler = LocalProfileHandler.current
    val profile = profileHandler.activeProfile
    val ssoTokenScope = profile.ssoTokenScope

    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClickAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    iconModifier = Modifier.size(24.dp),
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    profile = profile,
                    figure = profile.avatarFigure
                )
            }
        }
        if (profile.lastAuthenticated != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .offset(12.dp, 12.dp)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .border(
                        4.dp,
                        AppTheme.colors.neutral000,
                        CircleShape
                    )

            ) {
                when {
                    ssoTokenScope?.token?.isValid() == true -> {
                        Image(
                            painterResource(R.drawable.main_screen_erx_icon_large),
                            null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        Image(
                            painterResource(R.drawable.main_screen_erx_icon_gray_large),
                            null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileConnectionSection(onClickAvatar: () -> Unit, onClickRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SmallMainScreenAvatar(onClickAvatar)
        ConnectionHelper(onClickRefresh)
    }
}

@Composable
fun ConnectionHelper(onClickRefresh: () -> Unit) {
    val profileHandler = LocalProfileHandler.current
    val profile = profileHandler.activeProfile
    val ssoTokenScope = profile.ssoTokenScope

    if (profile.lastAuthenticated != null && ssoTokenScope?.token == null) {
        TertiaryButton(onClickRefresh) {
            Text(stringResource(R.string.mainscreen_login))
        }
    }
}
