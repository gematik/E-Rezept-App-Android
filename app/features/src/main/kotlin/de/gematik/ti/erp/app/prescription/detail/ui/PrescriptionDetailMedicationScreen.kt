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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationCategory
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.codeToFormMapping
import de.gematik.ti.erp.app.prescription.repository.normSizeMapping
import de.gematik.ti.erp.app.substitutionAllowed
import de.gematik.ti.erp.app.supplyForm
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.extensions.dateTimeMediumText
import de.gematik.ti.erp.app.utils.extensions.temporalText
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.letNotNullOnCondition
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class PrescriptionDetailMedicationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId =
            remember { requireNotNull(navBackStackEntry.arguments?.getString(PrescriptionDetailRoutes.TaskId)) }
        val selectedMedication =
            remember {
                requireNotNull(
                    navBackStackEntry.arguments?.getString(
                        PrescriptionDetailRoutes.SelectedMedication
                    )
                )
            }
        Napier.i { "selectedMedication" }
        Napier.i { selectedMedication }
        val prescriptionDataMedication = remember(selectedMedication) {
            fromNavigationString<PrescriptionData.Medication>(selectedMedication)
        }
        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId)
        val prescription by prescriptionDetailsController.prescriptionState
        val syncedPrescription = prescription as? PrescriptionData.Synced
        val scaffoldState = rememberScaffoldState()
        val listState = rememberLazyListState()
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Screen),
            scaffoldState = scaffoldState,
            listState = listState,
            onBack = navController::popBackStack,
            topBarTitle = stringResource(R.string.synced_medication_detail_header),
            navigationMode = NavigationBarMode.Back,
            snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
            actions = {}
        ) { innerPadding ->
            PrescriptionDetailMedicationScreenContent(
                listState,
                innerPadding,
                navController,
                prescriptionDataMedication,
                syncedPrescription
            )
        }
    }
}

@Composable
private fun PrescriptionDetailMedicationScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    navController: NavController,
    prescriptionDataMedication: PrescriptionData.Medication?,
    syncedPrescription: PrescriptionData.Synced?
) {
    val medication = when (prescriptionDataMedication) {
        is PrescriptionData.Medication.Dispense -> prescriptionDataMedication.medicationDispense.medication
        is PrescriptionData.Medication.Request -> prescriptionDataMedication.medicationRequest.medication
        null -> null
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag(TestTag.Prescriptions.Details.Medication.Content),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
        }
        when (medication) {
            is SyncedTaskData.MedicationPZN -> pznMedicationInformation(medication)
            is SyncedTaskData.MedicationIngredient -> ingredientMedicationInformation(medication) {
                    ingredient ->
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.path(
                        selectedIngredient = ingredient.toNavigationString()
                    )
                )
            }

            is SyncedTaskData.MedicationCompounding -> compoundingMedicationInformation(medication) {
                    ingredient ->
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.path(
                        selectedIngredient = ingredient.toNavigationString()
                    )
                )
            }

            is SyncedTaskData.MedicationFreeText -> freeTextMedicationInformation(medication)
            null -> {}
        }

        syncedPrescription?.authoredOn?.let { prescriptionInformation(it) }

        when (prescriptionDataMedication) {
            is PrescriptionData.Medication.Dispense ->
                medicationDispense(prescriptionDataMedication.medicationDispense)
            is PrescriptionData.Medication.Request ->
                medicationRequest(prescriptionDataMedication.medicationRequest)
            null -> {}
        }
        item {
            SpacerMedium()
        }
    }
}

private fun LazyListScope.prescriptionInformation(authoredOn: Instant) {
    item {
        AuthoredOnLabel(authoredOn)
    }
}

private fun LazyListScope.medicationRequest(medicationRequest: SyncedTaskData.MedicationRequest) {
    item {
        DosageInstructionLabel(medicationRequest.dosageInstruction)
    }
    item {
        QuantityLabel(medicationRequest.quantity)
    }
    item {
        medicationRequest.note?.let { NoteLabel(it) }
    }
    item {
        medicationRequest.bvg?.let { BvgLabel(it) }
    }
    item {
        SubstitutionLabel(medicationRequest.substitutionAllowed)
    }
}

private fun LazyListScope.medicationDispense(medicationDispense: SyncedTaskData.MedicationDispense) {
    item {
        HandedOverLabel(medicationDispense.whenHandedOver)
    }
    item {
        PerformerLabel(medicationDispense.performer)
    }
    item {
        DosageInstructionLabel(medicationDispense.dosageInstruction)
    }
    item {
        SubstitutionLabel(medicationDispense.wasSubstituted)
    }
}

private fun LazyListScope.pznMedicationInformation(medication: SyncedTaskData.MedicationPZN) {
    item {
        Label(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Name),
            text = medication.name(),
            label = stringResource(R.string.medication_trade_name)
        )
    }
    letNotNullOnCondition(
        first = medication.amount,
        condition = {
            medication.amount?.numerator?.value?.isNotNullOrEmpty() == true
        },
        transform = {
            item {
                AmountLabel(it)
            }
        }
    )
    medication.normSizeCode?.let {
        item {
            NormSizeLabel(it)
        }
    }
    item {
        PZNLabel(medication.uniqueIdentifier)
    }
    medication.form?.let {
        item {
            FormLabel(it)
        }
    }

    if (medication.category != SyncedTaskData.MedicationCategory.UNKNOWN) {
        item {
            CategoryLabel(medication.category)
        }
    }

    item {
        VaccineLabel(medication.vaccine)
    }
    medication.lotNumber?.let {
        item {
            LotNumberLabel(it)
        }
    }
    medication.expirationDate?.let {
        item {
            ExpirationDateLabel(it)
        }
    }
}

private fun LazyListScope.ingredientMedicationInformation(
    medication: SyncedTaskData.MedicationIngredient,
    onClickIngredient: (SyncedTaskData.Ingredient) -> Unit
) {
    medication.ingredients.forEach { ingredient ->
        item {
            IngredientNameLabel(ingredient.text) {
                onClickIngredient(ingredient)
            }
        }
    }
    medication.normSizeCode?.let {
        item {
            NormSizeLabel(it)
        }
    }

    medication.form?.let {
        item {
            FormLabel(it)
        }
    }
    letNotNullOnCondition(
        first = medication.amount,
        condition = {
            medication.amount?.numerator?.value?.isNotNullOrEmpty() == true
        },
        transform = {
            item {
                AmountLabel(it)
            }
        }
    )
    item {
        CategoryLabel(medication.category)
    }
    item {
        VaccineLabel(medication.vaccine)
    }
    medication.lotNumber?.let {
        item {
            LotNumberLabel(it)
        }
    }
    medication.expirationDate?.let {
        item {
            ExpirationDateLabel(it)
        }
    }
}

private fun LazyListScope.compoundingMedicationInformation(
    medication: SyncedTaskData.MedicationCompounding,
    onClickIngredient: (SyncedTaskData.Ingredient) -> Unit
) {
    item {
        Label(
            text = medication.name(),
            label = stringResource(R.string.medication_compounding_name)
        )
    }
    letNotNullOnCondition(
        first = medication.amount,
        condition = {
            medication.amount?.numerator?.value?.isNotNullOrEmpty() == true
        },
        transform = {
            item {
                AmountLabel(it)
            }
        }
    )
    medication.packaging?.let {
        item {
            PackagingLabel(it)
        }
    }
    medication.form?.let {
        item {
            FormLabel(it)
        }
    }
    medication.manufacturingInstructions?.let {
        item {
            ManufacturingInstructionsLabel(it)
        }
    }
    item {
        CategoryLabel(medication.category)
    }
    item {
        VaccineLabel(medication.vaccine)
    }
    medication.ingredients.forEachIndexed { index, ingredient ->
        item {
            IngredientNameLabel(ingredient.text, index + 1) {
                onClickIngredient(ingredient)
            }
        }
    }
    medication.lotNumber?.let {
        item {
            LotNumberLabel(it)
        }
    }
    medication.expirationDate?.let {
        item {
            ExpirationDateLabel(it)
        }
    }
}

private fun LazyListScope.freeTextMedicationInformation(medication: SyncedTaskData.MedicationFreeText) {
    item {
        Label(text = medication.name(), label = stringResource(R.string.medication_freetext_name))
    }
    item {
        CategoryLabel(medication.category)
    }
    item {
        VaccineLabel(medication.vaccine)
    }
    medication.form?.let {
        item {
            FormLabel(it)
        }
    }
    medication.lotNumber?.let {
        item {
            LotNumberLabel(it)
        }
    }
    medication.expirationDate?.let {
        item {
            ExpirationDateLabel(it)
        }
    }
}

@Composable
private fun NormSizeLabel(normSizeCode: String) {
    val description = normSizeMapping[normSizeCode]?.let { resourceId ->
        stringResource(resourceId)
    }
    Label(
        text = "$normSizeCode${description?.let { " - $it" } ?: ""}",
        label = stringResource(id = R.string.pres_detail_medication_label_normsize)
    )
}

@Composable
private fun PZNLabel(uniqueIdentifier: String) {
    Label(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.PZN),
        text = uniqueIdentifier,
        label = stringResource(id = R.string.pres_detail_medication_label_pzn)
    )
}

@Composable
fun FormLabel(form: String) {
    Label(
        modifier = Modifier
            .testTag(TestTag.Prescriptions.Details.Medication.SupplyForm)
            .semantics {
                supplyForm = form
            },
        text = codeToFormMapping[form]?.let { resourceId ->
            stringResource(resourceId)
        } ?: form,
        label = stringResource(id = R.string.pres_detail_medication_label_dosage_form)
    )
}

@Composable
private fun DosageInstructionLabel(dosageInstruction: String?) {
    dosageInstruction?.let { instruction ->
        val text = if (instruction.lowercase() == "dj") {
            stringResource(R.string.pres_detail_medication_dj)
        } else {
            instruction
        }

        Label(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.DosageInstruction),
            text = text,
            label = stringResource(R.string.pres_detail_medication_label_dosage_instruction)
        )
    }
}

@Composable
private fun QuantityLabel(quantity: Int) {
    if (quantity > 0) {
        Label(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Quantity),
            text = quantity.toString(),
            label = stringResource(R.string.pres_detail_medication_label_quantity)
        )
    }
}

@Composable
private fun CategoryLabel(category: SyncedTaskData.MedicationCategory) {
    val text = when (category) {
        SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL -> stringResource(R.string.medicines_bandages)
        SyncedTaskData.MedicationCategory.BTM -> stringResource(R.string.narcotics)
        SyncedTaskData.MedicationCategory.AMVV -> stringResource(R.string.amvv)
        else -> null
    }

    Label(
        modifier = Modifier
            .testTag(TestTag.Prescriptions.Details.Medication.Category)
            .then(
                if (BuildKonfig.INTERNAL) {
                    Modifier.semantics {
                        medicationCategory = when (category) {
                            SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL -> "00"
                            SyncedTaskData.MedicationCategory.BTM -> "01"
                            SyncedTaskData.MedicationCategory.AMVV -> "02"
                            SyncedTaskData.MedicationCategory.SONSTIGES -> "03"
                            else -> null
                        }
                    }
                } else {
                    Modifier
                }
            ),
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_label_category)
    )
}

@Composable
private fun AmountLabel(amount: SyncedTaskData.Ratio) {
    Label(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Amount),
        text = "${amount.numerator?.value} ${amount.numerator?.unit}",
        label = stringResource(id = R.string.pres_detail_medication_label_amount)
    )
}

@Composable
private fun BvgLabel(bvg: Boolean) {
    val text = if (bvg) {
        stringResource(id = R.string.pres_detail_yes)
    } else {
        stringResource(id = R.string.pres_detail_no)
    }
    Label(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.BVG),
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_label_bvg)
    )
}

@Composable
private fun AuthoredOnLabel(authoredOn: Instant) {
    Label(
        text = remember { dateTimeMediumText(authoredOn, TimeZone.currentSystemDefault()) },
        label = stringResource(id = R.string.pres_detail_medication_label_authored_on)
    )
}

@Composable
private fun NoteLabel(note: String) {
    Label(
        text = note,
        label = stringResource(id = R.string.pres_detail_medication_label_note)
    )
}

@Composable
private fun SubstitutionLabel(substitutionInfo: Boolean) {
    val text = if (substitutionInfo) {
        stringResource(id = R.string.pres_detail_yes)
    } else {
        stringResource(id = R.string.pres_detail_no)
    }
    Label(
        modifier = Modifier
            .testTag(TestTag.Prescriptions.Details.Medication.SubstitutionAllowed)
            .semantics {
                substitutionAllowed = substitutionInfo
            },
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_label_subtitution)
    )
}

@Composable
private fun PackagingLabel(packaging: String) {
    Label(
        text = packaging,
        label = stringResource(id = R.string.pres_detail_medication_label_packaging)
    )
}

@Composable
private fun ManufacturingInstructionsLabel(manufacturingInstructions: String) {
    Label(
        text = manufacturingInstructions,
        label = stringResource(id = R.string.pres_detail_medication_label_manufacturing)
    )
}

@Composable
private fun PerformerLabel(performer: String) {
    Label(
        text = performer,
        label = stringResource(id = R.string.pres_detail_medication_label_performer)
    )
}

@Composable
private fun HandedOverLabel(whenHandedOver: FhirTemporal?) {
    whenHandedOver?.let {
        Label(
            text = it.formattedString(),
            label = stringResource(id = R.string.pres_detail_medication_label_handed_over)
        )
    }
}

@Composable
private fun ExpirationDateLabel(expirationDate: FhirTemporal) {
    Label(
        text = remember { temporalText(expirationDate, TimeZone.currentSystemDefault()) },
        label = stringResource(id = R.string.pres_detail_medication_label_expiration_date)
    )
}

@Composable
private fun VaccineLabel(isVaccine: Boolean) {
    val text = if (isVaccine) {
        stringResource(id = R.string.pres_detail_yes)
    } else {
        stringResource(id = R.string.pres_detail_no)
    }
    Label(
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_vaccine)
    )
}

@Composable
private fun LotNumberLabel(lotNumber: String) {
    Label(
        text = lotNumber,
        label = stringResource(id = R.string.pres_detail_medication_label_lot_number)
    )
}
