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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.component.Label
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center

class PrescriptionDetailOrganizationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId = remember {
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            )
        } ?: ""
        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId)
        val profilePrescriptionData by prescriptionDetailsController.profilePrescription.collectAsStateWithLifecycle()

        UiStateMachine(
            state = profilePrescriptionData,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onEmpty = {
                ErrorScreenComponent()
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { (_, prescription) ->
                val syncedPrescription = prescription as? PrescriptionData.Synced
                val organization = syncedPrescription?.organization
                val listState = rememberLazyListState()
                AnimatedElevationScaffold(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.Screen),
                    topBarTitle = stringResource(R.string.pres_detail_organization_header),
                    listState = listState,
                    onBack = navController::popBackStack,
                    navigationMode = NavigationBarMode.Back
                ) { innerPadding ->
                    PrescriptionDetailOrganizationScreenContent(
                        listState,
                        innerPadding,
                        organization
                    )
                }
            }
        )
    }
}

@Composable
private fun PrescriptionDetailOrganizationScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    organization: SyncedTaskData.Organization?
) {
    val noValueText = stringResource(R.string.pres_details_no_value)
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag(TestTag.Prescriptions.Details.Organization.Content),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.Name),
                text = organization?.name ?: noValueText,
                label = stringResource(id = R.string.pres_detail_organization_label_name)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.Address),
                text = organization?.address?.joinToString()?.takeIf { it.isNotEmpty() } ?: noValueText,
                label = stringResource(id = R.string.pres_detail_organization_label_address)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.BSNR),
                text = organization?.uniqueIdentifier ?: noValueText,
                label = stringResource(id = R.string.pres_detail_organization_label_id)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.Phone),
                text = organization?.phone ?: noValueText,
                label = stringResource(id = R.string.pres_detail_organization_label_telephone)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.Organization.EMail),
                text = organization?.mail ?: noValueText,
                label = stringResource(id = R.string.pres_detail_organization_label_email)
            )
            SpacerMedium()
        }
    }
}
