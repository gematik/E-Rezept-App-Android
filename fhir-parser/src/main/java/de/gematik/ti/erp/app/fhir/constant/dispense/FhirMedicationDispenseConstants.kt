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

package de.gematik.ti.erp.app.fhir.constant.dispense

object FhirMedicationDispenseConstants {
    const val VERSION_1_4 = "1.4"

    // PZN, EPA
    const val MEDICATION_DISPENSE_PZN_EPA_FORM_URL =
        "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"

    /**
     * Represents the supported FHIR medication types used in the eRezept (electronic prescription) system.
     *
     * Each enum entry corresponds to a specific [StructureDefinition] profile defined by either KBV or gematik.
     * These types are used to determine how to parse and convert FHIR `Medication` resources into domain-specific
     * ERP medication dispense models.
     *
     * @property url The canonical URL that identifies the profile for this medication type.
     */
    enum class DispenseMedicationType(val url: String) {
        Pzn("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN"),
        Compounding("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding"),
        Ingredient("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient"),
        FreeText("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText"),
        EpaTypeI("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Medication"),
        EpaTypeII("https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pharmaceutical-product"),
        Unknown("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Unknown");

        companion object {
            /**
             * Attempts to resolve a [DispenseMedicationType] from the given [url].
             *
             * @receiver The profile URL to map.
             * @return The matching [DispenseMedicationType], or `null` if no match is found.
             */
            internal fun String.toDispenseMedicationType(): DispenseMedicationType? =
                DispenseMedicationType.entries.find { it.url == this }
        }
    }

    enum class DispenseMedicationReducedVersion {
        V_1_1_0,
        V_1_0_2,
        V_1_4;

        companion object {
            fun DispenseMedicationVersionType.toReducedVersion() = when (this) {
                is DispenseMedicationVersionType.Pzn102,
                is DispenseMedicationVersionType.Compounding102,
                is DispenseMedicationVersionType.Ingredient102,
                is DispenseMedicationVersionType.FreeText102 -> V_1_0_2

                is DispenseMedicationVersionType.Pzn110,
                is DispenseMedicationVersionType.Compounding110,
                is DispenseMedicationVersionType.Ingredient110,
                is DispenseMedicationVersionType.FreeText110 -> V_1_1_0

                is DispenseMedicationVersionType.Epa14 -> V_1_4
            }
        }
    }

    sealed class DispenseMedicationVersionType(val raw: String) {
        data object Pzn102 : DispenseMedicationVersionType("1.0.2")
        data object Pzn110 : DispenseMedicationVersionType("1.1.0")
        data object Compounding102 : DispenseMedicationVersionType("1.0.2")
        data object Compounding110 : DispenseMedicationVersionType("1.1.0")
        data object Ingredient102 : DispenseMedicationVersionType("1.0.2")
        data object Ingredient110 : DispenseMedicationVersionType("1.1.0")
        data object FreeText102 : DispenseMedicationVersionType("1.0.2")
        data object FreeText110 : DispenseMedicationVersionType("1.1.0")
        data object Epa14 : DispenseMedicationVersionType("1.4")

        companion object {
            /**
             * Returns all known version types.
             */
            private val all: List<DispenseMedicationVersionType> by lazy {
                listOf(
                    Pzn102, Pzn110,
                    Compounding102, Compounding110,
                    Ingredient102, Ingredient110,
                    FreeText102, FreeText110,
                    Epa14
                )
            }

            /**
             * Get all matching enum instances by version string (e.g., `"1.0.2"`).
             *
             * @return List of matching version types (e.g., all `*102`).
             */
            internal fun String.fromRaw(): DispenseMedicationVersionType? =
                all.find { it.raw == this }
        }
    }

    enum class MedicationCategory(val url: String) {
        Version110("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"),
        Version102("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category"),
        Version14("https://gematik.de/fhir/epa-medication/StructureDefinition/drug-category-extension")
    }

    const val DIGA_REDEEM_CODE = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_RedeemCode"

    const val DIGA_DEEP_LINK = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink"

    const val DIGA_DISPENSE_TYPE = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense_DiGA"
}
