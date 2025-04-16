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

package de.gematik.ti.erp.app.fhir.constant

/**
 * **FHIR Constants for GEMATIK and KBV Bundles**
 *
 * This object defines constant values used throughout the application for handling
 * FHIR (Fast Healthcare Interoperability Resources) Task and KBV-related structures.
 * It includes URLs for structure definitions, naming systems, and extension dates.
 */
object FhirConstants {
    /**
     * **FHIR Task Profile URL**
     *
     * This constant represents the **GEMATIK Task Profile** definition URL.
     * It is used to identify FHIR Task resources in the system.
     */
    const val TASK = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task"

    /**
     * **KBV Bundle Profile URL**
     *
     * This constant represents the **KBV PR ERP Bundle** definition URL.
     * It is used to identify KBV-compliant prescription bundles.
     */
    const val KBV_BUNDLE = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle"
    const val KBV_BUNDLE_DEVICE_REQUEST = "https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_Bundle"

    /**
     * **FHIR Prescription ID Naming System**
     *
     * This constant defines the **naming system** for **prescription IDs** in FHIR.
     * It is used to extract prescription-related identifiers from FHIR resources.
     */
    const val PRESCRIPTION_ID_SYSTEM = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"

    /**
     * **FHIR Access Code Naming System**
     *
     * This constant represents the **naming system** for **access codes** in FHIR.
     * It is used to identify access codes associated with prescriptions.
     */
    const val ACCESS_CODE_SYSTEM = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"

    /**
     * **Key for Date-based Values in FHIR Extensions**
     *
     * This constant represents `"valueDate"`, used in **FHIR extension elements**
     * that contain **dates** (e.g., expiry dates, acceptance dates).
     */
    const val VALUE_DATE = "valueDate"

    /**
     * **Key for Instant-based Values in FHIR Extensions**
     *
     * This constant represents `"valueInstant"`, used in **FHIR extension elements**
     * that contain **timestamp-based values** (e.g., last medication dispense).
     */
    const val VALUE_INSTANT = "valueInstant"

    const val VALUE_CODING = "valueCoding"

    /**
     * **FHIR Extension Date Types**
     *
     * This enum class defines the supported **FHIR extension date types**.
     * Each extension type contains:
     * - `url` → The **FHIR extension URL** for the date field.
     * - `valueString` → Specifies whether the extension stores a **date** or **instant** value.
     *
     * These are used to extract important lifecycle timestamps from **FHIR resources**.
     */
    enum class TaskMetaDataExtensionDates(
        val url: String,
        val valueString: String
    ) {

        /**
         * **Expiry Date Extension**
         *
         * - **FHIR Extension URL:** `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate"`
         * - **Stored as:** `"valueDate"`
         *
         * This extension represents the **prescription expiry date**.
         */
        EXPIRY_DATE_EXTENSION(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate",
            VALUE_DATE
        ),

        /**
         * **Acceptance Date Extension**
         *
         * - **FHIR Extension URL:** `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate"`
         * - **Stored as:** `"valueDate"`
         *
         * This extension represents the **date until which a prescription is valid for acceptance**.
         */
        ACCEPT_DATE_EXTENSION(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate",
            VALUE_DATE
        ),

        /**
         * **Last Medication Dispense Extension**
         *
         * - **FHIR Extension URL:** `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_LastMedicationDispense"`
         * - **Stored as:** `"valueInstant"`
         *
         * This extension represents the **timestamp of the last medication dispense event**.
         */
        LAST_MEDICATION_DISPENSE_EXTENSION(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_LastMedicationDispense",
            VALUE_INSTANT
        ),

        PRESCRIPTION_TYPE_EXTENSION(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType",
            VALUE_CODING
        );
    }

    enum class PvsIdentifier(val value: String) {
        FULL_URL("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer"),
        ITEM_KEY("system"),
        ITEM_VALUE("value");
    }

    const val PRACTITIONER_IDENTIFIER_NAME = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR"

    const val PATIENT_KVNR_NAME_103 = "http://fhir.de/NamingSystem/gkv/kvid-10"
    const val PATIENT_KVNR_CODE_SYSTEM_URL = "http://fhir.de/CodeSystem/identifier-type-de-basis"
    const val PATIENT_KVNR_CODE_PKV = "http://fhir.de/sid/pkv/kvid-10"
    const val PKV = "PKV"
    const val PATIENT_KVNR_CODE_GKV = "http://fhir.de/sid/gkv/kvid-10"

    const val ORGANIZATION_IDENTIFIER_BSNR_NAME = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"
    const val ORGANIZATION_IDENTIFIER_IKNR_NAME = "http://fhir.de/sid/arge-ik/iknr"

    const val COVERAGE_KBV_STATUS_CODE_SYSTEM = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS"

    const val TELECOM_PHONE = "phone"
    const val TELECOM_EMAIL = "email"

    const val PZN_IDENTIFIER = "http://fhir.de/CodeSystem/ifa/pzn"
    const val ATC_IDENTIFIER = "http://fhir.de/CodeSystem/bfarm/atc"
    const val ASK_IDENTIFIER = "http://fhir.de/CodeSystem/ask"
    const val SNOMED_IDENTIFIER = "http://snomed.info/sct"
}
