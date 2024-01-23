/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.mapper

import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData

fun ScannedTaskData.ScannedTask.toOrder() =
    PharmacyUseCaseData.PrescriptionOrder(
        taskId = taskId,
        accessCode = accessCode,
        title = name,
        index = index,
        timestamp = scannedOn,
        substitutionsAllowed = false
    )

fun SyncedTaskData.SyncedTask.toOrder() =
    PharmacyUseCaseData.PrescriptionOrder(
        taskId = taskId,
        accessCode = accessCode!!, // TODO: check, why we get here a nullable!!
        title = medicationName(),
        index = null,
        timestamp = authoredOn,
        substitutionsAllowed = false
    )
