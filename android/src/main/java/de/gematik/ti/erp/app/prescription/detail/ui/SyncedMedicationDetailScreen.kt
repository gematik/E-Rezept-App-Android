/*
 * Copyright (c) 2023 gematik GmbH
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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.medicationCategory
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.codeToFormMapping
import de.gematik.ti.erp.app.prescription.repository.normSizeMapping
import de.gematik.ti.erp.app.substitutionAllowed
import de.gematik.ti.erp.app.supplyForm
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.dateTimeMediumText
import de.gematik.ti.erp.app.utils.temporalText
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAccessor

@Composable
fun SyncedMedicationDetailScreen(
    prescription: PrescriptionData.Synced,
    medication: PrescriptionData.Medication,
    onClickIngredient: (SyncedTaskData.Ingredient) -> Unit,
    onBack: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Screen),
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.synced_medication_detail_header),
        navigationMode = NavigationBarMode.Back,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
        actions = {}
    ) { innerPadding ->
        val med = when (medication) {
            is PrescriptionData.Medication.Dispense -> medication.medicationDispense.medication
            is PrescriptionData.Medication.Request -> medication.medicationRequest.medication
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Prescriptions.Details.Medication.Content),
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        ) {
            item {
                SpacerMedium()
            }
            when (med) {
                is SyncedTaskData.MedicationPZN -> pznMedicationInformation(med)
                is SyncedTaskData.MedicationIngredient -> ingredientMedicationInformation(med, onClickIngredient)
                is SyncedTaskData.MedicationCompounding -> compoundingMedicationInformation(med, onClickIngredient)
                is SyncedTaskData.MedicationFreeText -> freeTextMedicationInformation(med)
                null -> {}
            }

            prescriptionInformation(prescription.authoredOn)

            when (medication) {
                is PrescriptionData.Medication.Dispense ->
                    medicationDispense(medication.medicationDispense)

                is PrescriptionData.Medication.Request ->
                    medicationRequest(medication.medicationRequest)
            }
            item {
                SpacerMedium()
            }
        }
    }
}

fun LazyListScope.prescriptionInformation(authoredOn: Instant) {
    item {
        AuthoredOnLabel(authoredOn)
    }
}

fun LazyListScope.medicationRequest(medicationRequest: SyncedTaskData.MedicationRequest) {
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

fun LazyListScope.medicationDispense(medicationDispense: SyncedTaskData.MedicationDispense) {
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

fun LazyListScope.pznMedicationInformation(medication: SyncedTaskData.MedicationPZN) {
    item {
        Label(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Name),
            text = medication.text,
            label = stringResource(R.string.medication_trade_name)
        )
    }
    medication.amount?.let {
        item {
            AmountLabel(it)
        }
    }
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

fun LazyListScope.ingredientMedicationInformation(
    medication: SyncedTaskData.MedicationIngredient,
    onClickIngredient: (SyncedTaskData.Ingredient) -> Unit
) {
    medication.ingredients.forEachIndexed { index, ingredient ->
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
    medication.amount?.let {
        item {
            AmountLabel(it)
        }
    }
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

fun LazyListScope.compoundingMedicationInformation(
    medication: SyncedTaskData.MedicationCompounding,
    onClickIngredient: (SyncedTaskData.Ingredient) -> Unit
) {
    item {
        Label(
            text = medication.text.takeIf { it.isNotEmpty() },
            label = stringResource(R.string.medication_compounding_name)
        )
    }
    medication.amount?.let {
        item {
            AmountLabel(it)
        }
    }
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

fun LazyListScope.freeTextMedicationInformation(medication: SyncedTaskData.MedicationFreeText) {
    item {
        Label(text = medication.text, label = stringResource(R.string.medication_freetext_name))
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
fun NormSizeLabel(normSizeCode: String) {
    val description = normSizeMapping[normSizeCode]?.let { resourceId ->
        stringResource(resourceId)
    }
    Label(
        text = "$normSizeCode${description?.let { " - $it" } ?: ""}",
        label = stringResource(id = R.string.pres_detail_medication_label_normsize)
    )
}

@Composable
fun PZNLabel(uniqueIdentifier: String) {
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
fun DosageInstructionLabel(dosageInstruction: String?) {
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
fun QuantityLabel(quantity: Int) {
    if (quantity > 0) {
        Label(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Quantity),
            text = quantity.toString(),
            label = stringResource(R.string.pres_detail_medication_label_quantity)
        )
    }
}

@Composable
fun CategoryLabel(category: SyncedTaskData.MedicationCategory) {
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
fun AmountLabel(amount: SyncedTaskData.Ratio) {
    Label(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Medication.Amount),
        text = amount.numerator?.value + " " + amount.numerator?.unit,
        label = stringResource(id = R.string.pres_detail_medication_label_amount)
    )
}

@Composable
fun BvgLabel(bvg: Boolean) {
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
fun AuthoredOnLabel(authoredOn: Instant) {
    Label(
        text = remember { dateTimeMediumText(authoredOn, ZoneId.systemDefault()) },
        label = stringResource(id = R.string.pres_detail_medication_label_authored_on)
    )
}

@Composable
fun NoteLabel(note: String) {
    Label(
        text = note,
        label = stringResource(id = R.string.pres_detail_medication_label_note)
    )
}

@Composable
fun SubstitutionLabel(substitutionInfo: Boolean) {
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
fun PackagingLabel(packaging: String) {
    Label(
        text = packaging,
        label = stringResource(id = R.string.pres_detail_medication_label_packaging)
    )
}

@Composable
fun ManufacturingInstructionsLabel(manufacturingInstructions: String) {
    Label(
        text = manufacturingInstructions,
        label = stringResource(id = R.string.pres_detail_medication_label_manufacturing)
    )
}

@Composable
fun PerformerLabel(performer: String) {
    Label(
        text = performer,
        label = stringResource(id = R.string.pres_detail_medication_label_performer)
    )
}

@Composable
fun HandedOverLabel(whenHandedOver: Instant) {
    Label(
        text = remember { dateTimeMediumText(whenHandedOver) },
        label = stringResource(id = R.string.pres_detail_medication_label_handed_over)
    )
}

@Composable
fun ExpirationDateLabel(expirationDate: TemporalAccessor) {
    Label(
        text = remember { temporalText(expirationDate, ZoneId.systemDefault()) },
        label = stringResource(id = R.string.pres_detail_medication_label_expiration_date)
    )
}

@Composable
fun VaccineLabel(isVaccine: Boolean) {
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
fun LotNumberLabel(lotNumber: String) {
    Label(
        text = lotNumber,
        label = stringResource(id = R.string.pres_detail_medication_label_lot_number)
    )
}

@Composable
fun IngredientAmountLabel(amount: String) {
    Label(
        text = amount,
        label = stringResource(id = R.string.pres_detail_medication_label_ingredient_amount)
    )
}

@Composable
fun IngredientNumberLabel(number: String) {
    Label(
        text = number,
        label = stringResource(id = R.string.pres_detail_medication_label_ingredient_number)
    )
}

@Composable
fun IngredientNameLabel(text: String, index: Int? = null, onClickLabel: (() -> Unit)? = null) {
    Label(
        text = text,
        label = annotatedStringResource(
            id = R.string.pres_detail_medication_label_ingredient_name,
            index ?: ""
        ).toString(),
        onClick = onClickLabel
    )
}

@Composable
fun StrengthLabel(strength: SyncedTaskData.Ratio) {
    strength.numerator?.let {
        Label(
            text = it.value + " " + it.unit,
            label = stringResource(id = R.string.pres_detail_medication_label_ingredient_strength_unit)
        )
    }
}
