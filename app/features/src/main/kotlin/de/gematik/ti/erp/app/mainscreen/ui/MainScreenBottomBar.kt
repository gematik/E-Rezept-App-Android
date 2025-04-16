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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenBottomNavigationItems
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.BottomNavigation

private const val BottomBarBadgeOffsetX = -5
private const val BottomBarBadgeOffsetY = 5

@Suppress("LongMethod", "ComplexMethod")
@Composable
internal fun MainScreenBottomBar(
    mainNavController: NavController,
    unreadOrdersCount: Long
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
        extraContent = {}
    ) {
        MainScreenBottomNavigationItems.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.testTag(
                    when (screen) {
                        PrescriptionRoutes.PrescriptionsScreen -> TestTag.BottomNavigation.PrescriptionButton
                        MessagesRoutes.MessageListScreen -> TestTag.BottomNavigation.OrdersButton
                        PharmacyRoutes.PharmacyStartScreen -> TestTag.BottomNavigation.PharmaciesButton
                        SettingsNavigationScreens.SettingsScreen -> TestTag.BottomNavigation.SettingsButton
                        else -> ""
                    }
                ),
                selectedContentColor = AppTheme.colors.primary700,
                unselectedContentColor = AppTheme.colors.neutral600,
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (screen) {
                            PrescriptionRoutes.PrescriptionsScreen ->
                                Icon(
                                    painterResource(R.drawable.ic_logo_outlined),
                                    contentDescription = null,
                                    modifier = Modifier.size(SizeDefaults.triple)
                                )

                            PharmacyRoutes.PharmacyStartScreen -> Icon(
                                Icons.Outlined.PinDrop,
                                contentDescription = null,
                                modifier = Modifier.size(SizeDefaults.triple)
                            )

                            MessagesRoutes.MessageListScreen ->
                                if (unreadOrdersCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                modifier = Modifier.offset(
                                                    x = BottomBarBadgeOffsetX.dp,
                                                    y = BottomBarBadgeOffsetY.dp
                                                ),
                                                backgroundColor = Color.Red,
                                                contentColor = Color.White
                                            ) { Text(unreadOrdersCount.toString()) }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(SizeDefaults.triple)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(SizeDefaults.triple)
                                    )
                                }

                            SettingsNavigationScreens.SettingsScreen -> Icon(
                                Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        stringResource(
                            when (screen) {
                                PrescriptionRoutes.PrescriptionsScreen -> R.string.pres_bottombar_prescriptions
                                MessagesRoutes.MessageListScreen -> R.string.messages_bottombar
                                PharmacyRoutes.PharmacyStartScreen -> R.string.pres_bottombar_pharmacies
                                SettingsNavigationScreens.SettingsScreen -> R.string.main_settings_acc
                                else -> R.string.pres_bottombar_prescriptions
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute == screen.route,
                alwaysShowLabel = true,
                onClick = {
                    when (screen) {
                        is PharmacyRoutes.PharmacyStartScreen -> {
                            mainNavController.navigate(screen.path(""))
                        }
                        is SettingsNavigationScreens.SettingsScreen -> {
                            mainNavController.navigate(screen.path())
                        }
                        is MessagesRoutes.MessageListScreen -> {
                            mainNavController.navigate(screen.path())
                        }
                        is PrescriptionRoutes.PrescriptionsScreen -> {
                            mainNavController.navigate(screen.path())
                            // on reaching here the back navigation only closes the app
                            mainNavController.navigateAndClearStack(route = PrescriptionRoutes.PrescriptionsScreen.route)
                        }
                    }
                }
            )
        }
    }
}
