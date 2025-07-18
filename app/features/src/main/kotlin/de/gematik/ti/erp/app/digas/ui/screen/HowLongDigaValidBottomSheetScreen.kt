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

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberGetPrescriptionByTaskIdController
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class HowLongDigaValidBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = false) {
    @Composable
    override fun Content() {
        val taskId = remember {
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            )
        } ?: ""

        val controller = rememberGetPrescriptionByTaskIdController(taskId)
        val prescriptionState by controller.prescription.collectAsStateWithLifecycle()

        UiStateMachine(
            state = prescriptionState,
            onLoading = { FullScreenLoadingIndicator() },
            onEmpty = {
                ErrorScreenComponent()
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { content ->
                val prescription = content as PrescriptionData.Synced
                Column(
                    Modifier
                        .padding(horizontal = PaddingDefaults.Medium)
                        .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge)
                ) {
                    SpacerMedium()
                    Text(
                        stringResource(R.string.how_long_is_diga_valid),
                        style = AppTheme.typography.subtitle1,
                        color = AppTheme.colors.neutral900,
                        modifier = Modifier.testTag(TestTag.Prescriptions.Details.PrescriptionDetailBottomSheetTitle)
                    )
                    SpacerMedium()
                    val start =
                        if (prescription.medicationRequest.multiplePrescriptionInfo.indicator) {
                            prescription.medicationRequest.multiplePrescriptionInfo.start
                                ?: prescription.authoredOn
                        } else {
                            prescription.authoredOn
                        }
                    Column {
                        DateRange(start = start, end = prescription.acceptUntil?.minus(1.days) ?: start)
                        SpacerSmall()
                        Text(
                            stringResource(R.string.diga_activation_code_request_period),
                            style = AppTheme.typography.body2l
                        )
                    }
                }
            }
        )
    }

    @Composable
    private fun DateRange(
        start: Instant,
        end: Instant
    ) {
        val formatter = rememberErpTimeFormatter()
        val startText = remember { formatter.date(start) }
        val endText = remember { formatter.date(end) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            androidx.compose.material.Text(startText, style = AppTheme.typography.subtitle2l)
            androidx.compose.material.Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = stringResource(R.string.a11y_diga_arrow),
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.double)
            )
            androidx.compose.material.Text(endText, style = AppTheme.typography.subtitle2l)
        }
    }
}
