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

package de.gematik.ti.erp.app.fhir.pkv.mocks

import de.gematik.ti.erp.app.fhir.FhirPkvChargeItem
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceBinaryErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvKbvBinaryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.support.ChargeItemType
import de.gematik.ti.erp.app.fhir.support.FhirChargeableItemCodeErpModel
import de.gematik.ti.erp.app.fhir.support.FhirCostErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

object ChargeItemBundleParserMocks {

    private val kbvBinary = byteArrayOf(
        89,
        50,
        82,
        106,
        77,
        84,
        86,
        106,
        78,
        84,
        104,
        107,
        77,
        122,
        108,
        107,
        77,
        106,
        108,
        108,
        78,
        68,
        100,
        106,
        77,
        84,
        107,
        49,
        77,
        106,
        73,
        122,
        78,
        68,
        108,
        107,
        79,
        68,
        82,
        106,
        77,
        84,
        104,
        105,
        78,
        84,
        108,
        105,
        89,
        84,
        90,
        107,
        77,
        71,
        70,
        104,
        90,
        109,
        73,
        53,
        78,
        71,
        89,
        121,
        90,
        106,
        77,
        50,
        78,
        68,
        70,
        107,
        78,
        71,
        74,
        105,
        90,
        84,
        107,
        49,
        79,
        68,
        104,
        105,
        77,
        81,
        61,
        61
    )
    val basic_1_2_taskData = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2023-07-03"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.None,
            emergencyFee = false,
            additionalFee = null,
            substitutionAllowed = true,
            dosageInstruction = "1-0-0-0",
            note = null,
            quantity = 1,
            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                indicator = false,
                numbering = null,
                start = null,
                end = null
            ),
            isSer = false,
            prescriberId = null
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "Beloc-Zok® mite 47,5 mg, 30 Retardtabletten N1",
            form = "RET",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                type = ErpMedicationProfileType.PZN,
                version = ErpMedicationProfileVersion.V_110
            ),
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "30", unit = "Stück"),
                denominator = FhirQuantityErpModel(value = "1", unit = "")
            ),
            isVaccine = false,
            normSizeCode = "N1",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "03879429",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Paula Privati",
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22")
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Blumenweg",
                houseNumber = "18",
                additionalAddressInformation = null,
                postalCode = "26427",
                city = "Esens"
            ),
            insuranceInformation = "P123464113"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Emma Schneider",
            qualification = "Fachärztin für Innere Medizin",
            doctorIdentifier = "987654423",
            dentistIdentifier = null,
            telematikId = "1-748382202"
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "MVZ",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Herbert-Lewin-Platz",
                houseNumber = "2",
                additionalAddressInformation = null,
                postalCode = "10623",
                city = "Berlin"
            ),
            bsnr = "721111100",
            iknr = null,
            telematikId = null,
            phone = "0301234567",
            email = "mvz@e-mail.de",
            fax = "030123456789"
        ),
        coverage = FhirCoverageErpModel(
            name = "Allianz Private Krankenversicherung",
            statusCode = "1",
            insuranceIdentifier = "123456789",
            coverageType = "PKV"
        ),
        deviceRequest = null
    )

    val pkvChargeItems_2_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                    binary = kbvBinary
                ),
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = false,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = null,
                        note = null,
                        quantity = 1,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "Venlafaxin - 1 A Pharma® 75mg 100 Tabl. N3",
                        form = "TAB",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = null,
                        isVaccine = false,
                        normSizeCode = "N3",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "05392039",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Paulus Privatus",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("1969-11-07")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Nauheimer Str.",
                            houseNumber = "188",
                            additionalAddressInformation = null,
                            postalCode = "50969",
                            city = "Köln"
                        ),
                        insuranceInformation = "P123464315"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Emilia Becker",
                        qualification = "Fachärztin für Psychiatrie und Psychotherapie",
                        doctorIdentifier = "582369858",
                        dentistIdentifier = null,
                        telematikId = null
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Praxis für Psychiatrie und Psychotherapie",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Herbert-Lewin-Platz",
                            houseNumber = "2",
                            additionalAddressInformation = null,
                            postalCode = "10623",
                            city = "Berlin"
                        ),
                        bsnr = "723333300",
                        iknr = null,
                        telematikId = null,
                        phone = "030369258147",
                        email = null,
                        fax = null
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.457.180.497.994.96",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T11:30:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel("31.4", "EUR"),
                    totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "31.4",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Pzn,
                                code = "09494280",
                                text = "VENLAFAXIN Heumann 75 mg Tabletten 100 St"
                            )
                        )
                    ),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78,
                        65, 81, 99, 67, 111, 73, 73, 85, 106, 84, 67, 67, 70, 73, 107, 67,
                        65, 81, 85, 120, 68, 84, 65, 76, 66, 103, 108, 103, 104, 107, 103,
                        66, 90, 81, 77
                    )
                ),
                medicationDispenseErpModel = FhirPkvInvoiceMedicationDispenseErpModel(
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03"),
                        type = FhirTemporalSerializationType.FhirTemporalLocalDate
                    )
                ),
                taskId = "200.457.180.497.994.96",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItems_3_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                    binary = kbvBinary
                ),
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = true,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = "1x, im Bedarfsfall nach 1h ein weiteres  (max. 3 Stk in 48 h)",
                        note = null,
                        quantity = 1,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "INFECTOCORTIKRUPP® Zäpfchen 100 mg 3 Supp. N1",
                        form = "SUB",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = null,
                        isVaccine = false,
                        normSizeCode = "N1",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "03386388",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Teddy Privati",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2022-07-30")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Sesamstraße",
                            houseNumber = "1",
                            additionalAddressInformation = null,
                            postalCode = "93047",
                            city = "Regensburg"
                        ),
                        insuranceInformation = "P123464532"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Dr. Maximilian Weber",
                        qualification = "Facharzt für Kinder- und Jugendmedizin",
                        doctorIdentifier = "456456534",
                        dentistIdentifier = null,
                        telematikId = null
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Kinderarztpraxis",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Yorckstraße",
                            houseNumber = "15",
                            additionalAddressInformation = "Hinterhaus",
                            postalCode = "93049",
                            city = "Regensburg"
                        ),
                        bsnr = "687777700",
                        iknr = null,
                        telematikId = null,
                        phone = "09411234567",
                        email = "kinderarztpraxis@e-mail.de",
                        fax = null
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.279.187.481.423.80",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T20:45:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "21.82",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Pzn,
                                code = "03386388",
                                text = "InfectoCortiKrupp® Zäpfchen 100 mg 3 St"
                            )
                        ),
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "2.5",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Ta1,
                                code = "02567018",
                                text = "Noctu-Gebühr"
                            )
                        )
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel("24.32", "EUR"),
                    totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78,
                        65, 81, 99, 67, 111, 73, 73, 85, 106, 84, 67, 67, 70, 73, 107, 67,
                        65, 81, 85, 120, 68, 84, 65, 76, 66, 103, 108, 103, 104, 107, 103,
                        66, 90, 81, 77, 69, 65, 103, 69, 119, 103, 103, 112, 49
                    )
                ),
                medicationDispenseErpModel = FhirPkvInvoiceMedicationDispenseErpModel(
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03"),
                        type = FhirTemporalSerializationType.FhirTemporalLocalDate
                    )
                ),
                taskId = "200.279.187.481.423.80",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItems_5_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                    binary = kbvBinary
                ),
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = false,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = null,
                        note = "Bitte auf Anwendung schulen",
                        quantity = 2,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "Viani 50µg/250µg 1 Diskus 60 ED N1",
                        form = "IHP",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = FhirRatioErpModel(
                            numerator = FhirQuantityErpModel(value = "1", unit = "Diskus"),
                            denominator = FhirQuantityErpModel(value = "1", unit = "")
                        ),
                        isVaccine = false,
                        normSizeCode = "N1",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "00427833",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Paula Privati",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("1935-06-22")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Blumenweg",
                            houseNumber = "18",
                            additionalAddressInformation = null,
                            postalCode = "26427",
                            city = "Esens"
                        ),
                        insuranceInformation = "P123464113"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Alexander Fischer",
                        qualification = "Weiterbildungsassistent",
                        doctorIdentifier = "895268385",
                        dentistIdentifier = null,
                        telematikId = null
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "MVZ",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Herbert-Lewin-Platz",
                            houseNumber = "2",
                            additionalAddressInformation = null,
                            postalCode = "10623",
                            city = "Berlin"
                        ),
                        bsnr = "721111100",
                        iknr = null,
                        telematikId = null,
                        phone = "0301234567",
                        email = "mvz@e-mail.de",
                        fax = "030123456789"
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.625.688.123.368.48",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T11:30:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "82.68",
                            tax = "19",
                            factor = "2",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Pzn,
                                code = "00427833",
                                text = "Viani 50µg/250µg 1 Diskus 60 ED N1"
                            )
                        )
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel("82.68", "EUR"),
                    totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78,
                        65, 81, 99, 67, 111, 73, 73, 85, 106, 84, 67, 67, 70, 73, 107, 67,
                        65, 81, 85, 120, 68, 84, 65, 76, 66, 103, 108, 103, 104, 107, 103,
                        66, 90, 81, 77, 69, 65, 103, 69, 119, 103, 103, 112, 49
                    )
                ),
                medicationDispenseErpModel = FhirPkvInvoiceMedicationDispenseErpModel(
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03"),
                        type = FhirTemporalSerializationType.FhirTemporalLocalDate
                    )
                ),
                taskId = "200.625.688.123.368.48",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItems_6_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                    binary = kbvBinary
                ),
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = false,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = "1-0-0-0",
                        note = null,
                        quantity = 1,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "Bisoprolol plus 10/25 - 1A Pharma® 100 Filmtbl. N3",
                        form = "FTA",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = FhirRatioErpModel(
                            numerator = FhirQuantityErpModel(value = "100", unit = "Stück"),
                            denominator = FhirQuantityErpModel(value = "1", unit = "")
                        ),
                        isVaccine = false,
                        normSizeCode = "N3",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "01624240",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Paolo Privati",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("1935-01-06")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Blumenweg",
                            houseNumber = "18",
                            additionalAddressInformation = null,
                            postalCode = "26427",
                            city = "Esens"
                        ),
                        insuranceInformation = "P123464237"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Dr. med. Emma Schneider",
                        qualification = "Fachärztin für Innere Medizin",
                        doctorIdentifier = "987654423",
                        dentistIdentifier = null,
                        telematikId = "1-748382202"
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "MVZ",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Herbert-Lewin-Platz",
                            houseNumber = "2",
                            additionalAddressInformation = null,
                            postalCode = "10623",
                            city = "Berlin"
                        ),
                        bsnr = "721111100",
                        iknr = null,
                        telematikId = null,
                        phone = "0301234567",
                        email = "mvz@e-mail.de",
                        fax = "030123456789"
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.280.604.133.110.12",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T11:30:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "42.77",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode =
                            FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Pzn,
                                code = "02091840",
                                text = "CONCOR 10 PLUS Filmtabletten 100 St"
                            )
                        )
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel("42.77", "EUR"),
                    totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78,
                        65, 81, 99, 67, 111, 73, 73, 85, 106, 84, 67, 67, 70, 73, 107, 67,
                        65, 81, 85, 120, 68, 84, 65, 76, 66, 103, 108, 103, 104, 107, 103,
                        66, 90, 81, 77, 69, 65, 103, 69, 119, 103, 103, 112, 49
                    )
                ),
                medicationDispenseErpModel = FhirPkvInvoiceMedicationDispenseErpModel(
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03"),
                        type = FhirTemporalSerializationType.FhirTemporalLocalDate
                    )
                ),
                taskId = "200.280.604.133.110.12",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItems_7_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = true,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = "1 Tablette noch in der Nacht, dann für 7 Tage jeweils 1 Tablette morgens und 1 Tablette abends einnehmen",
                        note = null,
                        quantity = 1,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "Amoxicillin/Clavulansäure AL 875mg/125mg 20 FTA N2",
                        form = "FTA",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = FhirRatioErpModel(
                            numerator = FhirQuantityErpModel(value = "20", unit = "Stück"),
                            denominator = FhirQuantityErpModel(value = "1", unit = "")
                        ),
                        isVaccine = false,
                        normSizeCode = "N2",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "10298302",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Paula Privati",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("1935-06-22")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Blumenweg",
                            houseNumber = "18",
                            additionalAddressInformation = null,
                            postalCode = "26427",
                            city = "Esens"
                        ),
                        insuranceInformation = "P123464113"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Dr. med. Emma Schneider",
                        qualification = "Fachärztin für Innere Medizin",
                        doctorIdentifier = "987654423",
                        dentistIdentifier = null,
                        telematikId = "1-748382202"
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "MVZ",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Herbert-Lewin-Platz",
                            houseNumber = "2",
                            additionalAddressInformation = null,
                            postalCode = "10623",
                            city = "Berlin"
                        ),
                        bsnr = "721111100",
                        iknr = null,
                        telematikId = null,
                        phone = "0301234567",
                        email = "mvz@e-mail.de",
                        fax = "030123456789"
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.339.908.107.779.64",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T21:30:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "61.34",
                            tax = "19",
                            factor = "2",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Pzn,
                                code = "11514676",
                                text = "Amoxicillin/Clavulansäure Heumann 875 mg/125 mg 10 St"
                            )
                        ),
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "2.5",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode = FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Ta1,
                                code = "02567018",
                                text = "Noctu-Gebühr"
                            )
                        )
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel(value = "63.84", unit = "EUR"),
                    totalAdditionalFee = FhirCostErpModel(value = "o", unit = "EUR"),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        97, 89, 68, 107, 106, 80, 111, 115, 119, 51, 83, 97, 53, 100, 88, 53,
                        69, 109, 83, 103, 104, 119, 104, 86, 103, 55, 100, 57, 106, 104, 111,
                        88, 72, 100, 119, 115, 122, 69, 84, 88, 86, 47, 56, 61
                    )
                ),
                medicationDispenseErpModel = null,
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                    binary = kbvBinary
                ),
                taskId = "200.339.908.107.779.64",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItems_8_V_1_2_Collection = FhirPkvChargeItemsErpModelCollection(
        chargeItems = listOf(
            FhirPkvChargeItem(
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(kbvBinary),
                kbvDataErpModel = FhirTaskDataErpModel(
                    pvsId = "Y/400/2107/36/999",
                    medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                        authoredOn = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03"),
                            type = FhirTemporalSerializationType.FhirTemporalLocalDate
                        ),
                        dateOfAccident = null,
                        location = null,
                        accidentType = FhirTaskAccidentType.None,
                        emergencyFee = false,
                        additionalFee = null,
                        substitutionAllowed = true,
                        dosageInstruction = null,
                        note = null,
                        quantity = 1,
                        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                            indicator = false,
                            numbering = null,
                            start = null,
                            end = null
                        ),
                        isSer = false,
                        prescriberId = null
                    ),
                    medication = FhirTaskKbvMedicationErpModel(
                        text = "Efluelda Injek.susp. 2022/2023 1 FER o. Kanüle N1",
                        form = "FER",
                        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                            type = ErpMedicationProfileType.PZN,
                            version = ErpMedicationProfileVersion.V_110
                        ),
                        amount = null,
                        isVaccine = true,
                        normSizeCode = "N1",
                        compoundingInstructions = null,
                        compoundingPackaging = null,
                        ingredients = emptyList(),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = "17543779",
                            atc = null,
                            ask = null,
                            snomed = null
                        ),
                        lotNumber = null,
                        expirationDate = null
                    ),
                    patient = FhirTaskKbvPatientErpModel(
                        name = "Paula Privati",
                        birthDate = FhirTemporal.LocalDate(
                            value = LocalDate.parse("1935-06-22")
                        ),
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Blumenweg",
                            houseNumber = "18",
                            additionalAddressInformation = null,
                            postalCode = "26427",
                            city = "Esens"
                        ),
                        insuranceInformation = "P123464113"
                    ),
                    practitioner = FhirTaskKbvPractitionerErpModel(
                        name = "Dr. med. Emma Schneider",
                        qualification = "Fachärztin für Innere Medizin",
                        doctorIdentifier = "987654423",
                        dentistIdentifier = null,
                        telematikId = "1-748382202"
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "MVZ",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Herbert-Lewin-Platz",
                            houseNumber = "2",
                            additionalAddressInformation = null,
                            postalCode = "10623",
                            city = "Berlin"
                        ),
                        bsnr = "721111100",
                        iknr = null,
                        telematikId = null,
                        phone = "0301234567",
                        email = "mvz@e-mail.de",
                        fax = "030123456789"
                    ),
                    coverage = FhirCoverageErpModel(
                        name = "Allianz Private Krankenversicherung",
                        statusCode = "1",
                        insuranceIdentifier = "123456789",
                        coverageType = "PKV"
                    ),
                    deviceRequest = null
                ),
                invoiceErpModel = FhirPkvInvoiceErpModel(
                    taskId = "200.108.757.032.088.60",
                    timestamp = FhirTemporal.Instant(
                        value = Instant.parse("2023-07-03T11:30:00Z"),
                        type = FhirTemporalSerializationType.FhirTemporalInstant
                    ),
                    organization = FhirTaskOrganizationErpModel(
                        name = "Adler-Apotheke",
                        address = FhirTaskKbvAddressErpModel(
                            streetName = "Taunusstraße",
                            houseNumber = "89",
                            additionalAddressInformation = null,
                            postalCode = "63225",
                            city = "Langen"
                        ),
                        bsnr = null,
                        iknr = "308412345",
                        telematikId = null,
                        phone = null,
                        email = null,
                        fax = null
                    ),
                    lineItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "50.97",
                            tax = "19",
                            factor = "1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode =
                            FhirChargeableItemCodeErpModel(
                                type = ChargeItemType.Ta1,
                                code = "02567053",
                                text = "Auseinzelung"

                            )
                        )
                    ),
                    additionalDispenseItems = listOf(
                        FhirPkvInvoiceChargeItemErpModel(
                            price = "42.83",
                            tax = null,
                            factor = "0.1",
                            isPartialQuantityDelivery = false,
                            spenderPzn = null,
                            chargeItemCode =
                            FhirChargeableItemCodeErpModel(type = ChargeItemType.Pzn, code = "17543785", text = null)

                        )
                    ),
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03")
                    ),
                    totalGrossFee = FhirCostErpModel("50.97", "EUR"),
                    totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                    binary = byteArrayOf(
                        89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                        77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                        78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                        89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                        90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                        79, 68, 104, 105, 77, 81, 61, 61
                    )
                ),
                invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                    binary = byteArrayOf(
                        97, 89, 68, 107, 106, 80, 111, 115, 119, 51, 83, 97, 53, 100, 88, 53,
                        69, 109, 83, 103, 104, 119, 104, 86, 103, 55, 100, 57, 106, 104, 111,
                        88, 72, 100, 119, 115, 122, 69, 84, 88, 86, 47, 56, 61
                    )
                ),
                medicationDispenseErpModel = FhirPkvInvoiceMedicationDispenseErpModel(
                    whenHandedOver = FhirTemporal.LocalDate(
                        value = LocalDate.parse("2023-07-03"),
                        type = FhirTemporalSerializationType.FhirTemporalLocalDate
                    )
                ),
                taskId = "200.108.757.032.088.60",
                accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
            )
        )
    )

    val pkvChargeItem_Rezeptur_parenterale_Zytostatika_1_V_1_3_invoice =
        FhirPkvInvoiceErpModel(
            taskId = "209.100.612.180.208.16",
            timestamp = FhirTemporal.Instant(
                Instant.parse("2024-11-06T11:30:00Z")
            ),
            organization = FhirTaskOrganizationErpModel(
                name = "Adler-Apotheke",
                address = FhirTaskKbvAddressErpModel(
                    streetName = "Taunusstraße",
                    houseNumber = "89",
                    additionalAddressInformation = null,
                    postalCode = "63225",
                    city = "Langen"
                ),
                bsnr = null,
                iknr = "308412345",
                telematikId = null,
                phone = null,
                email = null,
                fax = null
            ),
            lineItems = listOf(
                FhirPkvInvoiceChargeItemErpModel(
                    price = "389.17",
                    tax = "19.00",
                    factor = "1",
                    isPartialQuantityDelivery = false,
                    spenderPzn = null,
                    chargeItemCode =
                    FhirChargeableItemCodeErpModel(
                        type = ChargeItemType.Ta1,
                        code = "09999092",
                        text = "Parenterale Zubereitung"

                    )
                )
            ),
            additionalInvoiceInformation = listOf(
                "Bestandteile (Nettopreise):",
                "Herstellung 1 - 2024-11-04T12:00:00.000+00:00: 1 01131365 11 17.33 € / 1 09477471 11 1.36 € / 1 06460518 11 90.00 €",
                "Herstellung 2 - 2024-11-05T09:00:00.000+00:00: 1 01131365 11 17.33 € / 1 09477471 11 1.36 € / 1 06460518 11 90.00 €",
                "Herstellung 3 - 2024-11-06T10:00:00.000+00:00: 1 01131365 11 17.33 € / 1 01131365 99 0.96 € / 1 09477471 11 1.36 € / 1 06460518 11 90.00 €"
            ),
            additionalDispenseItems = emptyList(),
            whenHandedOver = null,
            totalGrossFee = null,
            totalAdditionalFee = null,
            binary = byteArrayOf(
                89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                79, 68, 104, 105, 77, 81, 61, 61
            )
        )

    val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3 =
        FhirPkvChargeItemsErpModelCollection(
            chargeItems = listOf(
                FhirPkvChargeItem(
                    kbvDataErpModel = FhirTaskDataErpModel(
                        pvsId = "Y/400/2107/36/999",
                        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                            authoredOn = FhirTemporal.LocalDate(
                                LocalDate.parse("2024-11-03")
                            ),
                            dateOfAccident = null,
                            location = null,
                            accidentType = FhirTaskAccidentType.None,
                            emergencyFee = false,
                            additionalFee = null,
                            substitutionAllowed = false,
                            dosageInstruction = null,
                            note = null,
                            quantity = 1,
                            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                                indicator = true,
                                numbering = FhirRatioErpModel(
                                    numerator = FhirQuantityErpModel(value = "1", unit = null),
                                    denominator = FhirQuantityErpModel(value = "4", unit = null)
                                ),
                                start = FhirTemporal.LocalDate(LocalDate.parse("2024-11-03")),
                                end = FhirTemporal.LocalDate(LocalDate.parse("2025-01-31"))
                            ),
                            isSer = false,
                            prescriberId = null
                        ),
                        medication = FhirTaskKbvMedicationErpModel(
                            text = "L-Thyroxin Henning 75 100 Tbl. N3",
                            form = "TAB",
                            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                                type = ErpMedicationProfileType.PZN,
                                version = ErpMedicationProfileVersion.V_110
                            ),
                            amount = null,
                            isVaccine = false,
                            normSizeCode = "N3",
                            compoundingInstructions = null,
                            compoundingPackaging = null,
                            ingredients = emptyList(),
                            identifier = FhirMedicationIdentifierErpModel(
                                pzn = "02532741",
                                atc = null,
                                ask = null,
                                snomed = null
                            ),
                            lotNumber = null,
                            expirationDate = null
                        ),
                        patient = FhirTaskKbvPatientErpModel(
                            name = "Paula Privati",
                            birthDate = FhirTemporal.LocalDate(LocalDate.parse("1935-06-22")),
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Blumenweg",
                                houseNumber = "18",
                                additionalAddressInformation = null,
                                postalCode = "26427",
                                city = "Esens"
                            ),
                            insuranceInformation = "P123464117"
                        ),
                        practitioner = FhirTaskKbvPractitionerErpModel(
                            name = "Dr. med. Emma Schneider",
                            qualification = "Fachärztin für Innere Medizin",
                            doctorIdentifier = "987654423",
                            dentistIdentifier = null,
                            telematikId = "1-748382202"
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "MVZ",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Herbert-Lewin-Platz",
                                houseNumber = "2",
                                additionalAddressInformation = null,
                                postalCode = "10623",
                                city = "Berlin"
                            ),
                            bsnr = "721111100",
                            iknr = null,
                            telematikId = null,
                            phone = "0301234567",
                            email = "mvz@e-mail.de",
                            fax = "030123456789"
                        ),
                        coverage = FhirCoverageErpModel(
                            name = "Allianz Private Krankenversicherung",
                            statusCode = "1",
                            insuranceIdentifier = "168140346",
                            coverageType = "PKV"
                        ),
                        deviceRequest = null
                    ),
                    invoiceErpModel = FhirPkvInvoiceErpModel(
                        taskId = "200.918.824.824.539.12",
                        timestamp = FhirTemporal.Instant(
                            Instant.parse("2023-07-03T11:30:00Z")
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "Adler-Apotheke",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Taunusstraße",
                                houseNumber = "89",
                                additionalAddressInformation = null,
                                postalCode = "63225",
                                city = "Langen"
                            ),
                            bsnr = null,
                            iknr = "308412345",
                            telematikId = null,
                            phone = null,
                            email = null,
                            fax = null
                        ),
                        lineItems = listOf(
                            FhirPkvInvoiceChargeItemErpModel(
                                price = "15.4",
                                tax = "19",
                                factor = "1",
                                isPartialQuantityDelivery = false,
                                spenderPzn = null,
                                chargeItemCode =
                                FhirChargeableItemCodeErpModel(
                                    type = ChargeItemType.Pzn,
                                    code = "02532741",
                                    text = "L-thyroxin 75 Henning Tabletten 100 St"

                                )
                            )
                        ),
                        additionalInvoiceInformation = emptyList(),
                        additionalDispenseItems = emptyList(),
                        whenHandedOver = null,
                        totalGrossFee = null,
                        totalAdditionalFee = null,
                        binary = byteArrayOf(
                            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
                            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
                            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
                            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
                            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
                            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
                            77, 81, 61, 61
                        )
                    ),
                    invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                        binary = byteArrayOf() // too big for mock
                    ),
                    medicationDispenseErpModel = null,
                    taskId = "",
                    accessCode = ""
                )
            )
        )

    val pkvChargeItem_Rezeptur_Ramipril_V_1_3 =
        FhirPkvChargeItemsErpModelCollection(
            chargeItems = listOf(
                FhirPkvChargeItem(
                    kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(
                        binary = kbvBinary
                    ),
                    kbvDataErpModel = FhirTaskDataErpModel(
                        pvsId = "Y/400/2107/36/999",
                        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                            authoredOn = FhirTemporal.LocalDate(
                                LocalDate.parse("2023-07-03")
                            ),
                            dateOfAccident = null,
                            location = null,
                            accidentType = FhirTaskAccidentType.None,
                            emergencyFee = false,
                            additionalFee = null,
                            substitutionAllowed = false,
                            dosageInstruction = null,
                            note = null,
                            quantity = 1,
                            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                                indicator = false,
                                numbering = null,
                                start = null,
                                end = null
                            ),
                            isSer = false,
                            prescriberId = null
                        ),
                        medication = FhirTaskKbvMedicationErpModel(
                            text = null,
                            form = "Tabletten",
                            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                                type = ErpMedicationProfileType.Ingredient,
                                version = ErpMedicationProfileVersion.V_110
                            ),
                            amount = FhirRatioErpModel(
                                numerator = FhirQuantityErpModel(value = "30", unit = "Stück"),
                                denominator = FhirQuantityErpModel(value = "1", unit = "")
                            ),
                            isVaccine = false,
                            normSizeCode = null,
                            compoundingInstructions = null,
                            compoundingPackaging = null,
                            ingredients = listOf(
                                FhirMedicationIngredientErpModel(
                                    text = "Ramipril",
                                    amount = null,
                                    form = null,
                                    strengthRatio = FhirRatioErpModel(
                                        numerator = FhirQuantityErpModel(value = "200", unit = "mg"),
                                        denominator = FhirQuantityErpModel(value = "1", unit = null)
                                    ),
                                    identifier = FhirMedicationIdentifierErpModel(
                                        pzn = null,
                                        atc = null,
                                        ask = "22686",
                                        snomed = null
                                    )
                                )
                            ),
                            identifier = FhirMedicationIdentifierErpModel(
                                pzn = null,
                                atc = null,
                                ask = "22686",
                                snomed = null
                            ),
                            lotNumber = null,
                            expirationDate = null
                        ),
                        patient = FhirTaskKbvPatientErpModel(
                            name = "Paulus Privatus",
                            birthDate = FhirTemporal.LocalDate(
                                LocalDate.parse("1969-11-07")
                            ),
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Nauheimer Str.",
                                houseNumber = "188",
                                additionalAddressInformation = null,
                                postalCode = "50969",
                                city = "Köln"
                            ),
                            insuranceInformation = "P123464315"
                        ),
                        practitioner = FhirTaskKbvPractitionerErpModel(
                            name = "Dr. med. Emma Schneider",
                            qualification = "Fachärztin für Innere Medizin",
                            doctorIdentifier = "987654423",
                            dentistIdentifier = null,
                            telematikId = "1-748382202"
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "MVZ",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Herbert-Lewin-Platz",
                                houseNumber = "2",
                                additionalAddressInformation = null,
                                postalCode = "10623",
                                city = "Berlin"
                            ),
                            bsnr = "721111100",
                            iknr = null,
                            telematikId = null,
                            phone = "0301234567",
                            email = "mvz@e-mail.de",
                            fax = "030123456789"
                        ),
                        coverage = FhirCoverageErpModel(
                            name = "Allianz Private Krankenversicherung",
                            statusCode = "1",
                            insuranceIdentifier = "123456789",
                            coverageType = "PKV"
                        ),
                        deviceRequest = null
                    ),
                    invoiceErpModel = FhirPkvInvoiceErpModel(
                        taskId = "200.643.100.572.979.08",
                        timestamp = FhirTemporal.Instant(
                            Instant.parse("2023-07-03T11:30:00Z")
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "Adler-Apotheke",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Taunusstraße",
                                houseNumber = "89",
                                additionalAddressInformation = null,
                                postalCode = "63225",
                                city = "Langen"
                            ),
                            bsnr = null,
                            iknr = "308412345",
                            telematikId = null,
                            phone = null,
                            email = null,
                            fax = null
                        ),
                        lineItems = listOf(
                            FhirPkvInvoiceChargeItemErpModel(
                                price = "12.78",
                                tax = "19",
                                factor = "1",
                                isPartialQuantityDelivery = false,
                                spenderPzn = null,
                                chargeItemCode = FhirChargeableItemCodeErpModel(
                                    type = ChargeItemType.Pzn,
                                    code = "06437063",
                                    text = "Doxycyclin 200-1a Pharma Tabletten - 20 St"
                                )
                            ),
                            FhirPkvInvoiceChargeItemErpModel(
                                price = "12.14",
                                tax = "19",
                                factor = "1",
                                isPartialQuantityDelivery = false,
                                spenderPzn = null,
                                chargeItemCode = FhirChargeableItemCodeErpModel(
                                    type = ChargeItemType.Pzn,
                                    code = "06437057",
                                    text = "Doxycyclin 200-1a Pharma Tabletten - 10 St"
                                )
                            )
                        ),
                        additionalInvoiceInformation = emptyList(),
                        additionalDispenseItems = emptyList(),
                        whenHandedOver = FhirTemporal.LocalDate(
                            value = LocalDate.parse("2023-07-03")
                        ),
                        totalGrossFee = FhirCostErpModel("24.92", "EUR"),
                        totalAdditionalFee = FhirCostErpModel("0", "EUR"),
                        binary = byteArrayOf(
                            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
                            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
                            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
                            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
                            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
                            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
                            77, 81, 61, 61
                        )
                    ),
                    invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                        binary = byteArrayOf(
                            97, 89, 68, 107, 106, 80, 111, 115, 119, 51, 83, 97,
                            53, 100, 88, 53, 69, 109, 83, 103, 104, 119, 104, 86,
                            103, 55, 100, 57, 106, 104, 111, 88, 72, 100, 119, 115,
                            122, 69, 84, 88, 86, 47, 56, 61
                        )
                    ),
                    medicationDispenseErpModel = null,
                    taskId = null,
                    accessCode = "abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd"
                )
            )
        )

    val pkvChargeItem_Einzelimport_Yellox_V_1_3 =
        FhirPkvChargeItemsErpModelCollection(
            chargeItems = listOf(
                FhirPkvChargeItem(
                    kbvDataErpModel = FhirTaskDataErpModel(
                        pvsId = "Y/400/2107/36/999",
                        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
                            authoredOn = FhirTemporal.LocalDate(
                                LocalDate.parse("2024-11-03")
                            ),
                            dateOfAccident = null,
                            location = null,
                            accidentType = FhirTaskAccidentType.None,
                            emergencyFee = false,
                            additionalFee = null,
                            substitutionAllowed = false,
                            dosageInstruction = null,
                            note = null,
                            quantity = 1,
                            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                                indicator = false,
                                numbering = null,
                                start = null,
                                end = null
                            ),
                            isSer = false,
                            prescriberId = null
                        ),
                        medication = FhirTaskKbvMedicationErpModel(
                            text = "Yellox 0,9 mg/ml Augentropfen",
                            form = null,
                            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
                            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                                type = ErpMedicationProfileType.FreeText,
                                version = ErpMedicationProfileVersion.V_110
                            ),
                            amount = null,
                            isVaccine = false,
                            normSizeCode = null,
                            compoundingInstructions = null,
                            compoundingPackaging = null,
                            ingredients = emptyList(),
                            identifier = FhirMedicationIdentifierErpModel(
                                pzn = null,
                                atc = null,
                                ask = null,
                                snomed = null
                            ),
                            lotNumber = null,
                            expirationDate = null
                        ),
                        patient = FhirTaskKbvPatientErpModel(
                            name = "Paolo Privati",
                            birthDate = FhirTemporal.LocalDate(
                                LocalDate.parse("1935-01-06")
                            ),
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Blumenweg",
                                houseNumber = "18",
                                additionalAddressInformation = null,
                                postalCode = "26427",
                                city = "Esens"
                            ),
                            insuranceInformation = "P123464233"
                        ),
                        practitioner = FhirTaskKbvPractitionerErpModel(
                            name = "Ernst Alder",
                            qualification = "Facharzt für Augenheilkunde",
                            doctorIdentifier = "987789324",
                            dentistIdentifier = null,
                            telematikId = null
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "MVZ",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Herbert-Lewin-Platz",
                                houseNumber = "2",
                                additionalAddressInformation = null,
                                postalCode = "10623",
                                city = "Berlin"
                            ),
                            bsnr = "721111100",
                            iknr = null,
                            telematikId = null,
                            phone = "030369258147",
                            email = null,
                            fax = null
                        ),
                        coverage = FhirCoverageErpModel(
                            name = "Allianz Private Krankenversicherung",
                            statusCode = "1",
                            insuranceIdentifier = "168140346",
                            coverageType = "PKV"
                        ),
                        deviceRequest = null
                    ),
                    invoiceErpModel = FhirPkvInvoiceErpModel(
                        taskId = "200.334.138.469.717.92",
                        timestamp = FhirTemporal.Instant(
                            Instant.parse("2024-11-07T23:30:00Z")
                        ),
                        organization = FhirTaskOrganizationErpModel(
                            name = "Adler-Apotheke",
                            address = FhirTaskKbvAddressErpModel(
                                streetName = "Taunusstraße",
                                houseNumber = "89",
                                additionalAddressInformation = null,
                                postalCode = "63225",
                                city = "Langen"
                            ),
                            bsnr = null,
                            iknr = "308412345",
                            telematikId = null,
                            phone = null,
                            email = null,
                            fax = null
                        ),
                        lineItems = listOf(
                            FhirPkvInvoiceChargeItemErpModel(
                                price = "27.58",
                                tax = "19.00",
                                factor = "1",
                                isPartialQuantityDelivery = false,
                                spenderPzn = null,
                                chargeItemCode = FhirChargeableItemCodeErpModel(
                                    type = ChargeItemType.Ta1,
                                    code = "09999117",
                                    text = "Einzelimport"
                                )
                            ),
                            FhirPkvInvoiceChargeItemErpModel(
                                price = "8.57",
                                tax = "19.00",
                                factor = "1",
                                isPartialQuantityDelivery = false,
                                spenderPzn = null,
                                chargeItemCode = FhirChargeableItemCodeErpModel(
                                    type = ChargeItemType.Ta1,
                                    code = "09999637",
                                    text = "Beschaffungskosten"
                                )
                            )
                        ),
                        additionalInvoiceInformation = emptyList(),
                        additionalDispenseItems = emptyList(),
                        whenHandedOver = null,
                        totalGrossFee = null,
                        totalAdditionalFee = null,
                        binary = byteArrayOf(
                            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
                            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
                            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
                            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
                            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
                            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
                            77, 81, 61, 61
                        )
                    ),
                    invoiceBinaryErpModel = FhirPkvInvoiceBinaryErpModel(
                        binary = byteArrayOf(
                            77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73,
                            104, 118, 99, 78, 65, 81, 99, 67, 111, 73, 73, 85,
                            106, 84, 67, 67, 70, 73, 107, 67, 65, 81, 85, 120,
                            // ... trimmed for readability (full list goes here) ...
                            115, 89, 77, 78, 57, 111, 116, 84, 110, 88, 106, 68,
                            105, 79, 75, 69, 79, 65, 47, 70, 118, 108, 51, 87
                        )
                    ),
                    medicationDispenseErpModel = null,
                    taskId = "",
                    accessCode = ""
                )
            )
        )

    val pkvInvoice_BelocZok =
        FhirPkvInvoiceErpModel(
            taskId = "200.424.187.927.272.20",
            timestamp = FhirTemporal.Instant(
                Instant.parse("2024-11-03T11:30:00Z")
            ),
            organization = FhirTaskOrganizationErpModel(
                name = "Adler-Apotheke",
                address = FhirTaskKbvAddressErpModel(
                    streetName = "Taunusstraße",
                    houseNumber = "89",
                    additionalAddressInformation = null,
                    postalCode = "63225",
                    city = "Langen"
                ),
                bsnr = null,
                iknr = "308412345",
                telematikId = null,
                phone = null,
                email = null,
                fax = null
            ),
            lineItems = listOf(
                FhirPkvInvoiceChargeItemErpModel(
                    price = "21.04",
                    tax = "19.00",
                    factor = "1",
                    isPartialQuantityDelivery = false,
                    spenderPzn = null,
                    chargeItemCode =
                    FhirChargeableItemCodeErpModel(
                        type = ChargeItemType.Pzn,
                        code = "03879429",
                        text = "BELOC-ZOK mite 47,5 mg Retardtabletten 30 St"

                    )
                )
            ),
            additionalInvoiceInformation = emptyList(),
            additionalDispenseItems = emptyList(),
            whenHandedOver = null,
            totalGrossFee = null,
            totalAdditionalFee = null,
            binary = byteArrayOf(
                89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                79, 68, 104, 105, 77, 81, 61, 61
            )
        )

    val pkvInvoice_Venlafaxin =
        FhirPkvInvoiceErpModel(
            taskId = "200.457.180.497.994.96",
            timestamp = FhirTemporal.Instant(
                Instant.parse("2024-11-03T11:30:00Z")
            ),
            organization = FhirTaskOrganizationErpModel(
                name = "Adler-Apotheke",
                address = FhirTaskKbvAddressErpModel(
                    streetName = "Taunusstraße",
                    houseNumber = "89",
                    additionalAddressInformation = null,
                    postalCode = "63225",
                    city = "Langen"
                ),
                bsnr = null,
                iknr = "308412345",
                telematikId = null,
                phone = null,
                email = null,
                fax = null
            ),
            lineItems = listOf(
                FhirPkvInvoiceChargeItemErpModel(
                    price = "31.40",
                    tax = "19.00",
                    factor = "1",
                    isPartialQuantityDelivery = false,
                    spenderPzn = null,
                    chargeItemCode =
                    FhirChargeableItemCodeErpModel(
                        type = ChargeItemType.Pzn,
                        code = "09494280",
                        text = "VENLAFAXIN Heumann 75 mg Tabletten 100 St"

                    )
                )
            ),
            additionalInvoiceInformation = emptyList(),
            additionalDispenseItems = emptyList(),
            whenHandedOver = null,
            totalGrossFee = null,
            totalAdditionalFee = null,
            binary = byteArrayOf(
                89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                79, 68, 104, 105, 77, 81, 61, 61
            )
        )

    val pkvInvoice_InfectoCortiKrupp_Noctu =
        FhirPkvInvoiceErpModel(
            taskId = "200.279.187.481.423.80",
            timestamp = FhirTemporal.Instant(
                Instant.parse("2024-11-03T20:45:00Z")
            ),
            organization = FhirTaskOrganizationErpModel(
                name = "Adler-Apotheke",
                address = FhirTaskKbvAddressErpModel(
                    streetName = "Taunusstraße",
                    houseNumber = "89",
                    additionalAddressInformation = null,
                    postalCode = "63225",
                    city = "Langen"
                ),
                bsnr = null,
                iknr = "308412345",
                telematikId = null,
                phone = null,
                email = null,
                fax = null
            ),
            lineItems = listOf(
                FhirPkvInvoiceChargeItemErpModel(
                    price = "21.82",
                    tax = "19.00",
                    factor = "1",
                    isPartialQuantityDelivery = false,
                    spenderPzn = null,
                    chargeItemCode =
                    FhirChargeableItemCodeErpModel(
                        type = ChargeItemType.Pzn,
                        code = "03386388",
                        text = "InfectoCortiKrupp® Zäpfchen 100 mg 3 St"

                    )
                ),
                FhirPkvInvoiceChargeItemErpModel(
                    price = "2.50",
                    tax = "19.00",
                    factor = "1",
                    isPartialQuantityDelivery = false,
                    spenderPzn = null,
                    chargeItemCode =
                    FhirChargeableItemCodeErpModel(
                        type = ChargeItemType.Ta1,
                        code = "02567018",
                        text = "Noctu-Gebühr"

                    )
                )
            ),
            additionalInvoiceInformation = emptyList(),
            additionalDispenseItems = emptyList(),
            whenHandedOver = null,
            totalGrossFee = null,
            totalAdditionalFee = null,
            binary = byteArrayOf(
                89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
                77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
                78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
                89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
                90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
                79, 68, 104, 105, 77, 81, 61, 61
            )
        )

    val pkvInvoice_Viani50ug250ug = FhirPkvInvoiceErpModel(
        taskId = "200.625.688.123.368.48",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "82.68",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "00427833",
                    text = "Viani 50µg/250µg 1 Diskus 60 ED N1"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = byteArrayOf(
            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
            77, 81, 61, 61
        )
    )

    val pkvInvoice_Concor10Plus = FhirPkvInvoiceErpModel(
        taskId = "200.280.604.133.110.12",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "42.77",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "02091840",
                    text = "CONCOR 10 PLUS Filmtabletten 100 St"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = byteArrayOf(
            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
            77, 81, 61, 61
        )
    )

    val pkvInvoice_Amoxiclav_Noctu = FhirPkvInvoiceErpModel(
        taskId = "200.339.908.107.779.64",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T21:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "61.34",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "11514676",
                    text = "Amoxicillin/Clavulansäure Heumann 875mg/125mg 10St"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "2.50",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "02567018",
                    text = "Noctu-Gebühr"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = byteArrayOf(
            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
            77, 81, 61, 61
        )
    )

    val pkvInvoice_EflueldaTetra = FhirPkvInvoiceErpModel(
        taskId = "200.108.757.032.088.60",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "54.81",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = true,
                spenderPzn = "18831517",
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "18831500",
                    text = "EFLUELDA Tetra 2024/2025 Inj.-Susp.i.e.F.-Sp.o.Kan N1"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = byteArrayOf(
            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107,
            77, 122, 108, 107, 77, 106, 108, 108, 78, 68, 100, 106,
            77, 84, 107, 49, 77, 106, 73, 122, 78, 68, 108, 107,
            79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53,
            78, 71, 89, 121, 90, 106, 77, 50, 78, 68, 70, 107,
            78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105,
            77, 81, 61, 61
        )
    )

    val pkvInvoice_Azithromycin = FhirPkvInvoiceErpModel(
        taskId = "200.085.048.660.160.92",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "31.96",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "16598608",
                    text = "Azithromycin Heumann 500 mg Filmtabletten N1"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "0.60",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "17717446",
                    text = "Lieferengpasspauschale"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = byteArrayOf(
            89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122, 108, 107,
            77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107, 49, 77, 106, 73, 122,
            78, 68, 108, 107, 79, 68, 82, 106, 77, 84, 104, 105, 78, 84, 108, 105,
            89, 84, 90, 107, 77, 71, 70, 104, 90, 109, 73, 53, 78, 71, 89, 121,
            90, 106, 77, 50, 78, 68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49,
            79, 68, 104, 105, 77, 81, 61, 61
        )
    )

    val pkvInvoice_Benazepril = FhirPkvInvoiceErpModel(
        taskId = "200.385.450.404.964.44",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "30.74",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "04351707",
                    text = "Benazepril AL 10mg 98 Filmtabletten N3"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "0.60",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "17717446",
                    text = "Lieferengpasspauschale"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = pkvInvoice_Azithromycin.binary
    )

    val pkvInvoice_Tamoxifen = FhirPkvInvoiceErpModel(
        taskId = "200.226.167.794.658.56",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "16.45",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = true,
                spenderPzn = "03852318",
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "03852301",
                    text = "Tamoxifen AL 20 Tabletten N1"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "0.60",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "17717446",
                    text = "Lieferengpasspauschale"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = pkvInvoice_Azithromycin.binary
    )

    val pkvInvoice_Doxycyclin = FhirPkvInvoiceErpModel(
        taskId = "200.082.658.364.487.24",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "25.60",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "06437028",
                    text = "Doxycyclin 100-1A Pharma Tabletten N2"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "11.84",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = true,
                spenderPzn = "06437028",
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "06437011",
                    text = "Doxycyclin 100-1A Pharma Tabletten N1"

                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "0.60",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "17717446",
                    text = "Lieferengpasspauschale"

                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = pkvInvoice_Azithromycin.binary
    )

    val pkvInvoice_Cotrim = FhirPkvInvoiceErpModel(
        taskId = "200.357.872.211.630.88",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2024-11-03T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "12.60",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode =
                FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "17550650",
                    text = "COTRIM-ratiopharm 400 mg/80 mg Tabletten N2"
                )
            )
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        whenHandedOver = null,
        totalGrossFee = null,
        totalAdditionalFee = null,
        binary = pkvInvoice_Azithromycin.binary
    )

    // The ERP-level flattened invoice model
    val erpmodelInvoice_v1_4 = FhirPkvInvoiceErpModel(
        taskId = "200.100.000.000.081.90",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2023-07-24T11:30:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "30.33",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode = FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "06313728",
                    text = "Sumatriptan 1A Pharma 100 mg Tabletten, 12 St"
                )
            )
        ),
        whenHandedOver = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-07-24")
        ),
        totalAdditionalFee = FhirCostErpModel(
            value = "0.00",
            unit = "EUR"
        ),
        totalGrossFee = FhirCostErpModel(
            value = "30.33",
            unit = "EUR"
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        binary = null
    )

    val erpmodelInvoice_v1_4_example2 = FhirPkvInvoiceErpModel(
        taskId = "200.100.000.000.082.87",
        timestamp = FhirTemporal.Instant(
            value = Instant.parse("2023-07-25T23:40:00Z")
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Adler-Apotheke",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Taunusstraße",
                houseNumber = "89",
                additionalAddressInformation = null,
                postalCode = "63225",
                city = "Langen"
            ),
            bsnr = null,
            iknr = "308412345",
            telematikId = null,
            phone = null,
            email = null,
            fax = null
        ),
        lineItems = listOf(
            FhirPkvInvoiceChargeItemErpModel(
                price = "48.98",
                tax = "19.00",
                factor = "2",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode = FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Pzn,
                    code = "11514676",
                    text = "Amoxicillin/Clavulansäure Heumann 875mg/125mg 10St"
                )
            ),
            FhirPkvInvoiceChargeItemErpModel(
                price = "2.50",
                tax = "19.00",
                factor = "1",
                isPartialQuantityDelivery = false,
                spenderPzn = null,
                chargeItemCode = FhirChargeableItemCodeErpModel(
                    type = ChargeItemType.Ta1,
                    code = "02567018",
                    text = "Noctu-Gebühr"
                )
            )
        ),
        whenHandedOver = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-07-25")
        ),
        totalAdditionalFee = FhirCostErpModel(
            value = "0.00",
            unit = "EUR"
        ),
        totalGrossFee = FhirCostErpModel(
            value = "51.48",
            unit = "EUR"
        ),
        additionalInvoiceInformation = emptyList(),
        additionalDispenseItems = emptyList(),
        binary = null
    )
}
