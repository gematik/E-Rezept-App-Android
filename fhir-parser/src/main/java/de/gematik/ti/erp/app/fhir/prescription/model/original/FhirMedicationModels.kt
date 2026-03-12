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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept.Companion.getCodingByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtensionReduced.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMedicationBatch
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatioValue
import de.gematik.ti.erp.app.fhir.common.model.original.FhirReference
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.findMedicationCategory
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.findMedicationCategorySpecialVersion102
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getCompoundingInstructions
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getCompoundingPackaging
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getNormSizeCode
import de.gematik.ti.erp.app.fhir.constant.prescription.medication.FhirMedicationConstants.getVaccine
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel.Companion.fromCode
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getRatio
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getRatioSpecialVersion102
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationIngredient.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfile.Companion.getMedicationProfile
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfileType.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfileType.FreeText
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfileType.PZN
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationVersion.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class FhirMedication(
    @SerialName("meta") val resourceType: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension>? = emptyList(),
    @SerialName("identifier") val identifier: List<FhirIdentifier>? = emptyList(), // from pkv
    @SerialName("code") val code: FhirCodeableConcept? = null,
    @SerialName("form") val form: FhirCodeableConcept? = null,
    @SerialName("amount") val amount: FhirMedicationAmount? = null,
    @SerialName("ingredient") val ingredients: List<FhirMedicationIngredient> = emptyList(),
    @SerialName("batch") val batch: FhirMedicationBatch? = null,
    // added on 1.6 version
    @SerialName("contained") val contained: List<JsonElement> = emptyList()
) {
    val profile: FhirMedicationProfile?
        get() = resourceType?.getMedicationProfile()

    val formText: String?
        get() =
            when (profile?.profileType) {
                PZN -> form?.getCodingByUrl(MEDICATION_PZN_FORM_TEXT_SYSTEM)?.code
                else -> form?.text
            }

    val amountRatio: FhirRatio?
        get() {
            if (profile?.profileType == FreeText) return null
            return when (profile?.version) {
                FhirMedicationVersion.V_102 -> amount?.getRatioSpecialVersion102()
                FhirMedicationVersion.V_110,
                FhirMedicationVersion.V_12,
                FhirMedicationVersion.V_13,
                FhirMedicationVersion.V_14,
                FhirMedicationVersion.V_15,
                FhirMedicationVersion.V_16
                -> amount?.getRatio()

                else -> null
            }
        }

    val medicationCategory: String?
        get() = when (profile?.version) {
            FhirMedicationVersion.V_102 -> extensions?.findMedicationCategorySpecialVersion102()
            FhirMedicationVersion.V_110,
            FhirMedicationVersion.V_12,
            FhirMedicationVersion.V_13,
            FhirMedicationVersion.V_14,
            FhirMedicationVersion.V_15,
            FhirMedicationVersion.V_16
            -> extensions?.findMedicationCategory()

            else -> null
        }

    val vaccine: Boolean
        get() = extensions?.getVaccine() == true

    val normSizeCode: String?
        get() = extensions?.getNormSizeCode()

    val compoundingInstructions: String?
        get() = extensions?.getCompoundingInstructions()

    val compoundingPackaging: String?
        get() = extensions?.getCompoundingPackaging()

    fun getIdentifiers(identifierUrl: String): String? =
        runCatching {
            code?.coding?.find { it.system == identifierUrl }?.code ?: ingredients.flatMap { it.itemCodeableConcept?.coding.orEmpty() }
                .find { it.system == identifierUrl }?.code
        }.onFailure {
            Napier.e("Error getting identifier: ${it.message}")
        }.getOrNull()

    fun getDisplayName(): String? {
        return code?.text ?: code?.coding?.firstOrNull()?.display ?: ingredients.firstOrNull()?.let { ingredient ->
            ingredient.itemCodeableConcept?.text ?: ingredient.itemCodeableConcept?.coding?.firstOrNull()?.display
        }
    }

    fun resolveContainedMedication(reference: String?): FhirMedication? {
        val referenceId = reference?.removePrefix("#") ?: return null
        return contained
            .find { it.jsonObject["id"]?.jsonPrimitive?.content == referenceId }
            ?.let {
                try {
                    SafeJson.value.decodeFromJsonElement(serializer(), it)
                } catch (e: Exception) {
                    Napier.e("Error decoding contained medication: ${e.message}")
                    null
                }
            }
    }

    fun getIdentifierErpModel(): FhirMedicationIdentifierErpModel {
        return FhirMedicationIdentifierErpModel(
            pzn = getIdentifiers(FhirConstants.PZN_IDENTIFIER),
            atc = getIdentifiers(FhirConstants.ATC_IDENTIFIER),
            ask = getIdentifiers(FhirConstants.ASK_IDENTIFIER),
            snomed = getIdentifiers(FhirConstants.SNOMED_IDENTIFIER)
        )
    }

    companion object {

        private const val MEDICATION_PZN_FORM_TEXT_SYSTEM = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"
        private fun JsonElement.isValidMedication(): Boolean = isValidKbvResource(
            FhirKbvResourceType.Medication
        )

        fun JsonElement.getMedication(): FhirMedication? {
            if (!this.isValidMedication()) return null
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e("Error parsing Medication: ${e.message}")
                null
            }
        }

        fun FhirMedication.toErpModel() =
            FhirTaskKbvMedicationErpModel(
                text = code?.text,
                form = formText,
                medicationCategory = fromCode(medicationCategory),
                medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                    type = profile?.profileType?.toErpModel() ?: ErpMedicationProfileType.Unknown,
                    version = profile?.version?.toErpModel() ?: ErpMedicationProfileVersion.Unknown
                ),
                amount = amountRatio?.toErpModel(),
                isVaccine = vaccine,
                normSizeCode = normSizeCode,
                compoundingInstructions = compoundingInstructions,
                compoundingPackaging = compoundingPackaging,
                ingredients = ingredients.map { it.toErpModel(this) },
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                identifier = getIdentifierErpModel()
            )
    }
}

// NOTE: When modifying this please change ErpMedicationProfileType too
enum class FhirMedicationProfileType(val type: String) {
    PZN("Medication_PZN"),
    FreeText("Medication_FreeText"),
    Compounding("Medication_Compounding"),
    Ingredient("Medication_Ingredient"),
    Unknown("")
    ;

    companion object {
        fun fromString(value: String): FhirMedicationProfileType = entries.find { it.type == value } ?: Unknown

        fun FhirMedicationProfileType.toErpModel(): ErpMedicationProfileType =
            ErpMedicationProfileType.entries.find { it.name == this.name } ?: ErpMedicationProfileType.Unknown
    }
}

// NOTE: When modifying this please change ErpMedicationVersion too
enum class FhirMedicationVersion(val version: String) {
    V_102("1.0.2"),
    V_110("1.1.0"),
    V_12("1.2"),
    V_13("1.3"),
    V_14("1.4"),
    V_15("1.5"),
    V_16("1.6"),
    Unknown("")
    ;

    companion object {
        fun fromString(value: String) = entries.find { it.version == value } ?: Unknown

        fun FhirMedicationVersion.toErpModel(): ErpMedicationProfileVersion =
            ErpMedicationProfileVersion.entries.find { it.name == this.name } ?: ErpMedicationProfileVersion.Unknown
    }
}

@Serializable
internal data class FhirMedicationProfile(
    val profileType: FhirMedicationProfileType,
    val version: FhirMedicationVersion
) {
    companion object {
        private fun extractMedicationProfile(url: String): FhirMedicationProfile? {
            val regex = Regex(""".*KBV_PR_ERP_([A-Za-z_]+)\|(\d+(?:\.\d+)+)""")
            val gemRegex = Regex(""".*GEM_ERP_PR_Medication\|(\d+(?:\.\d+)+)""")

            val match = regex.find(url)
            if (match != null) {
                val (type, version) = match.destructured
                return FhirMedicationProfile(
                    profileType = FhirMedicationProfileType.fromString(type),
                    version = FhirMedicationVersion.fromString(version)
                )
            }

            val gemMatch = gemRegex.find(url)
            if (gemMatch != null) {
                val (version) = gemMatch.destructured
                return FhirMedicationProfile(
                    profileType = PZN, // GEM profile is currently only for PZN-like medications in our model
                    version = FhirMedicationVersion.fromString(version)
                )
            }

            Napier.e { "FhirMedicationProfile $url regex failed match" }
            return null
        }

        fun FhirMeta.getMedicationProfile(): FhirMedicationProfile? {
            val profileType = profiles.firstOrNull()
            if (profileType == null) return null
            return extractMedicationProfile(profileType)
        }
    }
}

// TODO: move to common
@Serializable
internal data class FhirMedicationAmount(
    @SerialName("numerator") val numerator: FhirMedicationAmountNumerator? = null,
    @SerialName("denominator") val denominator: FhirRatioValue? = null
) {
    companion object {

        private const val MEDICATION_PACKAGING_SIZE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize"

        // added in from 1.6 version
        private const val EPA_MEDICATION_QUANTITY_EXTENSION =
            "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-total-quantity-formulation-extension"

        /**
         * A specific mapper specialized to medication for version 1.0.2 in KBV, dispense
         */
        fun FhirMedicationAmount.getRatioSpecialVersion102() = FhirRatio(
            numerator = FhirRatioValue(
                value = numerator?.valueDirect,
                unit = numerator?.unit
            ),
            denominator = FhirRatioValue(
                value = denominator?.value,
                unit = denominator?.unit
            )
        )

        /**
         * A specific mapper specialized to medication for version 1.1.0 in KBV, dispense
         */
        fun FhirMedicationAmount.getRatio(): FhirRatio {
            val numeratorExtension = numerator?.valueFromExtension
                ?.find { it.url == MEDICATION_PACKAGING_SIZE_EXTENSION_URL || it.url == EPA_MEDICATION_QUANTITY_EXTENSION }

            val numerator = FhirRatioValue(
                value = numeratorExtension?.valueString,
                unit = numerator?.unit
            )

            return FhirRatio(
                numerator = numerator,
                denominator = FhirRatioValue(
                    value = "1", // fixed value
                    unit = ""
                )
            )
        }
    }
}

@Serializable
internal data class FhirMedicationIngredient(
    @SerialName("itemCodeableConcept") val itemCodeableConcept: FhirCodeableConcept? = null,
    @SerialName("itemReference") val itemReference: FhirReference? = null,
    @SerialName("strength") val strength: FhirRatio? = null
) {
    val text = itemCodeableConcept?.text
    val amount = strength?.extensions?.findExtensionByUrl(MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL)?.valueString
    val form = strength?.extensions?.findExtensionByUrl(MEDICATION_INGREDIENT_FORM_EXTENSION_URL)?.valueString

    companion object {
        internal const val MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount"
        internal const val MEDICATION_INGREDIENT_FORM_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form"

        fun FhirMedicationIngredient.toErpModel(
            medicationForIdentifier: FhirMedication
        ): FhirMedicationIngredientErpModel {
            val referencedMedication = medicationForIdentifier.resolveContainedMedication(itemReference?.value)
            val identifier = referencedMedication?.getIdentifierErpModel() ?: medicationForIdentifier.getIdentifierErpModel()

            return FhirMedicationIngredientErpModel(
                text = referencedMedication?.getDisplayName() ?: text,
                amount = amount,
                form = form,
                strengthRatio = strength?.toErpModel(),
                identifier = identifier
            )
        }
    }
}

@Serializable
internal data class FhirMedicationAmountNumerator(
    @SerialName("value") val valueDirect: String? = null,
    @SerialName("extension") val valueFromExtension: List<FhirMedicationAmountValueExtension> = emptyList(),
    @SerialName("unit") val unit: String? = null,
    @SerialName("system") val system: String? = null,
    @SerialName("code") val code: String? = null
)

@Serializable
internal data class FhirMedicationAmountValueExtension(
    @SerialName("url") val url: String? = null,
    @SerialName("valueString") val valueString: String? = null
)
