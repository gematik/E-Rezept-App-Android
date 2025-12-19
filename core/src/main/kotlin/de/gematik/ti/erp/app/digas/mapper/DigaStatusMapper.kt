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

package de.gematik.ti.erp.app.digas.mapper

import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.model.DigaStatus.CompletedSuccessfully
import de.gematik.ti.erp.app.diga.model.DigaStatus.CompletedWithRejection
import de.gematik.ti.erp.app.diga.model.DigaStatus.InProgress
import de.gematik.ti.erp.app.diga.model.DigaStatus.Ready
import de.gematik.ti.erp.app.diga.model.DigaStatus.WrappedTaskStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.TaskStatus
import kotlinx.datetime.Instant

fun TaskStatus.mapToDigaStatus(
    isDeclined: Boolean,
    isRedeemed: Boolean,
    sentOn: Instant?,
    userActionState: DigaStatus?
): DigaStatus = when {
    userActionState != null -> userActionState
    this == TaskStatus.Ready -> Ready
    this == TaskStatus.InProgress -> InProgress(sentOn)
    this == TaskStatus.Completed && isRedeemed -> CompletedSuccessfully
    this == TaskStatus.Completed && isDeclined -> CompletedWithRejection(sentOn)
    else -> WrappedTaskStatus(this.name)
}
