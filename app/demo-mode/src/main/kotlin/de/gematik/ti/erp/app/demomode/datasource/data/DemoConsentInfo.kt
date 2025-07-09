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

package de.gematik.ti.erp.app.demomode.datasource.data

object DemoConsentInfo {
    val JSON_RESPONSE_CONSENT = """{
  "id": "4af9d0b8-7d90-4606-ae3d-12a45a148ff7",
  "type": "searchset",
  "timestamp": "2023-02-06T08:55:38.043+00:00",
  "resourceType": "Bundle",
  "total": 0,
  "entry": [
    {
      "fullUrl": "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/Consent/CHARGCONS-X764228532",
      "resource": {
        "resourceType": "Consent",
        "id": "CHARGCONS-X764228532",
        "meta": {
          "profile": [
            "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent|1.0"
          ]
        },
        "status": "active",
        "scope": {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/consentscope",
              "code": "patient-privacy",
              "display": "Privacy Consent"
            }
          ]
        },
        "category": [
          {
            "coding": [
              {
                "system": "https://gematik.de/fhir/erpchrg/CodeSystem/GEM_ERPCHRG_CS_ConsentType",
                "code": "CHARGCONS",
                "display": "Consent for saving electronic charge item"
              }
            ]
          }
        ],
        "patient": {
          "identifier": {
            "system": "http://fhir.de/sid/pkv/kvid-10",
            "value": "X764228532"
          }
        },
        "dateTime": "2023-02-03T13:19:04.642+00:00",
        "policyRule": {
          "coding": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
              "code": "OPTIN"
            }
          ]
        }
      },
      "search": {
        "mode": "match"
      }
    }
  ]
}
    """.trimIndent()
}
