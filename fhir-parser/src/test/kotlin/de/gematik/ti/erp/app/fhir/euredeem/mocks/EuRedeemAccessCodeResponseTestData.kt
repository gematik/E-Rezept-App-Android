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

package de.gematik.ti.erp.app.fhir.euredeem.mocks

import de.gematik.ti.erp.app.fhir.FhirEuRedeemAccessCodeResponseErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirParameter
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants
import de.gematik.ti.erp.app.fhir.euredeem.model.FhirEuRedeemAccessCodeResponseModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.Instant

val euRedeemAccessCodeResponse_v1_0: FhirEuRedeemAccessCodeResponseModel = FhirEuRedeemAccessCodeResponseModel(
    resourceType = FhirEuRedeemAccessCodeResponseConstants.RESOURCE_TYPE,
    id = FhirEuRedeemAccessCodeResponseConstants.ID,
    meta = FhirMeta(listOf(FhirEuRedeemAccessCodeResponseConstants.PROFILE_URL)),
    parameters = listOf(
        FhirParameter(
            name = FhirEuRedeemAccessCodeResponseConstants.CountryCodeParameter.NAME,
            valueCoding = FhirCoding(
                system = FhirEuRedeemAccessCodeRequestConstants.CountryCodeParameter.SYSTEM,
                code = "BE"
            )
        ),
        FhirParameter(
            name = FhirEuRedeemAccessCodeResponseConstants.AccessCodeParameter.NAME,
            valueIdentifier = FhirIdentifier(
                system = FhirEuRedeemAccessCodeRequestConstants.AccessCodeParameter.SYSTEM,
                value = "aBC123"
            )
        ),
        FhirParameter(
            name = FhirEuRedeemAccessCodeResponseConstants.ValidUntilParameter.NAME,
            valueInstant = "2025-10-01T16:29:00.434+00:00"
        ),
        FhirParameter(
            name = FhirEuRedeemAccessCodeResponseConstants.CreatedAtParameter.NAME,
            valueInstant = "2025-10-01T15:29:00.434+00:00"
        )
    )
)

val euRedeemAccessCodeResponseErpModel_v1_0: FhirEuRedeemAccessCodeResponseErpModel = FhirEuRedeemAccessCodeResponseErpModel(
    countryCode = "BE",
    accessCode = "aBC123",
    validUntil = FhirTemporal.Instant(Instant.parse("2025-10-01T16:29:00.434+00:00")),
    createdAt = FhirTemporal.Instant(Instant.parse("2025-10-01T15:29:00.434+00:00"))
)
