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

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuOrderEntityV1
import de.gematik.ti.erp.app.fhir.FhirEuRedeemAccessCodeResponseErpModel
import de.gematik.ti.erp.app.messages.mapper.toInternalMessage
import de.gematik.ti.erp.app.messages.mapper.toInternalMessageEntity
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.realm.kotlin.ext.toRealmList
import java.util.UUID

fun EuAccessCode.toEuAccessCodeEntityV1() =
    EuAccessCodeEntityV1().apply {
        accessCode = this@toEuAccessCodeEntityV1.accessCode
        countryCode = this@toEuAccessCodeEntityV1.countryCode
        validUntil = this@toEuAccessCodeEntityV1.validUntil.toRealmInstant()
        createdAt = this@toEuAccessCodeEntityV1.createdAt.toRealmInstant()

        profileId = this@toEuAccessCodeEntityV1.profileIdentifier
    }

fun EuAccessCodeEntityV1.toEuAccessCode() =
    EuAccessCode(
        accessCode = this@toEuAccessCode.accessCode,
        countryCode = this@toEuAccessCode.countryCode,
        validUntil = this@toEuAccessCode.validUntil.toInstant(),
        createdAt = this@toEuAccessCode.createdAt.toInstant(),

        profileIdentifier = this@toEuAccessCode.profileId
    )

fun FhirEuRedeemAccessCodeResponseErpModel.toEuAccessCode(
    profileId: ProfileIdentifier
) = EuAccessCode(
    accessCode = this@toEuAccessCode.accessCode,
    countryCode = this@toEuAccessCode.countryCode,
    validUntil = this@toEuAccessCode.validUntil.value,
    createdAt = this@toEuAccessCode.createdAt.value,
    profileIdentifier = profileId
)

fun FhirEuRedeemAccessCodeResponseErpModel.toEuOrder(
    profileId: ProfileIdentifier,
    relatedTaskIds: List<String>,
    orderId: String = UUID.randomUUID().toString()
) = EuOrder(
    orderId = orderId,
    profileId = profileId,
    countryCode = this@toEuOrder.countryCode,
    createdAt = this@toEuOrder.createdAt.value,
    euAccessCode = this.toEuAccessCode(profileId),
    messages = emptyList(),
    relatedTaskIds = relatedTaskIds
)

fun EuOrder.toEuOrderEntityV1() =
    EuOrderEntityV1().apply {
        orderId = this@toEuOrderEntityV1.orderId
        profileId = this@toEuOrderEntityV1.profileId
        countryCode = this@toEuOrderEntityV1.countryCode
        createdAt = this@toEuOrderEntityV1.createdAt.toRealmInstant()
        euAccessCode = this@toEuOrderEntityV1.euAccessCode?.toEuAccessCodeEntityV1()
        messages = this@toEuOrderEntityV1.messages.map { it.toInternalMessageEntity() }.toRealmList()
        relatedTaskIds = this@toEuOrderEntityV1.relatedTaskIds.toRealmList()
    }

fun EuOrderEntityV1.toEuOrder() =
    EuOrder(
        orderId = this@toEuOrder.orderId,
        profileId = this@toEuOrder.profileId,
        countryCode = this@toEuOrder.countryCode,
        createdAt = this@toEuOrder.createdAt.toInstant(),
        euAccessCode = this@toEuOrder.euAccessCode?.toEuAccessCode(),
        messages = this@toEuOrder.messages.map { it.toInternalMessage() },
        relatedTaskIds = this@toEuOrder.relatedTaskIds.toList()
    )
