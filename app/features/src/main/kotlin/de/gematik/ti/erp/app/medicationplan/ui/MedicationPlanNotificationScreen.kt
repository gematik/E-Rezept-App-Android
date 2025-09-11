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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.sharp.Alarm
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanNotificationScreenController
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationSuccessScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationSuccessScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

private const val NOT_AVAILABLE = -1

class MedicationPlanNotificationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val controller = rememberMedicationPlanNotificationScreenController()
        val profilesWithSchedulesState by controller.profilesWithSchedules.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val onBack by rememberUpdatedState {
            navController.navigate(
                PrescriptionRoutes.PrescriptionListScreen.path(),
                navOptions = navOptions {
                    popUpTo(MedicationPlanRoutes.subGraphName()) {
                        inclusive = true
                    }
                }
            )
        }
        val baseActivity = LocalActivity.current as BaseActivity
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            scope.launch {
                baseActivity.resetPendingNavigationToMedicationNotificationScreen()
            }
        }
        BackHandler {
            onBack()
        }
        MedicationPlanNotificationScreenScaffold(
            listState = listState,
            profilesWithSchedulesState = profilesWithSchedulesState,
            onClickMedicationPlan = { navController.navigate(MedicationPlanRoutes.MedicationPlanScheduleListScreen.path()) },
            onBack = {
                onBack()
            }
        )
    }
}

@Composable
fun MedicationPlanNotificationScreenScaffold(
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
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
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
                    FullScreenLoadingIndicator()
                },
                onEmpty = {
                    EmptyScreenComponent(
                        title = stringResource(R.string.medication_notification_empty_title),
                        body = stringResource(R.string.medication_notification_empty_info),
                        image = {
                            Icon(
                                imageVector = Icons.Sharp.Alarm,
                                contentDescription = null,
                                tint = AppTheme.colors.primary700,
                                modifier = Modifier
                                    .size(SizeDefaults.sevenfoldAndHalf)
                            )
                        }
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = onClickMedicationPlan
            ) {
                Text(stringResource(R.string.medication_plan_notification_bottombar))
                SpacerSmall()
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
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
        items(
            items = profilesWithSchedules
        ) { profileWithSchedules ->
            ProfilesWithSchedulesComponent(
                profile = profileWithSchedules.profile,
                profileSchedules = profileWithSchedules.medicationSchedules
            )
        }
    }
}

@Composable
private fun ProfilesWithSchedulesComponent(
    profile: ProfilesUseCaseData.Profile,
    profileSchedules: List<MedicationSchedule>
) {
    Column(
        modifier = Modifier
            .padding(vertical = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.MediumSmall)
    ) {
        ProfileHeader(
            profile = profile
        )
        profileSchedules.forEach { schedule ->
            NotificationsSection(
                medicationSchedule = schedule
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotificationsSection(medicationSchedule: MedicationSchedule) {
    val formatter = rememberErpTimeFormatter()
    Column(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Text(
            text = medicationSchedule.message.title,
            style = AppTheme.typography.subtitle1
        )
        medicationSchedule.notifications.forEach { notification ->
            val (image, description) = getDayTimeImageAndDescription(notification)
            ListItem(
                modifier = Modifier
                    .border(
                        BorderStroke(width = SizeDefaults.eighth, color = AppTheme.colors.neutral300),
                        shape = RoundedCornerShape(SizeDefaults.double)
                    )
                    .clip(RoundedCornerShape(SizeDefaults.double)),
                icon = {
                    Image(
                        painter = painterResource(id = image),
                        contentDescription = null,
                        modifier = Modifier
                            .size(SizeDefaults.fivefold)
                            .clip(CircleShape)
                    )
                },
                text = {
                    Text(
                        text = when (description) {
                            NOT_AVAILABLE -> ""
                            else -> stringResource(id = description)
                        },
                        style = AppTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryText = {
                    Text(
                        text = getNotificationTimeDescription(
                            notification = notification,
                            schedule = medicationSchedule,
                            formatter = formatter
                        ),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.neutral600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Suppress("MagicNumber")
fun getDayTimeImageAndDescription(
    notification: MedicationScheduleNotification
): Pair<Int, Int> {
    val hour = notification.time.hour

    val resources = when (hour) {
        in 4..10 -> R.drawable.morning to R.string.medication_plan_morning_text
        in 10..15 -> R.drawable.noon to R.string.medication_plan_noon_text
        in 15..17 -> R.drawable.afternoon to R.string.medication_plan_afternoon_text
        else -> R.drawable.evening to R.string.medication_plan_evening_text
    }
    return resources
}

@Composable
private fun getNotificationTimeDescription(
    notification: MedicationScheduleNotification,
    schedule: MedicationSchedule,
    now: Instant = Clock.System.now(),
    formatter: ErpTimeFormatter
): String {
    val nowUpperInterval = now.plus(1, DateTimeUnit.MINUTE).toLocalDateTime(TimeZone.currentSystemDefault()).time
    val nowLowerInterval = now.minus(1, DateTimeUnit.MINUTE).toLocalDateTime(TimeZone.currentSystemDefault()).time
    val isNowRange = nowLowerInterval.rangeTo(nowUpperInterval)
    val daysSinceTheLastNotification = schedule.daysSinceTheLastNotification(now.toLocalDate())
    return if (notification.dosage.form.isNotBlank() && notification.dosage.ratio.isNotBlank()) {
        when (daysSinceTheLastNotification) {
            0 -> {
                if (isNowRange.contains(notification.time)) {
                    stringResource(
                        R.string.medication_plan_notification_description_now,
                        notification.dosage.ratio,
                        notification.dosage.form
                    )
                } else {
                    stringResource(
                        R.string.medication_plan_notification_description_today,
                        formatter.time(notification.time).toAnnotatedString(),
                        notification.dosage.ratio.toAnnotatedString(),
                        notification.dosage.form.toAnnotatedString()
                    )
                }
            }
            1 ->
                annotatedPluralsResource(
                    R.plurals.medication_plan_notification_description,
                    daysSinceTheLastNotification,
                    args = arrayOf(
                        formatter.time(notification.time).toAnnotatedString(),
                        notification.dosage.ratio.toAnnotatedString(),
                        notification.dosage.form.toAnnotatedString()
                    )
                ).toString()
            else -> annotatedPluralsResource(
                R.plurals.medication_plan_notification_description,
                daysSinceTheLastNotification,
                args = arrayOf(
                    formatter.date(now.minus(daysSinceTheLastNotification.days)).toAnnotatedString(),
                    formatter.time(notification.time).toAnnotatedString(),
                    notification.dosage.ratio.toAnnotatedString(),
                    notification.dosage.form.toAnnotatedString()
                )
            ).toString()
        }
    } else {
        stringResource(R.string.medication_plan_success_dosage)
    }
}

@LightDarkPreview
@Composable
fun MedicationNotificationSuccessScreenPreview(
    @PreviewParameter(MedicationSuccessScreenPreviewParameter::class) previewData: MedicationSuccessScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationPlanNotificationScreenScaffold(
            listState = listState,
            profilesWithSchedulesState = previewData.state,
            onClickMedicationPlan = {},
            onBack = {}
        )
    }
}
