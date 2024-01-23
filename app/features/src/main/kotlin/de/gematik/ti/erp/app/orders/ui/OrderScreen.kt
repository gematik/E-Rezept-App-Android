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

package de.gematik.ti.erp.app.orders.ui

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.ui.UserNotAuthenticatedDialog
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource

@Composable
fun OrderScreen(
    mainNavController: NavController,
    mainScreenController: MainScreenController,
    onElevateTopBar: (Boolean) -> Unit
) {
    val profileController = rememberProfileController()
    val activeProfile by profileController.getActiveProfileState()

    var showUserNotAuthenticatedDialog by remember { mutableStateOf(false) }

    val onShowCardWall = {
        mainNavController.navigate(
            MainNavigationScreens.CardWall.path(activeProfile.id)
        )
    }
    if (showUserNotAuthenticatedDialog) {
        UserNotAuthenticatedDialog(
            onCancel = { showUserNotAuthenticatedDialog = false },
            onShowCardWall = onShowCardWall
        )
    }

    RefreshScaffold(
        profileId = activeProfile.id,
        onUserNotAuthenticated = { showUserNotAuthenticatedDialog = true },
        mainScreenController = mainScreenController,
        onShowCardWall = onShowCardWall
    ) { onRefresh ->
        Orders(
            activeProfile = activeProfile,
            onClickOrder = { orderId ->
                mainNavController.navigate(
                    MainNavigationScreens.Messages.path(orderId)
                )
            },
            onClickRefresh = {
                onRefresh(true, MutatePriority.UserInput)
            },
            onElevateTopBar = onElevateTopBar
        )
    }
}

@Composable
fun NewLabel() {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.primary100)
            .padding(horizontal = PaddingDefaults.Small, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.orders_label_new),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.primary900
        )
    }
}

@Composable
fun PrescriptionLabel(count: Int) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.neutral100)
            .padding(horizontal = PaddingDefaults.Small, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            annotatedPluralsResource(
                R.plurals.orders_plurals_label_nr_of_prescriptions,
                count,
                AnnotatedString(count.toString())
            ),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.neutral600
        )
    }
}

@Composable
fun OrderUseCaseData.Pharmacy.pharmacyName() =
    name.ifBlank {
        stringResource(R.string.orders_generic_pharmacy_name)
    }
