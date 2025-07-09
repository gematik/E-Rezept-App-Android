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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.fhir.model.DigaStatus.CompletedWithRejection
import de.gematik.ti.erp.app.fhir.model.DigaStatus.DownloadDigaApp
import de.gematik.ti.erp.app.fhir.model.DigaStatus.InProgress
import de.gematik.ti.erp.app.fhir.model.DigaStatus.OpenAppWithRedeemCode
import de.gematik.ti.erp.app.fhir.model.DigaStatus.ReadyForSelfArchiveDiga
import de.gematik.ti.erp.app.fhir.model.DigaStatus.SelfArchiveDiga
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import de.gematik.ti.erp.app.fhir.model.DigaStatusSteps.CompletedWithRejection as DigaStepCompletedWithRejection
import de.gematik.ti.erp.app.fhir.model.DigaStatusSteps.InProgress as DigaStepInProgress

class DeviceRequestEntityV1 : RealmObject {
    var id: String = ""
    var intent: String = ""
    var status: String = ""
    var pzn: String = ""
    var appName: String = ""
    var isSelfUse: Boolean = false
    var authoredOn: RealmInstant = RealmInstant.MIN
    var accidentType: String = ""
    var accidentLocation: String = ""
    var accidentDate: RealmInstant? = null

    // User action states mapped to step numbers:
    // 2  -> InProgress
    // 4  -> DownloadDigaApp
    // 5  -> OpenAppWithRedeemCode
    // 6  -> ReadyForSelfArchiveDiga
    // 7  -> SelfArchiveDiga
    var userActionState: Int? = null

    // time when dispenseRequest communication was sent
    var sentCommunicationOn: RealmInstant? = null
    var isNew: Boolean = true
    var isArchived: Boolean = false

    @Suppress("MagicNumber")
    companion object {

        private val userActionSteps: Map<Int, DigaStatus> = listOf(
            DownloadDigaApp,
            OpenAppWithRedeemCode,
            ReadyForSelfArchiveDiga,
            SelfArchiveDiga
        ).associateBy { it.step }

        fun DeviceRequestEntityV1.getDigaStatusForUserAction(): DigaStatus? {
            return userActionState?.let { step ->
                when (step) {
                    DigaStepInProgress.step -> InProgress(sentCommunicationOn?.toInstant())
                    DigaStepCompletedWithRejection.step -> CompletedWithRejection(sentCommunicationOn?.toInstant())
                    else -> userActionSteps[step]
                }
            }
        }
    }
}
