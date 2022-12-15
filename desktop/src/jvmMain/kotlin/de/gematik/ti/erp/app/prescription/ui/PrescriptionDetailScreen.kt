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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.Dialog
import de.gematik.ti.erp.app.common.HintCard
import de.gematik.ti.erp.app.common.HintCardDefaults
import de.gematik.ti.erp.app.common.HintLargeImage
import de.gematik.ti.erp.app.common.HintSmallImage
import de.gematik.ti.erp.app.common.HintTextLearnMoreButton
import de.gematik.ti.erp.app.common.SpacerTiny
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.fhir.InsuranceCompanyDetail
import de.gematik.ti.erp.app.fhir.MedicationRequestDetail
import de.gematik.ti.erp.app.fhir.OrganizationDetail
import de.gematik.ti.erp.app.fhir.PatientDetail
import de.gematik.ti.erp.app.fhir.PractitionerDetail
import de.gematik.ti.erp.app.fhir.codeToDosageFormMapping
import de.gematik.ti.erp.app.fhir.normSizeMapping
import de.gematik.ti.erp.app.fhir.statusMapping
import de.gematik.ti.erp.app.navigation.ui.Navigation
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val missingValue = "---"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrescriptionDetailsScreen(
    navigation: Navigation,
    prescription: PrescriptionUseCaseData.PrescriptionDetails,
    audits: List<PrescriptionUseCaseData.PrescriptionAudit>,
    onClickDelete: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

    // scroll back to the top if we select a new prescription
    LaunchedEffect(prescription) {
        lazyListState.animateScrollToItem(0)
    }

    val dtAuditFormatter =
        remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                contentPadding = PaddingValues(horizontal = PaddingDefaults.Medium),
                modifier = Modifier.widthIn(max = 560.dp).align(Alignment.Center)
            ) {
                if (prescription.medicationRequest.emergencyFee == true) {
                    item {
                        EmergencyServiceCard()
                    }
                }

                item {
                    Column(Modifier.padding(PaddingDefaults.Medium)) {
                        SelectionContainer {
                            Column {
                                Header(
                                    text = prescription.medication.text ?: missingValue
                                )
                                FullDetailSecondHeader(prescription)
                            }
                        }
                    }
                }

                if (prescription.medicationRequest.substitutionAllowed && !prescription.isDispensed) {
                    item {
                        SubstitutionAllowed()
                    }
                }

                item {
                    Column {
                        MedicationInformation(prescription)
                        if (prescription.isSubstituted) {
                            WasSubstitutedHint()
                        }
                        DosageInformation(prescription)
                        PatientInformation(prescription.patient, prescription.insurance)
                    }
                }

                item {
                    PractitionerInformation(prescription.practitioner)
                }
                item {
                    OrganizationInformation(prescription.organization)
                }
                item {
                    AccidentInformation(prescription.medicationRequest)
                }
                item {
                    SubHeader(
                        text = App.strings.presDetailProtocolHeader()
                    )
                }
                items(audits) {
                    Label(
                        text = if (it.text.isNullOrEmpty()) {
                            App.strings.presDetailProtocolEmptyText()
                        } else {
                            it.text
                        },
                        label = it.timestamp.format(dtAuditFormatter)
                    )
                }
                item {
                    TechnicalPrescriptionInformation(prescription.prescription)
                }
                item {
                    DeleteButton {
                        onClickDelete()
                    }
                }
            }
            VerticalScrollbar(
                scrollbarAdapter,
                modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 1.dp).fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DeleteButton(onClickDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDeleteDialog = true },
        modifier = Modifier
            .padding(
                horizontal = PaddingDefaults.Medium,
                vertical = PaddingDefaults.Medium * 2
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red600,
            contentColor = AppTheme.colors.neutral000
        )
    ) {
        Text(
            App.strings.presDetailDelete().uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium / 2,
                bottom = PaddingDefaults.Medium / 2
            )
        )
    }

    if (showDeleteDialog) {
        Dialog(
            title = App.strings.presDetailDeleteMsg(),
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = AppTheme.colors.red600,
                        contentColor = AppTheme.colors.neutral000
                    ),
                    onClick = {
                        onClickDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(App.strings.presDetailDeleteYes().uppercase(Locale.getDefault()))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text(App.strings.presDetailDeleteNo().uppercase(Locale.getDefault()))
                }
            },
            onDismissRequest = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun FullDetailSecondHeader(
    prescriptionDetails: PrescriptionUseCaseData.PrescriptionDetails
) {
    Column {
        Text(
            text = expiresOrAcceptedUntil(prescriptionDetails.prescription),
            style = AppTheme.typography.body2l
        )
        SpacerTiny()
    }
}

@Composable
private fun MedicationInformation(
    prescription: PrescriptionUseCaseData.PrescriptionDetails
) {
    val medicationType =
        prescription.medicationType()?.let { App.strings.codeToDosageFormMapping()[it]?.invoke() } ?: missingValue

    val uniqueIdentifier =
        prescription.uniqueIdentifier() ?: missingValue

    Column {
        SubHeader(
            text = App.strings.presDetailMedicationHeader()
        )

        Label(
            text = medicationType,
            label = App.strings.presDetailMedicationLabelDosageForm()
        )

        Label(
            text = if (prescription.medication.normSizeCode != null) {
                "${prescription.medication.normSizeCode} - " +
                    "${App.strings.normSizeMapping()[prescription.medication.normSizeCode]?.invoke()}"
            } else {
                missingValue
            },
            label = App.strings.presDetailMedicationLabelNormsize()
        )

        Label(
            text = uniqueIdentifier,
            label = App.strings.presDetailMedicationLabelId()
        )
    }
}

@Composable
private fun WasSubstitutedHint() =
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        properties = HintCardDefaults.flatProperties(
            backgroundColor = AppTheme.colors.red100,
            contentColor = AppTheme.colors.neutral999
        ),
        image = {
            HintSmallImage(
                painterResource("images/medical_hand_out_circle_red.webp"),
                innerPadding = it
            )
        },
        title = { Text(App.strings.presDetailSubstitutedHeader()) },
        body = { Text(App.strings.presDetailSubstitutedInfo()) }
    )

@Composable
private fun DosageInformation(
    prescription: PrescriptionUseCaseData.PrescriptionDetails
) {
    val infoText = prescription.dosageInstruction() ?: App.strings.presDetailDosageDefaultInfo()

    SubHeader(
        text = App.strings.presDetailDosageHeader()
    )
    HintCard(
        modifier = Modifier.padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium),
        properties = HintCardDefaults.flatProperties(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = AppTheme.colors.primary100
        ),
        image = { HintSmallImage(painterResource("images/doctor_circle.webp"), innerPadding = it) },
        title = null,
        body = { Text(infoText) }
    )
}

@Composable
private fun PatientInformation(
    patient: PatientDetail,
    insurance: InsuranceCompanyDetail
) {
    Column {
        val dtFormatter =
            remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

        SubHeader(
            text = App.strings.presDetailPatientHeader()
        )

        Label(
            text = patient.name ?: missingValue,
            label = App.strings.presDetailPatientLabelName()
        )

        Label(
            text = patient.address ?: missingValue,
            label = App.strings.presDetailPatientLabelAddress()
        )

        Label(
            text = patient.birthdate?.format(dtFormatter) ?: missingValue,
            label = App.strings.presDetailPatientLabelBirthdate()
        )

        Label(
            text = insurance.name ?: missingValue,
            label = App.strings.presDetailPatientLabelInsurance()
        )

        Label(
            text = insurance.statusCode?.let { App.strings.statusMapping()[it]?.invoke() } ?: missingValue,
            label = App.strings.presDetailPatientLabelMemberStatus()
        )

        Label(
            text = patient.insuranceIdentifier ?: missingValue,
            label = App.strings.presDetailPatientLabelInsuranceId()
        )
    }
}

@Composable
private fun PractitionerInformation(
    practitioner: PractitionerDetail
) {
    Column {
        SubHeader(
            text = App.strings.presDetailPractitionerHeader()
        )

        Label(
            text = practitioner.name ?: missingValue,
            label = App.strings.presDetailPractitionerLabelName()
        )

        Label(
            text = practitioner.qualification ?: missingValue,
            label = App.strings.presDetailPractitionerLabelQualification()
        )

        Label(
            text = practitioner.practitionerIdentifier ?: missingValue,
            label = App.strings.presDetailPractitionerLabelId()
        )
    }
}

@Composable
private fun OrganizationInformation(
    organization: OrganizationDetail
) {
    Column {
        SubHeader(
            text = App.strings.presDetailOrganizationHeader()
        )

        Label(
            text = organization.name ?: missingValue,
            label = App.strings.presDetailOrganizationLabelName()
        )

        Label(
            text = organization.address ?: missingValue,
            label = App.strings.presDetailOrganizationLabelAddress()
        )

        Label(
            text = organization.uniqueIdentifier ?: missingValue,
            label = App.strings.presDetailOrganizationLabelId()
        )

        Label(
            text = organization.phone ?: missingValue,
            label = App.strings.presDetailOrganizationLabelTelephone()
        )

        Label(
            text = organization.mail ?: missingValue,
            label = App.strings.presDetailOrganizationLabelEmail()
        )
    }
}

@Composable
private fun AccidentInformation(
    medicationRequest: MedicationRequestDetail
) {
    val dtFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    SubHeader(
        text = App.strings.presDetailAccidentHeader()
    )

    Label(
        text = medicationRequest.dateOfAccident?.format(dtFormatter) ?: missingValue,
        label = App.strings.presDetailAccidentLabelDate()
    )

    Label(
        text = medicationRequest.location ?: missingValue,
        label = App.strings.presDetailAccidentLabelLocation()
    )
}

@Composable
private fun TechnicalPrescriptionInformation(prescription: PrescriptionUseCaseData.Prescription) {
    Column {
        SubHeader(App.strings.presDetailTechnicalInformation())

        Label(
            text = prescription.taskId,
            label = App.strings.taskId()
        )
    }
}

@Composable
private fun Group(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        Modifier.padding(PaddingDefaults.Medium),
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp
    ) {
        Column {
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Label(
    text: String,
    label: String
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .padding(PaddingDefaults.Medium)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body1
            )
            SpacerTiny()
            Text(
                text = label,
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
private fun Header(
    text: String,
    modifier: Modifier = Modifier
) =
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(
            top = PaddingDefaults.Large
        )
    )

@Composable
private fun SubHeader(
    text: String,
    modifier: Modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
) =
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(
            top = PaddingDefaults.Large + PaddingDefaults.Medium,
            bottom = PaddingDefaults.Medium
        )
    )

@Composable
private fun EmergencyServiceCard(
    modifier: Modifier = Modifier
) =
    HintCard(
        modifier = modifier,
        properties = HintCardDefaults.properties(elevation = 0.dp),
        image = {
            HintLargeImage(
                painterResource("images/pharmacist.webp"),
                innerPadding = it
            )
        },
        title = { Text(App.strings.presDetailNoctuHeader()) },
        body = { Text(App.strings.presDetailNoctuInfo()) }
    )

@Composable
private fun SubstitutionAllowed() =
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        properties = HintCardDefaults.flatProperties(
            backgroundColor = AppTheme.colors.primary100
        ),
        image = {
            HintSmallImage(
                painterResource("images/pharmacist_circle.webp"),
                innerPadding = it
            )
        },
        title = { Text(App.strings.presDetailAutIdemHeader()) },
        body = { Text(App.strings.presDetailAutIdemInfo()) },
        action = {
            HintTextLearnMoreButton()
        }
    )
