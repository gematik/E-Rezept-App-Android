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

package de.gematik.ti.erp.app.eurezept.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.eurezept.domin.model.EuAvailabilityInfo
import de.gematik.ti.erp.app.eurezept.domin.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuPrescriptionSelectionController
import de.gematik.ti.erp.app.eurezept.ui.preview.EuPrescriptionSelectionPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuPrescriptionSelectionPreviewParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

internal class EuPrescriptionSelectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val currentSelectedPrescriptions by graphController.selectedPrescriptions.collectAsStateWithLifecycle()
        val controller = rememberEuPrescriptionSelectionController(
            initialSelectedPrescriptions = currentSelectedPrescriptions
        )
        val lazyListState = rememberLazyListState()

        val prescriptions by controller.prescriptions.collectAsStateWithLifecycle()
        val selectedPrescriptionIds by controller.selectedPrescriptionIds.collectAsStateWithLifecycle()
        val selectedAvailableEuRedeemablePrescriptions by controller.selectedAvailableEuRedeemablePrescriptions.collectAsStateWithLifecycle()
        val activeProfile by graphController.activeProfile.collectAsStateWithLifecycle()

        EuPrescriptionSelectionScaffold(
            listState = lazyListState,
            prescriptions = prescriptions,
            selectedPrescriptionIds = selectedPrescriptionIds,
            profileName = activeProfile.data?.name.toString(),
            getAvailabilityInfo = { prescription -> controller.getAvailabilityInfo(prescription) },
            onBack = {
                graphController.setSelectedPrescriptions(selectedAvailableEuRedeemablePrescriptions)
                navController.popBackStack()
            },
            onPrescriptionToggle = { prescriptionId ->
                controller.togglePrescriptionSelection(prescriptionId)
            }
        )
    }
}

@Composable
fun EuPrescriptionSelectionScaffold(
    listState: LazyListState,
    prescriptions: List<EuPrescription>,
    selectedPrescriptionIds: Set<String>,
    profileName: String,
    getAvailabilityInfo: (EuPrescription) -> EuAvailabilityInfo,
    onBack: () -> Unit,
    onPrescriptionToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedElevationScaffold(
        modifier = modifier,
        topBarTitle = stringResource(R.string.eu_prescription_selection_title),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.back),
        onBack = onBack,
        bottomBar = {}
    ) { paddingValues ->
        EuPrescriptionSelectionContent(
            paddingValues = paddingValues,
            listState = listState,
            prescriptions = prescriptions,
            selectedPrescriptionIds = selectedPrescriptionIds,
            profileName = profileName,
            onPrescriptionToggle = onPrescriptionToggle,
            getAvailabilityInfo = getAvailabilityInfo
        )
    }
}

@Composable
fun EuPrescriptionSelectionContent(
    paddingValues: PaddingValues,
    listState: LazyListState,
    prescriptions: List<EuPrescription>,
    selectedPrescriptionIds: Set<String>,
    profileName: String,
    onPrescriptionToggle: (String) -> Unit,
    getAvailabilityInfo: (EuPrescription) -> EuAvailabilityInfo,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.zero)
    ) {
        item {
            ProfileSection(
                profileName = profileName
            )
            SpacerMedium()
        }

        item {
            PrescriptionSelectionContainer(
                prescriptions = prescriptions,
                selectedPrescriptionIds = selectedPrescriptionIds,
                onPrescriptionToggle = onPrescriptionToggle,
                getAvailabilityInfo = getAvailabilityInfo
            )
        }

        item {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = PaddingDefaults.XXLarge)
            )
        }
    }
}

@Composable
fun PrescriptionSelectionContainer(
    prescriptions: List<EuPrescription>,
    selectedPrescriptionIds: Set<String>,
    onPrescriptionToggle: (String) -> Unit,
    getAvailabilityInfo: (EuPrescription) -> EuAvailabilityInfo,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(SizeDefaults.double),
        contentPadding = PaddingValues(SizeDefaults.zero),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppTheme.colors.neutral900,
            containerColor = Color.Transparent
        ),
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(
                color = AppTheme.colors.neutral200,
                thickness = SizeDefaults.eighth
            )

            prescriptions.forEach { prescription ->
                PrescriptionListItemInContainer(
                    prescription = prescription,
                    isSelected = prescription.id in selectedPrescriptionIds,
                    availabilityInfo = getAvailabilityInfo(prescription),
                    onToggle = { onPrescriptionToggle(prescription.id) }
                )
            }
        }
    }
}

@Composable
fun PrescriptionListItemInContainer(
    prescription: EuPrescription,
    isSelected: Boolean,
    availabilityInfo: EuAvailabilityInfo,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = rememberErpTimeFormatter()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = availabilityInfo.isAvailable) { onToggle() }
            .padding(SizeDefaults.double),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.ShortMedium)
    ) {
        if (availabilityInfo.isAvailable) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier
                        .size(SizeDefaults.triple)
                        .clickable { onToggle() }
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = AppTheme.colors.neutral400,
                    modifier = Modifier
                        .size(SizeDefaults.triple)
                        .clickable { onToggle() }
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = AppTheme.colors.red600,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prescription.name,
                style = AppTheme.typography.body1,
                color = if (availabilityInfo.isAvailable) AppTheme.colors.neutral900 else AppTheme.colors.neutral900,
                fontWeight = FontWeight.Medium
            )

            SpacerSmall()

            Text(
                text = availabilityInfo.reason
                    ?: availabilityInfo.expiryDate?.let { expiryDate ->
                        stringResource(
                            R.string.eu_prescription_selection_available_until,
                            remember { formatter.date(expiryDate) }
                        )
                    }
                    ?: stringResource(R.string.eu_prescription_selection_available),
                style = AppTheme.typography.subtitle1,
                color = if (availabilityInfo.isAvailable) AppTheme.colors.neutral600 else AppTheme.colors.neutral600
            )
        }
    }
}

@Composable
fun ProfileSection(
    profileName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(SizeDefaults.sixfold)
                .background(AppTheme.colors.neutral200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_myplaces),
                contentDescription = null,
                tint = AppTheme.colors.neutral600,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }
        Text(
            text = profileName,
            style = AppTheme.typography.h6,
            color = AppTheme.colors.neutral900,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@LightDarkPreview
@Composable
fun EuPrescriptionSelectionContentPreview(
    @PreviewParameter(EuPrescriptionSelectionPreviewParameterProvider::class)
    previewData: EuPrescriptionSelectionPreviewData
) {
    PreviewTheme {
        EuPrescriptionSelectionContent(
            paddingValues = PaddingValues(SizeDefaults.zero),
            listState = rememberLazyListState(),
            prescriptions = previewData.prescriptions,
            selectedPrescriptionIds = previewData.selectedPrescriptionIds,
            profileName = previewData.profileName,
            onPrescriptionToggle = { },
            getAvailabilityInfo = previewData.getAvailabilityInfo
        )
    }
}
