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

package de.gematik.ti.erp.app.main.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.HorizontalDivider
import de.gematik.ti.erp.app.common.SpacerMedium
import de.gematik.ti.erp.app.common.SpacerSmall
import de.gematik.ti.erp.app.common.VerticalDivider
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.communication.ui.CommunicationScreen
import de.gematik.ti.erp.app.navigation.ui.Navigation
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.protocol.ui.ProtocolScreen
import java.util.Locale
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun LoggedInScreen(
    mainViewModel: MainScreenViewModel,
    navigation: Navigation
) {
    Scaffold(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(Modifier.fillMaxSize()) {
            SideBar(
                mainViewModel = mainViewModel,
                navigation = navigation
            )
            VerticalDivider()
            Column {
                val title = when (navigation.currentBackStackEntry) {
                    MainNavigation.PrescriptionsUnredeemed -> App.strings.desktopMainRedeemablePrescriptions()
                    MainNavigation.PrescriptionsRedeemed -> App.strings.desktopMainRedeemedPrescriptions()
                    MainNavigation.PharmacyCommunications -> App.strings.desktopMainRedeemedPrescriptions()
                    else -> ""
                }
                TopBar(title)
                HorizontalDivider()
                Box(Modifier.fillMaxSize()) {
                    when (navigation.currentBackStackEntry) {
                        is MainNavigation.Prescriptions -> PrescriptionScreen(navigation)
                        is MainNavigation.PharmacyCommunications -> CommunicationScreen()
                        is MainNavigation.Protocol -> ProtocolScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun DarkModeToggle(
    toggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val handleOffset by animateDpAsState(if (toggled) 32.dp - 20.dp else 0.dp)
    Box(
        Modifier.toggleable(
            toggled,
            onValueChange = onToggle,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        )
    ) {
        Box(
            Modifier
                .background(AppTheme.colors.neutral400, CircleShape)
                .requiredSize(width = 32.dp, height = 14.dp)
                .align(Alignment.Center)
        )
        DarkModeHandle(
            modifier = Modifier.offset(x = handleOffset),
            backgroundColor = AppTheme.colors.neutral100
        ) {
            Crossfade(toggled) {
                when (it) {
                    true -> Image(painterResource("images/nights_stay.svg"), null)
                    false -> Image(painterResource("images/wb_sunny.svg"), null)
                }
            }
        }
    }
}

@Composable
private fun DarkModeHandle(
    modifier: Modifier,
    backgroundColor: Color,
    elevation: Dp = 2.dp,
    icon: @Composable () -> Unit
) {
    val shape = CircleShape
    Box(
        modifier
            .shadow(elevation, shape)
            .requiredSize(20.dp)
            .background(LocalElevationOverlay.current?.apply(backgroundColor, elevation) ?: backgroundColor, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.requiredSize(12.dp)) {
            icon()
        }
    }
}

@Composable
private fun TopBar(
    title: String
) {
    Row(
        Modifier.requiredHeight(56.dp).padding(horizontal = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.h6)
        Spacer(Modifier.weight(1f))
        Logo(
            Modifier.height(24.dp)
        )
    }
}

@Composable
private fun SideBar(
    mainViewModel: MainScreenViewModel,
    navigation: Navigation
) {
    val mainState by produceState(mainViewModel.defaultState) {
        mainViewModel.screenState().collect {
            value = it
        }
    }

    val selected = navigation.currentBackStackEntry

    val coScope = rememberCoroutineScope()
    Column(Modifier.width(224.dp)) {
        Row(
            Modifier.padding(horizontal = PaddingDefaults.Medium).height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource("images/erp_logo.webp"),
                null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.weight(1f))
            DarkModeToggle(
                toggled = mainState.darkMode,
                onToggle = { if (it) mainViewModel.onEnableDarkMode() else mainViewModel.onDisableDarkMode() }
            )
        }
        SpacerMedium()
        Text(
            App.strings.desktopMainPrescriptions().uppercase(Locale.getDefault()),
            style = AppTheme.typography.overlinel,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
        Column(
            Modifier.padding(PaddingDefaults.Small),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            SideBarItem(
                Icons.Outlined.Medication,
                App.strings.desktopMainRedeemablePrescriptions(),
                style = AppTheme.typography.subtitle2l,
                selected = selected == MainNavigation.PrescriptionsUnredeemed
            ) {
                navigation.navigate(MainNavigation.PrescriptionsUnredeemed)
            }
            SideBarItem(
                Icons.Outlined.Archive,
                App.strings.desktopMainRedeemedPrescriptions(),
                style = AppTheme.typography.subtitle2l,
                selected = selected == MainNavigation.PrescriptionsRedeemed
            ) {
                navigation.navigate(MainNavigation.PrescriptionsRedeemed)
            }
            SideBarItem(
                Icons.Outlined.History,
                App.strings.desktopMainPharmacyProtocol(),
                style = AppTheme.typography.subtitle2l,
                selected = selected == MainNavigation.Protocol
            ) {
                navigation.navigate(MainNavigation.Protocol)
            }
        }
        SpacerMedium()
        Text(
            App.strings.desktopMainCommunications().uppercase(Locale.getDefault()),
            style = AppTheme.typography.overlinel,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
        Column(
            Modifier.padding(PaddingDefaults.Small),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            SideBarItem(
                Icons.Outlined.Message,
                App.strings.desktopMainPharmacyCommunications(),
                style = AppTheme.typography.subtitle2l,
                selected = selected == MainNavigation.PharmacyCommunications
            ) {
                navigation.navigate(MainNavigation.PharmacyCommunications)
            }
        }
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.padding(PaddingDefaults.Small),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            val uriHandler = LocalUriHandler.current
            val helpLink = App.strings.desktopHelpLink()
            if (mainState.zoomed) {
                SideBarItem(Icons.Outlined.ZoomOut, App.strings.desktopMainZoomOut()) {
                    mainViewModel.onZoomOut()
                }
            } else {
                SideBarItem(Icons.Outlined.ZoomIn, App.strings.desktopMainZoomIn()) {
                    mainViewModel.onZoomIn()
                }
            }
            SideBarItem(Icons.Outlined.ChatBubbleOutline, App.strings.desktopMainHelp()) {
                uriHandler.openUri(helpLink)
            }
            SideBarItem(Icons.Outlined.Logout, App.strings.desktopMainLogout()) {
                coScope.launch { mainViewModel.onLogout() }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SideBarItem(
    icon: ImageVector,
    text: String,
    selected: Boolean = false,
    style: TextStyle = AppTheme.typography.body2l,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = if (selected) AppTheme.colors.neutral100 else Color.Unspecified
    ) {
        Row(
            Modifier.padding(horizontal = PaddingDefaults.Small, vertical = PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppTheme.colors.neutral500)
            SpacerSmall()
            Text(text, style = style)
        }
    }
}
