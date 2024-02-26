/*
 * Copyright (c) 2024 gematik GmbH
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
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenBottomNavigationItems
import de.gematik.ti.erp.app.mainscreen.navigation.calculatePrescriptionCount
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.profiles.presentation.ProfileController
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.BottomNavigation

private const val BottomBarBadgeOffsetX = -5
private const val BottomBarBadgeOffsetY = 5

@Suppress("LongMethod", "ComplexMethod")
@Composable
internal fun MainScreenBottomBar(
    navController: NavController,
    bottomNavController: NavController,
    profileController: ProfileController,
    mainScreenController: MainScreenController
) {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val activeProfile by profileController.getActiveProfileState()

    val unreadPrescriptionCount = calculatePrescriptionCount(mainScreenController)
    val unreadOrdersCount by mainScreenController.updateUnreadOrders(activeProfile)
        .collectAsStateWithLifecycle(0)

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
        extraContent = {}
    ) {
        MainScreenBottomNavigationItems.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.testTag(
                    when (screen) {
                        MainNavigationScreens.Prescriptions -> TestTag.BottomNavigation.PrescriptionButton
                        MainNavigationScreens.Orders -> TestTag.BottomNavigation.OrdersButton
                        MainNavigationScreens.Pharmacies -> TestTag.BottomNavigation.PharmaciesButton
                        SettingsNavigationScreens.SettingsScreen -> TestTag.BottomNavigation.SettingsButton
                        else -> ""
                    }
                ),
                selectedContentColor = AppTheme.colors.primary700,
                unselectedContentColor = AppTheme.colors.neutral600,
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (screen) {
                            MainNavigationScreens.Prescriptions ->
                                if (unreadPrescriptionCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                modifier = Modifier.offset(
                                                    x = BottomBarBadgeOffsetX.dp,
                                                    y = BottomBarBadgeOffsetY.dp
                                                ),
                                                backgroundColor = Color.Red,
                                                contentColor = Color.White
                                            ) { Text(unreadPrescriptionCount.toString()) }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_logo_outlined),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        painterResource(R.drawable.ic_logo_outlined),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                            MainNavigationScreens.Pharmacies -> Icon(
                                Icons.Outlined.PinDrop,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            MainNavigationScreens.Orders ->
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
                                            Icons.Outlined.ShoppingBag,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Outlined.ShoppingBag,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
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
                                MainNavigationScreens.Prescriptions -> R.string.pres_bottombar_prescriptions
                                MainNavigationScreens.Orders -> R.string.pres_bottombar_orders
                                MainNavigationScreens.Pharmacies -> R.string.pres_bottombar_pharmacies
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
                    if (currentRoute != screen.route) {
                        when (screen.route) {
                            MainNavigationScreens.Pharmacies.route ->
                                navController.navigate(screen.path())

                            else ->
                                bottomNavController.navigate(screen.path())
                        }
                    }
                }
            )
        }
    }
}
