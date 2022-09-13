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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.repository.statusMapping
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PatientScreen(
    prescription: PrescriptionData.Synced,
    onBack: () -> Unit
) {
    val patient = prescription.patient
    val insurance = prescription.insurance
    val noValueText = stringResource(R.string.pres_details_no_value)
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.pres_detail_patient_header),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize().padding(innerPadding),
            state = listState,
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        ) {
            item {
                SpacerMedium()
                Label(
                    text = patient.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_label_name)
                )
            }
            item {
                Label(
                    text = patient.address?.joinToString() ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_label_address)
                )
            }
            item {
                Label(
                    text = remember(LocalConfiguration.current, patient) {
                        val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

                        patient.birthdate?.let {
                            LocalDateTime
                                .ofInstant(it, ZoneId.systemDefault())
                                .format(dtFormatter)
                        } ?: noValueText
                    },
                    label = stringResource(R.string.pres_detail_patient_label_birthdate)
                )
            }
            item {
                Label(
                    text = insurance.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_label_insurance)
                )
            }
            item {
                Label(
                    text = insurance.status?.let { statusMapping[it]?.let { stringResource(it) } } ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_label_member_status)
                )
            }
            item {
                Label(
                    text = patient.insuranceIdentifier ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_label_insurance_id)
                )
                SpacerMedium()
            }
        }
    }
}
