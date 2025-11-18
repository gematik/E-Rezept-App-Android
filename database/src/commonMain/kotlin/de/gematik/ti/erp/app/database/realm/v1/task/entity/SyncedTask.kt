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

package de.gematik.ti.erp.app.database.realm.v1.task.entity

import de.gematik.ti.erp.app.database.realm.utils.Cascading
import de.gematik.ti.erp.app.database.realm.utils.enumName
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

enum class TaskStatusV1 {
    Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed
}

class SyncedTaskEntityV1 : RealmObject, Cascading {
    // Task Entities

    var taskId: String = ""
    var accessCode: String = ""
    var lastModified: RealmInstant = RealmInstant.MIN
    var lastMedicationDispense: RealmInstant? = RealmInstant.MIN

    var expiresOn: RealmInstant? = null
    var acceptUntil: RealmInstant? = null
    var authoredOn: RealmInstant = RealmInstant.MIN

    var isEuRedeemableByProperties: Boolean = false
    var isEuRedeemableByPatientAuthorization: Boolean = false

    // KBV Bundle Entities

    var organization: OrganizationEntityV1? = null // an organization can contain multiple authors
    var practitioner: PractitionerEntityV1? = null
    var patient: PatientEntityV1? = null
    var insuranceInformation: InsuranceInformationEntityV1? = null

    var _status: String = TaskStatusV1.Other.toString()

    @delegate:Ignore
    var status: TaskStatusV1 by enumName(::_status)

    var medicationRequest: MedicationRequestEntityV1? = null

    var deviceRequest: DeviceRequestEntityV1? = null

    var medicationDispenses: RealmList<MedicationDispenseEntityV1> = realmListOf() // Code amd deepLink

    var communications: RealmList<CommunicationEntityV1> = realmListOf()

    // back reference
    var parent: ProfileEntityV1? = null

    var isIncomplete: Boolean = false
    var pvsIdentifier: String = ""
    var failureToReport: String = ""

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            organization?.let { yield(it) }
            practitioner?.let { yield(it) }
            patient?.let { yield(it) }
            insuranceInformation?.let { yield(it) }
            medicationRequest?.let { yield(it) }
            deviceRequest?.let { yield(it) }
            yield(medicationDispenses)
            yield(communications)
        }
}
