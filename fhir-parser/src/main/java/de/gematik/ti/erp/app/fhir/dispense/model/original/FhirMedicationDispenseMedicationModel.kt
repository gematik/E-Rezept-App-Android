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

package de.gematik.ti.erp.app.fhir.dispense.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept.Companion.getCodingByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMedicationBatch
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationReducedVersion.Companion.toReducedVersion
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationType
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationType.Companion.toDispenseMedicationType
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationVersionType.Companion.fromRaw
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getCompoundingInstructions
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getCompoundingPackaging
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getEpaCompoundingInstructions
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getEpaCompoundingPackaging
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getEpaVaccine
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getNormSizeCode
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getVaccine
import de.gematik.ti.erp.app.fhir.dispense.model.CompoundingContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedEpaMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedIngredientMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedPznMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.EpaContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedCompoundingMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedFreeTextMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.IngredientContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.PznContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.erp.toCategoryVersionMapper
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.inferMedicationTypeOnUnknown
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getRatio102
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getRatio110
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirIngredientListSerializer
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a legacy `Medication` resource that is embedded (contained) inside a `MedicationDispense` resource.
 *
 * This structure is only used for all versions (e.g., 1.2, 1.4) where the medication details are included within
 * the `contained` field of the `MedicationDispense` payload.
 *
 * It holds coding information (e.g., PZN), form, amount, ingredients, and batch data.
 *
 * @property resourceType Usually "Medication", but kept nullable for safety.
 * @property id Resource ID (internal reference).
 * @property meta Metadata such as version or profile.
 * @property extensions Custom extensions (e.g., BVG indicators).
 * @property code Medication code (e.g., ATC, PZN) and textual representation.
 * @property form Dosage form (e.g., tablet, capsule).
 * @property amount Amount of medication.
 * @property ingredients List of medication ingredients (e.g., active substances).
 * @property batch Batch information including lot number and expiration.
 * @property medications Medication information which could be another medication as an ingredient
 */
@Serializable
internal data class FhirMedicationDispenseMedicationModel(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("code") val code: FhirCodeableConcept? = null,
    @SerialName("itemCodeableConcept") val itemCodeableConcept: FhirCodeableConcept? = null,
    @SerialName("form") val form: FhirCodeableConcept? = null,
    @SerialName("amount") val amount: FhirMedicationAmount? = null,
    @Serializable(with = SafeFhirIngredientListSerializer::class)
    @SerialName("ingredient")
    val ingredients: List<FhirDispenseIngredient> = emptyList(),
    @SerialName("batch") val batch: FhirMedicationBatch? = null,
    @SerialName("contained") val medications: List<FhirMedicationDispenseMedicationModel> = emptyList()
) {
    companion object {

        private fun FhirMedicationDispenseMedicationModel.extractInternalMedications() =
            medications.filter { it.resourceType == FhirMediationDispenseResourceType.Medication.name }

        private fun FhirMedicationDispenseMedicationModel.getMedicationTypeVersion(): FhirMedicationDispenseConstants.DispenseMedicationVersionType? {
            val types = meta?.profiles?.getOrNull(0)?.split("|")
            return types?.getOrNull(1)?.fromRaw()
        }

        private fun FhirMedicationDispenseMedicationModel.getCategory(
            version: FhirMedicationDispenseConstants.DispenseMedicationVersionType?
        ) = version?.let {
            extensions.findExtensionByUrl(it.toCategoryVersionMapper().url)?.valueCoding?.code
        }

        @Suppress("ktlint:max-line-length")
        private fun FhirMedicationDispenseMedicationModel.getAmountRatio(version: FhirMedicationDispenseConstants.DispenseMedicationVersionType?) =
            when (version?.toReducedVersion()) {
                FhirMedicationDispenseConstants.DispenseMedicationReducedVersion.V_1_1_0 -> amount?.getRatio110()
                FhirMedicationDispenseConstants.DispenseMedicationReducedVersion.V_1_0_2,
                FhirMedicationDispenseConstants.DispenseMedicationReducedVersion.V_1_4 -> amount?.getRatio102()

                null -> null
            }

        private fun FhirMedicationDispenseMedicationModel.getIdentifierValue(url: String) =
            code?.getCodingByUrl(url)?.code ?: itemCodeableConcept?.getCodingByUrl(url)?.code

        private fun FhirMedicationDispenseMedicationModel.getErpModelIdentifier() =
            FhirMedicationIdentifierErpModel(
                pzn = getIdentifierValue(FhirConstants.PZN_IDENTIFIER),
                atc = getIdentifierValue(FhirConstants.ATC_IDENTIFIER),
                ask = getIdentifierValue(FhirConstants.ASK_IDENTIFIER),
                snomed = getIdentifierValue(FhirConstants.SNOMED_IDENTIFIER)
            )

        private fun List<FhirDispenseIngredient>.mapToErpIngredients(
            identifier: FhirMedicationIdentifierErpModel
        ): List<FhirMedicationIngredientErpModel> =
            this.mapNotNull {
                (it as? FhirCodeableIngredient)?.let { codeable ->
                    FhirMedicationIngredientErpModel(
                        text = codeable.text,
                        amount = codeable.amount,
                        form = codeable.form,
                        strengthRatio = codeable.strength?.toErpModel(),
                        identifier = identifier
                    )
                }
            }

        private fun FhirMedicationDispenseMedicationModel.getMedicationType(): DispenseMedicationType {
            val types = meta?.profiles?.getOrNull(0)?.split("|")
            return types?.getOrNull(0)?.toDispenseMedicationType() ?: DispenseMedicationType.Unknown
        }

        private fun FhirMedicationDispenseMedicationModel.inferMedicationTypeOnUnknown(): DispenseMedicationType? {
            return when {
                code?.getCodingByUrl(FhirConstants.PZN_IDENTIFIER) != null -> DispenseMedicationType.Pzn

                ingredients.any { it is FhirCodeableIngredient } &&
                    extensions.getCompoundingInstructions() != null -> DispenseMedicationType.Compounding

                ingredients.any { it is FhirCodeableIngredient } -> DispenseMedicationType.Ingredient

                code?.text != null &&
                    code.coding.isNullOrEmpty() &&
                    ingredients.isEmpty() -> DispenseMedicationType.FreeText

                medications.any { it.resourceType == "Medication" } -> DispenseMedicationType.EpaTypeI

                else -> null
            }
        }

        private fun FhirMedicationDispenseMedicationModel.toPznMedicationErpModel(): DispensedPznMedicationErpModel {
            val version = getMedicationTypeVersion()

            return DispensedPznMedicationErpModel(
                text = code?.text,
                category = getCategory(version),
                form = form?.getCodingByUrl(FhirMedicationDispenseConstants.MEDICATION_DISPENSE_PZN_EPA_FORM_URL)?.code,
                amount = getAmountRatio(version)?.toErpModel(),
                isVaccine = extensions.getVaccine(),
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                contextualData = PznContextualData(
                    identifier = getErpModelIdentifier(),
                    normSizeCode = extensions.getNormSizeCode()
                )
            )
        }

        private fun FhirMedicationDispenseMedicationModel.toCompoundingMedicationErpModel(): FhirDispensedCompoundingMedicationErpModel {
            val version = getMedicationTypeVersion()

            return FhirDispensedCompoundingMedicationErpModel(
                text = code?.text,
                category = getCategory(version),
                form = form?.text,
                amount = getAmountRatio(version)?.toErpModel(),
                isVaccine = extensions.getVaccine(),
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                contextualData = CompoundingContextualData(
                    identifier = getErpModelIdentifier(),
                    manufacturingInstructions = extensions.getCompoundingInstructions(),
                    packaging = extensions.getCompoundingPackaging(),
                    ingredients = ingredients.mapToErpIngredients(getErpModelIdentifier())
                )
            )
        }

        private fun FhirMedicationDispenseMedicationModel.toIngredientMedicationErpModel(): DispensedIngredientMedicationErpModel {
            val version = getMedicationTypeVersion()

            return DispensedIngredientMedicationErpModel(
                text = code?.text,
                category = getCategory(version),
                form = form?.text,
                amount = getAmountRatio(version)?.toErpModel(),
                isVaccine = extensions.getVaccine(),
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                contextualData = IngredientContextualData(
                    identifier = getErpModelIdentifier(),
                    normSizeCode = extensions.getNormSizeCode(),
                    ingredients = ingredients.mapToErpIngredients(getErpModelIdentifier())
                )
            )
        }

        private fun FhirMedicationDispenseMedicationModel.toFreeTextMedicationErpModel(): FhirDispensedFreeTextMedicationErpModel {
            val version = getMedicationTypeVersion()

            return FhirDispensedFreeTextMedicationErpModel(
                text = code?.text,
                category = getCategory(version),
                form = form?.text,
                amount = getAmountRatio(version)?.toErpModel(),
                isVaccine = extensions.getVaccine(),
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal()
            )
        }

        private fun FhirMedicationDispenseMedicationModel.toEpaMedicationErpModel(): DispensedEpaMedicationErpModel {
            val version = getMedicationTypeVersion()

            return DispensedEpaMedicationErpModel(
                text = code?.text,
                category = getCategory(version),
                form = form?.getCodingByUrl(FhirMedicationDispenseConstants.MEDICATION_DISPENSE_PZN_EPA_FORM_URL)?.code,
                amount = getAmountRatio(version)?.toErpModel(),
                isVaccine = extensions.getEpaVaccine(),
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                contextualData = EpaContextualData(
                    identifier = getErpModelIdentifier(),
                    normSizeCode = extensions.getNormSizeCode(),
                    manufacturingInstructions = extensions.getEpaCompoundingInstructions(),
                    packaging = extensions.getEpaCompoundingPackaging(),
                    ingredients = ingredients
                        .takeIf { it.filterIsInstance<FhirReferenceIngredient>().isEmpty() }
                        ?.mapToErpIngredients(getErpModelIdentifier()) ?: emptyList(),
                    internalMedication = extractInternalMedications().map { it.toEpaMedicationErpModel() }
                )
            )
        }

        /**
         * Maps a [DispenseMedicationType] to its corresponding transformation function that converts
         * the raw [FhirMedicationDispenseMedicationModel] into a concrete [DispensedMedicationErpModel].
         *
         * This method centralizes the transformation logic and ensures consistency across all known medication types.
         *
         * @param model The raw FHIR medication resource to convert.
         * @return The matching [DispensedMedicationErpModel], or `null` if the type is [DispenseMedicationType.Unknown].
         */
        private fun DispenseMedicationType.toErpModel(
            model: FhirMedicationDispenseMedicationModel
        ): DispensedMedicationErpModel? = when (this) {
            DispenseMedicationType.Pzn -> model.toPznMedicationErpModel()
            DispenseMedicationType.Compounding -> model.toCompoundingMedicationErpModel()
            DispenseMedicationType.Ingredient -> model.toIngredientMedicationErpModel()
            DispenseMedicationType.FreeText -> model.toFreeTextMedicationErpModel()
            DispenseMedicationType.EpaTypeI,
            DispenseMedicationType.EpaTypeII -> model.toEpaMedicationErpModel()

            DispenseMedicationType.Unknown -> null
        }

        internal fun JsonElement.getMedicationDispensedMedication(): FhirMedicationDispenseMedicationModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }

        /**
         * Converts a [FhirMedicationDispenseMedicationModel] into its corresponding [DispensedMedicationErpModel],
         * based on its detected [DispenseMedicationType].
         *
         * If the medication type is [DispenseMedicationType.Unknown], it will attempt to infer the type using
         * [inferMedicationTypeOnUnknown] and then convert accordingly.
         *
         * @return A concrete subclass of [DispensedMedicationErpModel] if the type is known or inferred correctly,
         * or `null` if no valid type mapping is found.
         */
        internal fun FhirMedicationDispenseMedicationModel.toTypedErpModel(): DispensedMedicationErpModel? {
            val type = getMedicationType()
                .takeUnless { it == DispenseMedicationType.Unknown }
                ?: inferMedicationTypeOnUnknown()

            return type?.toErpModel(this)
        }
    }
}
