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

package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestEntityV1.Companion.getDigaStatusForUserAction
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.dispense.model.erp.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.RequestIntent
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.asFhirTemporal
import de.gematik.ti.erp.app.utils.toLocalDate

object ErpTaskMappers {
    fun DeviceRequestEntityV1.toErpModel() = FhirTaskKbvDeviceRequestErpModel(
        id = id,
        intent = RequestIntent.fromCode(intent),
        status = status,
        pzn = pzn,
        appName = appName,
        isSelfUse = isSelfUse,
        authoredOn = authoredOn.toInstant().asFhirTemporal(),
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.getFhirTaskAccidentByType(accidentType),
            date = accidentDate?.toInstant()?.toLocalDate()?.let { FhirTemporal.LocalDate(it) },
            location = accidentLocation
        ),
        userActionState = getDigaStatusForUserAction(),
        isNew = isNew,
        isArchived = isArchived
    )

    fun DeviceRequestDispenseEntityV1.toErpModel() = FhirDispenseDeviceRequestErpModel(
        deepLink = deepLink,
        declineCode = declineCode,
        referencePzn = referencePzn,
        redeemCode = redeemCode,
        note = note,
        display = display,
        status = status,
        modifiedDate = modifiedDate?.toInstant()?.asFhirTemporal()
    )
}
