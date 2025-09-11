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
package de.gematik.ti.erp.app.data

// charge entry
val pkvChargeItemBundleEntry by lazy { getResourceAsString("/fhir/pkv_parser/charge_item_bundle_vers_1_2.json") }
val pkvChargeItemBundle_1_4_Entry by lazy { getResourceAsString("/fhir/pkv_parser/chargeItem_ChargeItem_GET_Completed.json") }

// version 1.2
val pkvChargeItem_1_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_1.json") }
val pkvChargeItem_2_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_2.json") }
val pkvChargeItem_3_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_3.json") }
val pkvChargeItem_5_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_5.json") }
val pkvChargeItem_6_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_6.json") }
val pkvChargeItem_7_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_7.json") }
val pkvChargeItem_8_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN-Verordnung_Nr_8.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN_Mehrfachverordnung_PZN_MV_1.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN_Mehrfachverordnung_PZN_MV_2.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN_Mehrfachverordnung_PZN_MV_3.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/PZN_Mehrfachverordnung_PZN_MV_4.json") }
val pkvChargeItem_Rezeptur_parenterale_Zytostatika_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/Rezeptur-parenterale_Zytostatika.json") }
val pkvChargeItem_Rezeptur_Verordnung_Nr_1_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/Rezeptur-Verordnung_Nr_1.json") }
val pkvChargeItem_Rezeptur_Verordnung_Nr_2_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/Rezeptur-Verordnung_Nr_2.json") }
val pkvChargeItem_Wirkstoff_Verordnung_V_1_2_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/Wirkstoff-Verordnung.json") }

// version 1.3
val pkvChargeItem_1_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_1.json") }
val pkvChargeItem_2_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_2.json") }
val pkvChargeItem_3_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_3.json") }
val pkvChargeItem_5_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_5.json") }
val pkvChargeItem_6_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_6.json") }
val pkvChargeItem_7_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_7.json") }
val pkvChargeItem_8_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_8.json") }
val pkvChargeItem_14_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_14.json") }
val pkvChargeItem_15_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_15.json") }
val pkvChargeItem_16_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_16.json") }
val pkvChargeItem_17_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_17.json") }
val pkvChargeItem_18_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN-Verordnung_Nr_18.json") }
val pkvChargeItem_freitext_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/Freitext-Verordnung.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN_Mehrfachverordnung_PZN_MV_1.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN_Mehrfachverordnung_PZN_MV_2.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN_Mehrfachverordnung_PZN_MV_3.json") }
val pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_3_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_3/PZN_Mehrfachverordnung_PZN_MV_4.json") }
val pkvChargeItem_Rezeptur_parenterale_Zytostatika_Rezeptur_parenterale_Zytostatika_1_V_1_3_Bundle by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv1_3/Rezeptur-parenterale_Zytostatika_Rezeptur-parenterale_Zytostatika_1.json"
    )
}

// version 1.4
val Bundle_abgabedaten_1_4_ad80703d_8c62_44a3_b12b_2ea66eda0aa2_12164329 by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv1_4/Bundle_abgabedaten_ad80703d_8c62_44a3_b12b_2ea66eda0aa2_12164329.json"
    )
}
val Bundle_abgabedaten_1_4_f548dde3_a319_486b_8624_6176ff41ad90_4508766 by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv1_4/Bundle-f548dde3-a319-486b-8624-6176ff41ad90--4508766.json"
    )
}

// version 1.5
val Bundle_abgabendaten_1_5_72bd741c_7ad8_41d8_97c3_9aabbdd0f5b4_10483995 by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv_1_5/Bundle-72bd741c-7ad8-41d8-97c3-9aabbdd0f5b4--10483995.json"
    )
}
val Bundle_abgabendaten_1_5_edd55212_965f_4018_a287_6b08e7f5c53c_56751409 by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv_1_5/Bundle-edd55212-965f-4018-a287-6b08e7f5c53c--56751409.json"
    )
}
val Bundle_abgabendaten_1_5_fe4a04af_0828_4977_a5ce_bfeed16ebf10_58406714 by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv_1_5/Bundle-fe4a04af-0828-4977-a5ce-bfeed16ebf10--58406714.json"
    )
}

// version 1.2
val pkvChargeItem_1_V_1_2__invoice_Bundle by lazy { getResourceAsString("/fhir/pkv_parser/pkv1_2/invoice_bundle/PZN-Verordnung_Nr_1_invoice_bundle.json") }
val pkvChargeItem_1_V_1_2__invoice_binary_Bundle by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv1_2/invoice_binary_bundle/PZN-Verordnung_Nr_1_invoice_binary_bundle.json"
    )
}
val pkvChargeItem_1_V_1_2__invoice_medication_dispense_Bundle by lazy {
    getResourceAsString(
        "/fhir/pkv_parser/pkv1_2/invoice_medication_dispense/PZN-Verordnung_Nr_1_invoice_medication_dispense_bundle.json.json"
    )
}
