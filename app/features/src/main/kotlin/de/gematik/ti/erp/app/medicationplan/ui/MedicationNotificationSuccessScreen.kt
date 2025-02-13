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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.sharp.Alarm
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.components.getDayTimeImageAndDescription
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationNotificationSuccessScreenController
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationSuccessScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationSuccessScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

private const val NOT_AVAILABLE = -1

class MedicationNotificationSuccessScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val baseActivity = LocalActivity.current as BaseActivity
        val controller = rememberMedicationNotificationSuccessScreenController()
        val profilesWithSchedulesState by controller.profilesWithSchedules.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        BackHandler {
            navController.popBackStack()
        }

        MedicationNotificationSuccessScreenScaffold(
            listState = listState,
            profilesWithSchedulesState = profilesWithSchedulesState,
            onClickMedicationPlan = { navController.navigate(MedicationPlanRoutes.MedicationPlanList.path()) },
            onBack = {
                baseActivity.medicationSuccessHasBeenShown()
                navController.navigate(PrescriptionRoutes.PrescriptionsScreen.path())
            }
        )
    }
}

@Composable
fun MedicationNotificationSuccessScreenScaffold(
    listState: LazyListState,
    profilesWithSchedulesState: UiState<List<ProfileWithSchedules>>,
    onClickMedicationPlan: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = stringResource(R.string.medication_plan_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Close,
        bottomBar = {
            MedicationNotificationSuccessScreenBottomBar(
                onClickMedicationPlan = onClickMedicationPlan
            )
        },
        content = { contentPadding ->

            UiStateMachine(
                state = profilesWithSchedulesState,
                onLoading = {
                    Center {
                        CircularProgressIndicator()
                    }
                },
                onEmpty = {
                    EmptyScreenComponent(
                        title = stringResource(R.string.medication_notification_empty_title),
                        body = stringResource(R.string.medication_notification_empty_info),
                        image = { EmptyScreenImage() }
                    ) {}
                },
                onError = {
                    ErrorScreenComponent()
                },
                onContent = { profilesWithSchedules ->
                    MedicationNotificationSuccessScreenContent(
                        profilesWithSchedules = profilesWithSchedules,
                        listState = listState,
                        contentPadding = contentPadding
                    )
                }
            )
        }
    )
}

@Composable
fun MedicationNotificationSuccessScreenBottomBar(onClickMedicationPlan: () -> Unit) {
    BottomAppBar(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        val color = AppTheme.colors.primary700
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button
                ) { onClickMedicationPlan() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.medication_plan_reminder_bottom_text), color = color)
            SpacerSmall()
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = color)
        }
    }
}

@Composable
fun EmptyScreenImage() {
    Icon(
        imageVector = Icons.Sharp.Alarm,
        contentDescription = null,
        tint = AppTheme.colors.primary700,
        modifier = Modifier
            .size(SizeDefaults.sevenfold)
    )
}

@Composable
fun MedicationNotificationSuccessScreenContent(
    listState: LazyListState,
    contentPadding: PaddingValues,
    profilesWithSchedules: List<ProfileWithSchedules>
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item { SpacerLarge() }
        items(
            items = profilesWithSchedules
        ) { profileWithSchedules ->
            if (profilesWithSchedules.size > 1) {
                ProfileHeader(profileWithSchedules.profile)
                SpacerMedium()
            }
            profileWithSchedules.medicationSchedules.forEach { schedule ->
                val title = schedule.message.title
                ScheduleTitle(title)
                SpacerSmall()
                schedule.notifications.forEach { notification ->
                    val (image, description) = getDayTimeImageAndDescription(notification)
                    MedicationPlanReminderCard(
                        imageResource = image,
                        title = when (description) {
                            NOT_AVAILABLE -> ""
                            else -> stringResource(id = description)
                        },
                        description = when {
                            notification.dosage.form.isNotBlank() -> stringResource(
                                R.string.medication_plan_success_dosage_success,
                                requireNotNull(notification.time),
                                requireNotNull(notification.dosage.form)
                            )

                            else -> stringResource(R.string.medication_plan_success_dosage)
                        }
                    )
                    SpacerMedium()
                }
                SpacerMedium()
            }
        }
    }
}

@Composable
fun ScheduleTitle(title: String) {
    Text(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        text = title,
        style = AppTheme.typography.subtitle1
    )
}

@Composable
fun ProfileHeader(profile: ProfilesUseCaseData.Profile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            modifier = Modifier.size(SizeDefaults.sixfold),
            emptyIcon = Icons.Rounded.PersonOutline,
            profile = profile,
            iconModifier = Modifier.size(SizeDefaults.doubleHalf)
        )
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = profile.name,
            style = AppTheme.typography.subtitle2
        )
    }
}

@LightDarkPreview
@Composable
fun MedicationNotificationSuccessScreenPreview(
    @PreviewParameter(MedicationSuccessScreenPreviewParameter::class) previewData: MedicationSuccessScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationNotificationSuccessScreenScaffold(
            listState = listState,
            profilesWithSchedulesState = previewData.state,
            onClickMedicationPlan = {},
            onBack = {}
        )
    }
}
