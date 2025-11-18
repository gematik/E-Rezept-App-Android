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
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedEpaMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedIngredientMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedPznMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.EpaContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedFreeTextMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.IngredientContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.PznContextualData
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

internal val erp_model_1_2 by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_2.json"
    )
}

internal val erp_model_1_4_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_simple.json"
    )
}

internal val erp_model_1_4_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_complex.json"
    )
}

internal val erp_model_1_4_kombi_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_kombi_complex.json"
    )
}

internal val erp_model_1_4_multiple_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_multiple_simple.json"
    )
}

internal val erp_model_diga_deeplink by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_deeplink.json"
    )
}

internal val erp_model_diga_name_and_pzn by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_name_and_pzn.json"
    )
}

internal val erp_model_diga_no_redeem_code by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_no_redeem_code.json"
    )
}

internal val simpleMedicationDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "18", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)

internal val unknownMedicationProfileDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(
                    value = "18",
                    unit = "Stk"
                ),
                denominator = FhirQuantityErpModel(
                    value = "1",
                    unit = null
                )
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)

internal val multipleMedicationDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.099.999.99",
    patientId = "X000000000",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-000000000000000",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2025-03-31")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Ibuprofen",
            category = null,
            form = "TAB",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "20", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "12345678",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null
            )
        ),
        DispensedIngredientMedicationErpModel(
            text = "Compounded Cream",
            category = null,
            form = "Cream",
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = IngredientContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null,
                ingredients = listOf(
                    FhirMedicationIngredientErpModel(
                        text = "Clotrimazole",
                        amount = null,
                        form = null,
                        strengthRatio = null,
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = null,
                            atc = null,
                            ask = null,
                            snomed = null
                        )
                    )
                )
            )
        ),
        DispensedIngredientMedicationErpModel(
            text = "Compound X",
            category = null,
            form = null,
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = IngredientContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null,
                ingredients = listOf(
                    FhirMedicationIngredientErpModel(
                        text = null,
                        amount = null,
                        form = null,
                        strengthRatio = FhirRatioErpModel(
                            numerator = FhirQuantityErpModel(value = "2.8", unit = "mg"),
                            denominator = FhirQuantityErpModel(value = "1", unit = "Sprühstoß")
                        ),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = null,
                            atc = null,
                            ask = null,
                            snomed = null
                        )
                    )
                )
            )
        ),
        FhirDispensedFreeTextMedicationErpModel(
            text = "Freestyle migraine drops - use as directed",
            category = null,
            form = "Liquid",
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null
        )
    ),
    dispensedDeviceRequest = null
)

internal val medicationDispenseNoCategory = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(value = LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "18", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)

val erpMedicationDispenseDiGADeepLinkV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-DiGA-DeepLink",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "8-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = FhirDispenseDeviceRequestErpModel(
        deepLink = "https://gematico.de?redeemCode=DE12345678901234",
        redeemCode = "DE12345678901234",
        declineCode = null,
        modifiedDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("2025-10-01")
        ),
        note = null,
        referencePzn = "12345678",
        display = "Gematico Diabetestherapie",
        status = "completed"
    )
)

val erpMedicationDispenseDiGANameAndPznV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-DiGA-Name-And-PZN",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "8-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = FhirDispenseDeviceRequestErpModel(
        deepLink = null,
        redeemCode = "DE12345678901234",
        declineCode = null,
        modifiedDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("2025-10-01")
        ),
        note = null,
        referencePzn = "12345678",
        display = "Gematico Diabetestherapie",
        status = "completed" // Replace with enum if applicable
    )
)

val erpMedicationDispenseDiGANoRedeemCodeV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-DiGA-NoRedeemCode",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "8-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = FhirDispenseDeviceRequestErpModel(
        deepLink = null,
        redeemCode = null,
        declineCode = "asked-declined",
        modifiedDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("2025-10-01")
        ),
        note = "Freischaltcode für DiGA konnte nicht erstellt werden",
        referencePzn = null,
        display = null,
        status = "completed" // Replace with enum if applicable
    )
)

val erpMedicationDispenseKombipackungV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-Kombipackung",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = null
)

val erpMedicationDispenseRezepturV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-Rezeptur",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = null
)

val erpMedicationDispenseWithoutMedicationV15 = FhirMedicationDispenseErpModel(
    dispenseId = "Example-MedicationDispense-Without-Medication",
    patientId = "X123456789",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000095957",
    handedOver = FhirTemporal.LocalDate(
        value = LocalDate.parse("2025-10-01")
    ),
    dispensedMedication = emptyList(),
    dispensedDeviceRequest = null
)

val medicationDispenseErpModelCollectionV15SingleMedication = FhirMedicationDispenseErpModelCollection(
    dispensedMedications = listOf(
        FhirMedicationDispenseErpModel(
            dispenseId = "200.000.000.000.000.01",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = "1-0-1-0",
            performer = "3-2-APO-XanthippeVeilchenblau01",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = "Sumatriptan-1a Pharma 100 mg Tabletten",
                    category = null,
                    form = "TAB",
                    amount = null,
                    isVaccine = false,
                    lotNumber = null,
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = "N1",
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = listOf(
                            FhirMedicationIngredientErpModel(
                                text = "Sumatriptan",
                                amount = null,
                                form = null,
                                strengthRatio = FhirRatioErpModel(
                                    numerator = FhirQuantityErpModel(
                                        value = "100",
                                        unit = "mg"
                                    ),
                                    denominator = FhirQuantityErpModel(
                                        value = "1",
                                        unit = null
                                    )
                                ),
                                identifier = FhirMedicationIdentifierErpModel(
                                    pzn = "06313728",
                                    atc = null,
                                    ask = null,
                                    snomed = null
                                )
                            )
                        ),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null
        )
    )
)

val medicationDispenseErpModelCollectionV15MultipleMedications = FhirMedicationDispenseErpModelCollection(
    dispensedMedications = listOf(
        FhirMedicationDispenseErpModel(
            dispenseId = "160.000.000.000.000.01",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = null,
            performer = "3-2-APO-XanthippeVeilchenblau01",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = null,
                    category = null,
                    form = null,
                    amount = null,
                    isVaccine = false,
                    lotNumber = "123456",
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = null,
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = emptyList(),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null
        ),
        FhirMedicationDispenseErpModel(
            dispenseId = "160.000.000.000.000.02",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = null,
            performer = "3-2-APO-XanthippeVeilchenblau01",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = null,
                    category = null,
                    form = null,
                    amount = null,
                    isVaccine = false,
                    lotNumber = "123456",
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = null,
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = emptyList(),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null
        )
    )
)

val medicationDispenseErpModelCollectionEuV10Single = FhirMedicationDispenseErpModelCollection(
    dispensedMedications = listOf(
        FhirMedicationDispenseErpModel(
            dispenseId = "160.000.000.000.000.01",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = null,
            performer = "",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = "Sumatriptan-1a Pharma 100 mg Tabletten",
                    category = null,
                    form = "TAB",
                    amount = null,
                    isVaccine = false,
                    lotNumber = null,
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = "N1",
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = listOf(
                            FhirMedicationIngredientErpModel(
                                text = "Sumatriptan",
                                amount = null,
                                form = null,
                                strengthRatio = FhirRatioErpModel(
                                    numerator = FhirQuantityErpModel(
                                        value = "100",
                                        unit = "mg"
                                    ),
                                    denominator = FhirQuantityErpModel(
                                        value = "1",
                                        unit = null
                                    )
                                ),
                                identifier = FhirMedicationIdentifierErpModel(
                                    pzn = "06313728",
                                    atc = null,
                                    ask = null,
                                    snomed = null
                                )
                            )
                        ),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null,
            pharmacyName = "Pharmacia de Santa Maria", // EU pharmacy
            pharmacyAddress = FhirTaskKbvAddressErpModel(
                streetName = null,
                houseNumber = null,
                additionalAddressInformation = null,
                postalCode = "1234-567",
                city = "Lisbon"
            )
        )
    )
)

val medicationDispenseErpModelCollectionEuV10Multiple = FhirMedicationDispenseErpModelCollection(
    dispensedMedications = listOf(
        FhirMedicationDispenseErpModel(
            dispenseId = "160.000.000.000.000.01",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = null,
            performer = "",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = "Sumatriptan-1a Pharma 100 mg Tabletten",
                    category = null,
                    form = "TAB",
                    amount = null,
                    isVaccine = false,
                    lotNumber = null,
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = "N1",
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = listOf(
                            FhirMedicationIngredientErpModel(
                                text = "Sumatriptan",
                                amount = null,
                                form = null,
                                strengthRatio = FhirRatioErpModel(
                                    numerator = FhirQuantityErpModel(value = "100", unit = "mg"),
                                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                                ),
                                identifier = FhirMedicationIdentifierErpModel(
                                    pzn = "06313728",
                                    atc = null,
                                    ask = null,
                                    snomed = null
                                )
                            )
                        ),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null,
            pharmacyName = "Pharmacia de Santa Maria",
            pharmacyAddress = FhirTaskKbvAddressErpModel(
                streetName = null,
                houseNumber = null,
                additionalAddressInformation = null,
                postalCode = "1234-567",
                city = "Lisbon"
            )
        ),
        FhirMedicationDispenseErpModel(
            dispenseId = "160.000.000.000.000.02",
            patientId = "X123456789",
            substitutionAllowed = false,
            dosageInstruction = "1-0-1-0",
            performer = "",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-01")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = "Sumatriptan-1a Pharma 100 mg Tabletten",
                    category = null,
                    form = "TAB",
                    amount = null,
                    isVaccine = false,
                    lotNumber = null,
                    expirationDate = null,
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "06313728",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = "N1",
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = listOf(
                            FhirMedicationIngredientErpModel(
                                text = "Sumatriptan",
                                amount = null,
                                form = null,
                                strengthRatio = FhirRatioErpModel(
                                    numerator = FhirQuantityErpModel(value = "100", unit = "mg"),
                                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                                ),
                                identifier = FhirMedicationIdentifierErpModel(
                                    pzn = "06313728",
                                    atc = null,
                                    ask = null,
                                    snomed = null
                                )
                            )
                        ),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null,
            pharmacyName = null,
            pharmacyAddress = null
        )
    )
)

val medicationDispenseErpModelCollectionEuV10SingleUrn = FhirMedicationDispenseErpModelCollection(
    dispensedMedications = listOf(
        FhirMedicationDispenseErpModel(
            dispenseId = "200.000.003.588.257.69",
            patientId = "X110583717",
            substitutionAllowed = true,
            dosageInstruction = null,
            performer = "",
            handedOver = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-10-31")
            ),
            dispensedMedication = listOf(
                DispensedEpaMedicationErpModel(
                    text = null,
                    category = null,
                    form = "TUB",
                    amount = null,
                    isVaccine = false,
                    lotNumber = "2873620872",
                    expirationDate = FhirTemporal.Instant(
                        Instant.parse("2025-10-31T11:00:13Z")
                    ),
                    contextualData = EpaContextualData(
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "58621439",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        normSizeCode = null,
                        manufacturingInstructions = null,
                        packaging = null,
                        ingredients = emptyList(),
                        internalMedication = emptyList()
                    )
                )
            ),
            dispensedDeviceRequest = null,
            pharmacyName = "Fürsten Apotheke",
            pharmacyAddress = FhirTaskKbvAddressErpModel(
                streetName = null,
                houseNumber = null,
                additionalAddressInformation = null,
                postalCode = "612855949",
                city = "Schön Dianaland"
            )
        )
    )
)
