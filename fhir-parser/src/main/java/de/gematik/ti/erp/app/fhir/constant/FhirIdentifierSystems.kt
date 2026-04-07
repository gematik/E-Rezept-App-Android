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

package de.gematik.ti.erp.app.fhir.constant

/**
 * **Centralized FHIR Identifier and Naming Systems**
 *
 * Single source of truth for all FHIR identifier and naming system URLs used across the application.
 * This prevents duplication and ensures consistency when URLs need to be updated.
 */
object FhirIdentifierSystems {

    /**
     * **Prescription and Task Identifiers**
     */
    object Prescription {
        const val PRESCRIPTION_ID = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"
        const val ACCESS_CODE = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"
    }

    /**
     * **Patient Identifiers**
     */
    object Patient {
        /** GKV (Statutory health insurance) patient identifier */
        const val KVNR_GKV = "http://fhir.de/sid/gkv/kvid-10"

        /** PKV (Private health insurance) patient identifier */
        const val KVNR_PKV = "http://fhir.de/sid/pkv/kvid-10"

        /** Legacy GKV naming system (version 1.0.3 compatibility) */
        const val KVNR_NAME_103 = "http://fhir.de/NamingSystem/gkv/kvid-10"

        /** Patient identifier type code system */
        const val CODE_SYSTEM = "http://fhir.de/CodeSystem/identifier-type-de-basis"
    }

    /**
     * **Organization and Practitioner Identifiers**
     */
    object Healthcare {
        /** Telematik ID for organizations and practitioners */
        const val TELEMATIK_ID = "https://gematik.de/fhir/sid/telematik-id"

        /** Betriebsstättennummer (BSNR) for organizations */
        const val BSNR = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"

        /** Institutionskennzeichen (IKNR) for organizations */
        const val IKNR = "http://fhir.de/sid/arge-ik/iknr"

        /** Prüfarztnummer (PVS) for practitioners */
        const val PRUEFNUMMER = "https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer"
    }

    /**
     * **Medication and Product Identifiers**
     */
    object Medication {
        /** Pharmazentralnummer (PZN) - German pharmaceutical central number */
        const val PZN = "http://fhir.de/CodeSystem/ifa/pzn"

        /** ATC (Anatomical Therapeutic Chemical) classification codes */
        const val ATC = "http://fhir.de/CodeSystem/bfarm/atc"

        /** Arzneispezialitätenkatalog (ASK) - Medicinal products catalog */
        const val ASK = "http://fhir.de/CodeSystem/ask"

        /** ABDA TA1 codes (PKV cytostatics/compounding) */
        const val ABDA_TA1 = "http://TA1.abda.de"

        /** GKV Hilfsmittelnummer (Medical aid catalog number) */
        const val GKV_HMNR = "http://fhir.de/sid/gkv/hmnr"

        /** SNOMED CT for clinical terminology */
        const val SNOMED = "http://snomed.info/sct"
    }

    /**
     * **Communication and Order Systems**
     */
    object Communication {
        const val ORDER_ID = "https://gematik.de/fhir/NamingSystem/OrderID"
    }

    /**
     * **Reference Formats**
     */
    object Reference {
        const val URN_UUID_PREFIX = "urn:uuid:"
    }

    /**
     * **Healthcare Directory Identifiers**
     */
    object Vzd {
        /** Structure definition URL for special opening times extension */
        const val SPECIAL_OPENING_TIMES_EX =
            "https://gematik.de/fhir/directory/StructureDefinition/SpecialOpeningTimesEX"

        /** Code system for physical features of a pharmacy */
        const val PHYSICAL_FEATURES_SYSTEM =
            "https://gematik.de/fhir/directory/CodeSystem/physicalFeatures"

        /** Code system for pharmacy healthcare specialty */
        const val PHARMACY_SPECIALTY_SYSTEM =
            "https://gematik.de/fhir/directory/CodeSystem/PharmacyHealthcareSpecialtyCS"

        /** Code system for general healthcare service specialty */
        const val HEALTHCARE_SERVICE_SPECIALTY_SYSTEM =
            "https://gematik.de/fhir/directory/CodeSystem/HealthcareServiceSpecialtyCS"
    }
}
