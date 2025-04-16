/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtensionReduced.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatioValue
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMedicationCategoryErpModel.Companion.fromCode
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getAmount102
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount.Companion.getAmount110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.findMedicationCategory102
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.findMedicationCategory110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.getCompoundingInstructions
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.getCompoundingPackaging
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.getNormSizeCode
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationExtensions.getVaccine
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationIngredient.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfile.Companion.getMedicationProfile
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfileType.FreeText
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationProfileType.PZN
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirMedication(
    @SerialName("meta") val resourceType: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension>? = emptyList(),
    @SerialName("code") val code: FhirCodeableConcept? = null,
    @SerialName("form") val form: FhirCodeableConcept? = null,
    @SerialName("amount") val amount: FhirMedicationAmount? = null,
    @SerialName("ingredient") val ingredients: List<FhirMedicationIngredient> = emptyList(),
    @SerialName("batch") val batch: FhirMedicationBatch? = null
) {
    val profile: FhirMedicationProfile?
        get() = resourceType?.getMedicationProfile()

    val formText: String?
        get() =
            when (profile?.profileType) {
                PZN -> form?.coding?.find { it.system == MEDICATION_PZN_FORM_TEXT_SYSTEM }?.code
                else -> form?.text
            }

    val amountRatio: FhirRatio?
        get() = when {
            profile?.profileType == FreeText -> null
            profile?.version == FhirMedicationVersion.V_102 -> amount?.getAmount102()
            profile?.version == FhirMedicationVersion.V_110 -> amount?.getAmount110()
            else -> null
        }

    val medicationCategory: String?
        get() = when (profile?.version) {
            FhirMedicationVersion.V_102 -> extensions?.findMedicationCategory102()
            FhirMedicationVersion.V_110 -> extensions?.findMedicationCategory110()
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
                type = profile?.profileType?.type ?: "",
                version = profile?.version?.version ?: "",
                form = formText,
                medicationCategory = fromCode(medicationCategory),
                amount = amountRatio?.toErpModel(),
                isVaccine = vaccine,
                normSizeCode = normSizeCode,
                compoundingInstructions = compoundingInstructions,
                compoundingPackaging = compoundingPackaging,
                ingredients = ingredients.map { it.toErpModel(this) },
                lotNumber = batch?.lotNumber,
                expirationDate = batch?.expirationDate?.asFhirTemporal(),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = getIdentifiers(FhirConstants.PZN_IDENTIFIER),
                    atc = getIdentifiers(FhirConstants.ATC_IDENTIFIER),
                    ask = getIdentifiers(FhirConstants.ASK_IDENTIFIER),
                    snomed = getIdentifiers(FhirConstants.SNOMED_IDENTIFIER)
                )
            )
    }
}

@Serializable
internal data class FhirMedicationBatch(
    @SerialName("lotNumber") val lotNumber: String? = null,
    @SerialName("expirationDate") val expirationDate: String? = null
)

object FhirMedicationExtensions {
    private const val MEDICATION_CATEGORY = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"
    private const val MEDICATION_CATEGORY_SYSTEM = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category"
    private const val MEDICATION_VACCINE_EXTENSION = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine"
    private const val MEDICATION_NORM_GROSSE = "http://fhir.de/StructureDefinition/normgroesse"
    private const val MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction"
    private const val MEDICATION_COMPOUNDING_PACKAGING = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging"

    internal fun List<FhirExtension>.findMedicationCategory110() =
        findExtensionByUrl(MEDICATION_CATEGORY)?.valueCoding?.code

    internal fun List<FhirExtension>.findMedicationCategory102(): String? =
        findExtensionByUrl(MEDICATION_CATEGORY)
            ?.takeIf { it.valueCoding?.system == MEDICATION_CATEGORY_SYSTEM }
            ?.valueCoding?.code

    internal fun List<FhirExtension>.getVaccine(): Boolean =
        findExtensionByUrl(MEDICATION_VACCINE_EXTENSION)?.valueBoolean == true

    internal fun List<FhirExtension>.getNormSizeCode(): String? = findExtensionByUrl(MEDICATION_NORM_GROSSE)?.valueCode

    internal fun List<FhirExtension>.getCompoundingInstructions(): String? =
        findExtensionByUrl(MEDICATION_COMPOUNDING_MANUFACTURING_INSTRUCTIONS)?.valueString

    internal fun List<FhirExtension>.getCompoundingPackaging(): String? =
        findExtensionByUrl(MEDICATION_COMPOUNDING_PACKAGING)?.valueString
}

enum class FhirMedicationProfileType(val type: String) {
    PZN("Medication_PZN"),
    FreeText("Medication_FreeText"),
    Compounding("Medication_Compounding"),
    Ingredient("Medication_Ingredient"),
    Unknown("");

    companion object {
        fun fromString(value: String): FhirMedicationProfileType = entries.find { it.type == value } ?: Unknown
    }
}

enum class FhirMedicationVersion(val version: String) {
    V_102("1.0.2"),
    V_110("1.1.0"),
    Unknown("");

    companion object {
        fun fromString(value: String) = entries.find { it.version == value } ?: Unknown
    }
}

@Serializable
internal data class FhirMedicationProfile(
    val profileType: FhirMedicationProfileType,
    val version: FhirMedicationVersion
) {
    companion object {
        private fun extractMedicationProfile(url: String): FhirMedicationProfile? {
            val regex = Regex(""".*KBV_PR_ERP_([A-Za-z_]+)\|(\d+\.\d+\.\d+)""")
            val match = regex.find(url) ?: return null

            val (type, version) = match.destructured
            return FhirMedicationProfile(
                profileType = FhirMedicationProfileType.fromString(type),
                version = FhirMedicationVersion.fromString(version)
            )
        }

        fun FhirMeta.getMedicationProfile(): FhirMedicationProfile? {
            val profileType = profiles.firstOrNull()
            profileType?.let {
                return extractMedicationProfile(it)
            } ?: return null
        }
    }
}

@Serializable
internal data class FhirMedicationAmount(
    @SerialName("numerator") val numerator: FhirMedicationAmountNumerator? = null,
    @SerialName("denominator") val denominator: FhirRatioValue? = null
) {
    companion object {

        private const val MEDICATION_PACKAGING_SIZE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize"

        fun FhirMedicationAmount.getAmount102() = FhirRatio(
            numerator = FhirRatioValue(
                value = numerator?.valueDirect,
                unit = numerator?.unit
            ),
            denominator = FhirRatioValue(
                value = denominator?.value,
                unit = denominator?.unit
            )
        )

        fun FhirMedicationAmount.getAmount110(): FhirRatio {
            val numeratorExtension = numerator?.valueFromExtension
                ?.find { it.url == MEDICATION_PACKAGING_SIZE_EXTENSION_URL }

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
    @SerialName("strength") val strength: FhirRatio? = null
) {
    companion object {
        private const val MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount"
        private const val MEDICATION_INGREDIENT_FORM_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form"

        fun FhirMedicationIngredient.toErpModel(
            medicationForIdentifier: FhirMedication // todo: Not sure if this correct {parseIdentifier} is confusing
        ) = FhirMedicationIngredientErpModel(
            text = itemCodeableConcept?.text,
            amount = strength?.extensions?.findExtensionByUrl(
                MEDICATION_INGREDIENT_AMOUNT_EXTENSION_URL
            )?.valueString,
            form = strength?.extensions?.findExtensionByUrl(MEDICATION_INGREDIENT_FORM_EXTENSION_URL)?.valueString,
            strengthRatio = strength?.toErpModel(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = medicationForIdentifier.getIdentifiers(FhirConstants.PZN_IDENTIFIER),
                atc = medicationForIdentifier.getIdentifiers(FhirConstants.ATC_IDENTIFIER),
                ask = medicationForIdentifier.getIdentifiers(FhirConstants.ASK_IDENTIFIER),
                snomed = medicationForIdentifier.getIdentifiers(FhirConstants.SNOMED_IDENTIFIER)
            )
        )
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
