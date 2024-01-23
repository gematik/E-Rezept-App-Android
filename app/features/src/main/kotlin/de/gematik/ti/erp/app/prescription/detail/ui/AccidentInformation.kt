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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val NoInfo = "-"

@Composable
fun AccidentInformation(
    prescriptionDetailsController: PrescriptionDetailsController,
    onBack: () -> Unit
) {
    val prescription by prescriptionDetailsController.prescriptionState
    val syncedPrescription = prescription as? PrescriptionData.Synced

    // TODO : UI for accident types
    val isAccident = remember(syncedPrescription) {
        syncedPrescription?.medicationRequest?.accidentType != SyncedTaskData.AccidentType.None
    }

    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.pres_detail_accident_header),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) { innerPadding ->
        LazyColumn(
            Modifier.padding(innerPadding),
            state = listState,
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        ) {
            item {
                SpacerMedium()
                Label(
                    text = if (isAccident) {
                        stringResource(R.string.pres_detail_yes)
                    } else {
                        stringResource(R.string.pres_detail_no)
                    },
                    label = stringResource(R.string.pres_detail_accident_header)
                )
            }
            item {
                val text = if (isAccident) {
                    remember(LocalConfiguration.current, syncedPrescription?.medicationRequest?.dateOfAccident) {
                        val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        syncedPrescription?.medicationRequest?.dateOfAccident
                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                            ?.date
                            ?.toJavaLocalDate()
                            ?.format(dtFormatter)
                            ?: MissingValue
                    }
                } else {
                    NoInfo
                }
                Label(
                    text = text,
                    label = stringResource(R.string.pres_detail_accident_label_date)
                )
            }
            item {
                val text = if (isAccident) {
                    syncedPrescription?.medicationRequest?.location ?: MissingValue
                } else {
                    NoInfo
                }
                Label(
                    text = text,
                    label = stringResource(R.string.pres_detail_accident_label_location)
                )
                SpacerMedium()
            }
        }
    }
}
