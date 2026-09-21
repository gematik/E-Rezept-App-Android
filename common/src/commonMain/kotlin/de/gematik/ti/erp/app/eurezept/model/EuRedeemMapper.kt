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

package de.gematik.ti.erp.app.eurezept.model

import de.gematik.ti.erp.app.fhir.FhirEuRedeemAccessCodeResponseErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import java.util.UUID

fun FhirEuRedeemAccessCodeResponseErpModel.toEuAccessCode(
    profileId: ProfileIdentifier
) = EuAccessCodeErpModel(
    accessCode = this@toEuAccessCode.accessCode,
    countryCode = this@toEuAccessCode.countryCode,
    validUntil = this@toEuAccessCode.validUntil.value,
    createdAt = this@toEuAccessCode.createdAt.value,
    profileIdentifier = profileId
)

fun FhirEuRedeemAccessCodeResponseErpModel.toModel(
    profileId: ProfileIdentifier,
    relatedTaskIds: List<String>,
    orderId: String = UUID.randomUUID().toString()
) = EuOrderErpModel(
    orderId = orderId,
    profileId = profileId,
    countryCode = this@toModel.countryCode,
    createdAt = this@toModel.createdAt.value,
    euAccessCode = this.toEuAccessCode(profileId),
    events = emptyList(), // we do not have the events in the response, its built only at db level
    relatedTaskIds = relatedTaskIds
)
