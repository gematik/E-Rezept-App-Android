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

package de.gematik.ti.erp.app.fhir.dispense.mocks

import de.gematik.ti.erp.app.data.getResourceAsString
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatioValue
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirCodeableIngredient
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseActor
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseIdentifier
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByExtension
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByIdentifier
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByReference
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestText

/**
 * These JSON objects represent encoded FHIR models.
 *
 * They serve as **snapshots** of the actual FHIR resources received from the backend or external systems.
 * The structure reflects the serialized form of domain models defined in the application, typically using
 * `kotlinx.serialization`.
 *
 * These snapshots are useful for:
 * - Debugging and validation of incoming FHIR payloads.
 * - Ensuring backwards compatibility across FHIR version changes.
 * - Serving as reference fixtures for unit or integration tests.
 *
 * ⚠️ Note: The content and structure of these JSONs are version-specific and may evolve as
 * the FHIR specification or server implementation changes.
 */

internal val fhir_model_medication_dispense by lazy { getResourceAsString("/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy.json") }
internal val fhir_model_medication_dispense_unknown_medication_profile by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy_unknown_medication_profile.json"
    )
}
internal val fhir_model_medication_dispense_without_category by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy_without_category.json"
    )
}

internal val fhir_model_medication_dispense_compounding by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_compounding.json"
    )
}

internal val fhir_model_medication_dispense_free_text by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_free_text.json"
    )
}

internal val fhir_model_medication_dispense_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_simple.json"
    )
}

internal val fhir_model_medication_1_4_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_complex.json"
    )
}

internal val fhir_model_medication_1_4_pharma_product by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_pharma_product.json"
    )
}

internal val fhir_model_medication_1_4_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_simple.json"
    )
}

internal val fhir_model_medication_dispense_diga_deeplink by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_deeplink.json"
    )
}

internal val fhir_model_medication_dispense_diga_name_and_pzn by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_name_and_pzn.json"
    )
}

internal val fhir_model_medication_dispense_diga_no_redeem_code by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_no_redeem_code.json"
    )
}

internal val fhirMedicationDispenseV14ExampleWithoutMedication = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-Without-Medication",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.4"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "160.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "3-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2024-04-03",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByReference(
        reference = "Medication/SumatripanMedication"
    ),
    substitution = null,
    extension = emptyList(),
    note = emptyList()
)

internal val fhirMedicationDispenseDiGADeepLinkV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-DiGA-DeepLink",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense_DiGA|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "162.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "8-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/CodeSystem/ifa/pzn",
            value = "12345678",
            type = null
        ),
        display = "Gematico Diabetestherapie"
    ),
    substitution = null,
    extension = listOf(
        FhirExtension(
            url = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_RedeemCode",
            valueCoding = null,
            valueCodeableConcept = null,
            valueCode = null,
            valueString = "DE12345678901234",
            valueUrl = null,
            valueDate = null,
            valueBoolean = null,
            valueRatio = null,
            valuePeriod = null,
            valueIdentifier = null,
            extensions = emptyList()
        ),
        FhirExtension(
            url = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink",
            valueCoding = null,
            valueCodeableConcept = null,
            valueCode = null,
            valueString = null,
            valueUrl = "https://gematico.de?redeemCode=DE12345678901234",
            valueDate = null,
            valueBoolean = null,
            valueRatio = null,
            valuePeriod = null,
            valueIdentifier = null,
            extensions = emptyList()
        )
    ),
    note = emptyList()
)

internal val fhirMedicationDispenseDiGANameAndPznV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-DiGA-Name-And-PZN",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense_DiGA|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "162.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "8-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/CodeSystem/ifa/pzn",
            value = "12345678",
            type = null
        ),
        display = "Gematico Diabetestherapie"
    ),
    substitution = null,
    extension = listOf(
        FhirExtension(
            url = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_RedeemCode",
            valueCoding = null,
            valueCodeableConcept = null,
            valueCode = null,
            valueString = "DE12345678901234",
            valueUrl = null,
            valueDate = null,
            valueBoolean = null,
            valueRatio = null,
            valuePeriod = null,
            valueIdentifier = null,
            extensions = emptyList()
        )
    ),
    note = emptyList()
)

internal val fhirMedicationDispenseDiGANoRedeemCodeV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-DiGA-NoRedeemCode",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense_DiGA|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "162.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "8-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByExtension(
        extension = listOf(
            FhirExtension(
                url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = "asked-declined",
                valueString = null,
                valueUrl = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        )
    ),
    substitution = null,
    extension = emptyList(),
    note = listOf(
        FhirMedicationRequestText(
            text = "Freischaltcode für DiGA konnte nicht erstellt werden"
        )
    )
)

internal val fhirMedicationDispenseKombipackungV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-Kombipackung",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "160.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "3-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByReference(
        reference = "Medication/Medication-Kombipackung"
    ),
    substitution = null,
    extension = emptyList(),
    note = emptyList()
)

internal val fhirMedicationDispenseRezepturV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-Rezeptur",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "160.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed",
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "3-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01",
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByReference(
        reference = "Medication/Medication-Rezeptur"
    ),
    substitution = null,
    extension = emptyList(),
    note = emptyList()
)

internal val fhirMedicationDispenseWithoutMedicationV15 = FhirMedicationDispenseV14V15DispenseModel(
    id = "Example-MedicationDispense-Without-Medication",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.5"
        )
    ),
    identifier = listOf(
        FhirIdentifier(
            system = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId",
            value = "160.000.033.491.280.78",
            type = null
        )
    ),
    status = "completed", // Replace with enum if applicable
    subject = FhirMedicationDispenseIdentifier(
        identifier = FhirIdentifier(
            system = "http://fhir.de/sid/gkv/kvid-10",
            value = "X123456789",
            type = null
        )
    ),
    performer = listOf(
        FhirMedicationDispenseActor(
            actor = FhirMedicationDispenseIdentifier(
                identifier = FhirIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = "3-SMC-B-Testkarte-883110000095957",
                    type = null
                )
            )
        )
    ),
    whenHandedOver = "2025-10-01", // Or LocalDate.parse("2025-10-01")
    dosageInstruction = emptyList(),
    whenPrepared = null,
    medicationReference = MedicationReferenceByReference(
        reference = "Medication/SumatripanMedication"
    ),
    substitution = null,
    extension = emptyList(),
    note = emptyList()
)

internal val fhirMedicationDispenseMedicationV15WithoutStrengthNumerator = FhirMedicationDispenseMedicationModel(
    resourceType = "Medication",
    id = "Medication-Without-Strength-Numerator",
    meta = FhirMeta(
        profiles = listOf(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Medication|1.5"
        )
    ),
    extensions = emptyList(),
    code = FhirCodeableConcept(
        coding = emptyList(),
        text = "Infusion bestehend aus 85mg Doxorubicin aufgeloest zur Verabreichung in 250ml 5-%iger (50 mg/ml) Glucose-Infusionsloesung"
    ),
    itemCodeableConcept = null,
    form = FhirCodeableConcept(
        coding = listOf(
            FhirCoding(
                coding = emptyList(),
                system = "http://standardterms.edqm.eu",
                code = "11210000",
                version = null,
                display = "Solution for infusion"
            )
        ),
        text = null
    ),
    amount = null,
    ingredients = listOf(
        FhirCodeableIngredient(
            itemCodeableConcept = FhirCodeableConcept(
                coding = listOf(
                    FhirCoding(
                        coding = emptyList(),
                        system = "http://fhir.de/CodeSystem/bfarm/atc",
                        code = "L01DB01",
                        version = null,
                        display = "Doxorubicin"
                    )
                ),
                text = null
            ),
            strength = FhirRatio(
                extensions = emptyList(),
                numerator = FhirRatioValue(
                    value = null,
                    unit = null
                ),
                denominator = FhirRatioValue(
                    value = "1",
                    unit = null
                )
            )
        )
    ),
    batch = null,
    medications = emptyList()
)
