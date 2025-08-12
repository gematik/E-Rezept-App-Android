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

package de.gematik.ti.erp.app.consent.model

import de.gematik.ti.erp.app.fhir.model.json
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.profileValue
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement

private fun template(
    insuranceIdentifier: String,
    timeStamp: String
) = """
{
  "resourceType": "Consent",
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
        "code": "patient-privacy"
      }
    ]
  },
  "category": [
    {
      "coding": [
        {
          "system": "https://gematik.de/fhir/erpchrg/CodeSystem/GEM_ERPCHRG_CS_ConsentType",
          "code": "CHARGCONS"
        }
      ]
    }
  ],
  "patient": {
    "identifier": {
      "system": "http://fhir.de/sid/pkv/kvid-10",
      "value": "$insuranceIdentifier"
    }
  },
  "dateTime": "$timeStamp",
  "policyRule": {
    "coding": [
      {
        "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
        "code": "OPTIN"
      }
    ]
  }
}
""".trimIndent()

fun createConsent(
    insuranceId: String
): JsonElement {
    val templateString = template(
        insuranceIdentifier = insuranceId,
        timeStamp = Clock.System.now().asFhirTemporal().formattedString()
    )

    return json.parseToJsonElement(templateString)
}

enum class ConsentType {
    Charge
}

fun extractConsentBundle(
    bundle: JsonElement,
    save: (
        consent: List<ConsentType>
    ) -> Unit
) {
    val resources = bundle
        .findAll("entry.resource")

    val consents = resources.mapNotNull { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileValue(
                "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent",
                "1.0"
            ).invoke(
                profileString
            ) -> extractConsent(bundle)

            else -> error("unsupported profile")
        }
    }.toList()
    save(consents)
}

fun extractConsent(bundle: JsonElement) =
    bundle.findAll("entry.resource.category.coding.code").firstOrNull()?.let {
        when (it.containedString()) {
            "CHARGCONS" -> ConsentType.Charge
            else -> null
        }
    }
