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

package de.gematik.ti.erp.app.fhir.constant.pkv

import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvCodeSystems.ABDA_TA1
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvCodeSystems.GKV_HMNR
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvCodeSystems.IFA_PZN
import de.gematik.ti.erp.app.fhir.support.ChargeItemType

internal object FhirPkvEntryConstants {
    const val FHIR_PKV_PROFILE_TAG =
        "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_ChargeItem"
    const val FHIR_PKV_KVID = "http://fhir.de/sid/gkv/kvid-10"
}

@Suppress("unused")
internal object FhirPkvConstants {

    const val GEM_ERP_CHARGE_ITEM_BUNDLE_URL = "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_ChargeItem"
    const val DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle"
    const val DAV_PR_ERP_INVOICE_BUNDLE_V_1_5_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PR-ERP-AbgabedatenBundle"
    const val GEM_ERP_KBV_PR_BUNDLE_URL = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle"
    const val GEM_ERP_INVOICE_BINARY_BUNDLE_URL = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Bundle"
    const val GEM_ERP_PKV_PR_MEDICATION_DISPENSE_URL = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense"
}

@Suppress("unused")
internal object FhirPkvInvoiceBundleConstats {
    const val INVOICE_SUPPLEMENTARY_UNIT_PROFILE_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenEinheit"
    const val INVOICE_LINE_ITEMS_PROFILE_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen"
    const val DISPENSE_COMPOUNDING_DETAILS_PROFILE_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenHerstellung"

    const val DispenseInformationProfileUrl = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abgabeinformationen"
    val DISPENSE_INFORMATION_PROFILE_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-ZusatzdatenEinheit"
    val MANUFACTURING_STEP_COUNTER_EXTENSION_URL = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zaehler"

    // ZusatzdatenFaktorkennzeichen = factor flag on price component (e.g., surcharge/discount indicator)
    const val FACTOR_FLAG = "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-ZusatzdatenFaktorkennzeichen"
}

internal object FhirPkvChargeItemConstants {
    const val TASKID_SYSTEM_URL = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"
    const val ACCESS_CODE_SYSTEM_URL = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"
    const val KVNR_SYSTEM_URL = "http://fhir.de/sid/pkv/kvid-10"
    const val TELEMATIK_ID_SYSTEM_URL = "https://gematik.de/fhir/sid/telematik-id"
}

internal object FhirPkvCodeSystems {
    // IFA Pharmazentralnummer (German drug article number)
    const val IFA_PZN = "http://fhir.de/CodeSystem/ifa/pzn"

    // ABDA TA1 (tax/fee/service codes used in PKV cytostatics/compounding context)
    const val ABDA_TA1 = "http://TA1.abda.de"

    // GKV Hilfsmittelnummer (medical aid catalog number)
    const val GKV_HMNR = "http://fhir.de/sid/gkv/hmnr"

    val PRIORITY = listOf(IFA_PZN, ABDA_TA1, GKV_HMNR)
}

internal object FhirPkvInvoiceConstants {
    const val INVOICE_TOTAL_COPAYMENT_EXTENSION_URL =
        "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Gesamtzuzahlung"
    const val VAT_RATE_EXTENSION_URL =
        "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-MwStSatz"

    // Handle Teilmengenabgabe (partial quantity dispensing) for v1.3
    const val ADDITIONAL_ATTRIBUTES_EXTENSION_URL =
        "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zusatzattribute"
    const val ADDITIONAL_ATTR_PARTIAL_QUANTITY_NAME = "ZusatzattributTeilmengenabgabe"
    const val ADDITIONAL_ATTR_KEY_NAME = "Schluessel"

    const val SPENDER_PZN_EXTENSION_URL =
        "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zusatzattribute"
    const val SPENDER_PZN_PARTIAL_QUANTITY_NAME = "ZusatzattributTeilmengenabgabe"

    // specific key carrying donor/supplier PZN
    const val SPENDER_PZN_KEY_NAME = "Spender-PZN"

    val CHARGE_ITEM_TYPE_URL: LinkedHashMap<String, ChargeItemType> = linkedMapOf(
        IFA_PZN to ChargeItemType.Pzn,
        ABDA_TA1 to ChargeItemType.Ta1,
        GKV_HMNR to ChargeItemType.Hmnr
    )

    // TA1 Kennzeichen für "Trennung" (separation of invoice components)
    const val TA1_SEPARATION_BILLING_CODE = "02567053"

    // TA1 Kennzeichen für "Parenterale Zytostatika" (cytostatics compounding)
    const val TA1_PARENTERAL_CYTOSTATICS_CODE = "09999092"

    const val COMPONENTS_NET_PRICES_LABEL = "Bestandteile (Nettopreise):"
    const val COMPONENTS_LABEL = "Bestandteile:"
    const val NET_PRICES_SUFFIX = " (Nettopreise)"

    const val PRODUCTION_STEP_TEMPLATE = "Herstellung %d - %s: %s"
}
