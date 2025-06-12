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

package de.gematik.ti.erp.app.digas.mapper

import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaTimestamps
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.TimeZone

internal fun SyncedTaskData.SyncedTask.toDigaMainScreenUiModel(): DigaMainScreenUiModel {
    val dispenseDeviceRequest = medicationDispenses.firstOrNull()?.deviceRequest

    val digaStatus = status.mapToDigaStatus(
        userActionState = deviceRequest?.userActionState,
        isDeclined = dispenseDeviceRequest?.isDeclined ?: false,
        isRedeemed = dispenseDeviceRequest?.isRedeemed ?: false,
        sentOn = deviceRequest?.sentOn?.toInstant() ?: lastModified
    )

    val canBeRedeemedAgain = digaStatus is DigaStatus.InProgress

    return DigaMainScreenUiModel(
        name = deviceRequest?.appName,
        canBeRedeemedAgain = canBeRedeemedAgain,
        lifeCycleTimestamps = DigaTimestamps(
            issuedOn = authoredOn,
            sentOn = deviceRequest?.sentOn?.toInstant(TimeZone.UTC) ?: lastModified,
            modifiedOn = medicationDispenses.firstOrNull()?.deviceRequest?.modifiedDate?.toInstant(TimeZone.UTC),
            expiresOn = expiresOn
        ),
        prescribingPerson = practitioner.name,
        deepLink = dispenseDeviceRequest?.deepLink,
        institution = organization.name,
        insuredPerson = patient.name,
        code = dispenseDeviceRequest?.redeemCode,
        declineNote = dispenseDeviceRequest?.note,
        status = digaStatus,
        isArchived = deviceRequest?.isArchived ?: false
    )
}
