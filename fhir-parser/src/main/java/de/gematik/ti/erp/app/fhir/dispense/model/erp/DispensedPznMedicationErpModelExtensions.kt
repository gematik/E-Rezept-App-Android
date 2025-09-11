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

package de.gematik.ti.erp.app.fhir.dispense.model.erp

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DIGA_DEEP_LINK
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DIGA_REDEEM_CODE
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationVersionType
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationCategory.Version102
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationCategory.Version110
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationCategory.Version14
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.toTypedErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel.Companion.isDigaType
import de.gematik.ti.erp.app.fhir.error.fhirError
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import de.gematik.ti.erp.app.utils.ParserUtil.asNullableFhirTemporal
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

internal fun DispenseMedicationVersionType.toCategoryVersionMapper() =
    when (this) {
        DispenseMedicationVersionType.Pzn102,
        DispenseMedicationVersionType.Compounding102,
        DispenseMedicationVersionType.Ingredient102,
        DispenseMedicationVersionType.FreeText102 -> Version102

        DispenseMedicationVersionType.Pzn110,
        DispenseMedicationVersionType.Compounding110,
        DispenseMedicationVersionType.Ingredient110,
        DispenseMedicationVersionType.FreeText110 -> Version110

        DispenseMedicationVersionType.Epa14 -> Version14
    }

internal fun FhirMedicationDispenseV14V15DispenseModel.toErpModel(
    medication: FhirMedicationDispenseMedicationModel?
) = FhirMedicationDispenseErpModel(
    dispenseId = id ?: "",
    patientId = subject?.identifier?.value ?: "",
    substitutionAllowed = substitution?.wasSubstituted ?: false,
    dosageInstruction = dosageInstruction.map { it.text }.firstOrNull(),
    performer = performer.map { it.actor?.identifier?.value }.firstOrNull(),
    handedOver = whenHandedOver?.asFhirTemporal(),
    dispensedMedication = medication?.toTypedErpModel()?.let { listOf(it) } ?: emptyList(),
    dispensedDeviceRequest = when {
        isDigaType() -> toDeviceRequestErpModel()
        else -> null
    }
)

// https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
private fun FhirMedicationDispenseV14V15DispenseModel.dataAbsentCodeIfNoRedeemCode(): String? {
    val redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString

    if (!redeemCode.isNullOrEmpty()) {
        return null
    }

    val declineCode = medicationReference?.declineCode
    return declineCode ?: fhirError("Diga medication dispense decline code rule failed: required decline code is missing")
}

private fun FhirMedicationDispenseV14V15DispenseModel.toDeviceRequestErpModel(): FhirDispenseDeviceRequestErpModel {
    return FhirDispenseDeviceRequestErpModel(
        declineCode = dataAbsentCodeIfNoRedeemCode(),
        referencePzn = if (isRedeemCodePresent()) medicationReference!!.pzn else medicationReference?.pzn,
        display = if (isRedeemCodePresent()) medicationReference!!.displayInfo else medicationReference?.displayInfo,
        note = fallbackNoteIfNoActivationCode(),
        redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString,
        deepLink = extension.findExtensionByUrl(DIGA_DEEP_LINK)?.valueUrl,
        modifiedDate = whenPrepared?.asNullableFhirTemporal() ?: whenHandedOver?.asNullableFhirTemporal(),
        status = status
    )
}

// https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
private fun FhirMedicationDispenseV14V15DispenseModel.isRedeemCodePresent() =
    extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString.isNotNullOrEmpty()

// https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
private fun FhirMedicationDispenseV14V15DispenseModel.fallbackNoteIfNoActivationCode(): String? {
    val redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString

    if (!redeemCode.isNullOrEmpty()) {
        return null
    }

    val noteText = note.firstOrNull()?.text
    return noteText ?: fhirError("Diga medication dispense note rule failed: required note is missing")
}
