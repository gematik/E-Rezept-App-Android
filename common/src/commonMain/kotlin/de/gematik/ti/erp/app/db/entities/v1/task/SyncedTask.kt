/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.enumName
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.ext.realmListOf

enum class TaskStatusV1 {
    Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed;
}

class SyncedTaskEntityV1 : RealmObject, Cascading {
    // Task Entities

    var taskId: String = ""
    var accessCode: String? = null
    var lastModified: RealmInstant = RealmInstant.MIN

    var expiresOn: RealmInstant? = null
    var acceptUntil: RealmInstant? = null
    var authoredOn: RealmInstant = RealmInstant.MIN

    // KBV Bundle Entities

    var organization: OrganizationEntityV1? = null // an organization can contain multiple authors
    var practitioner: PractitionerEntityV1? = null
    var patient: PatientEntityV1? = null
    var insuranceInformation: InsuranceInformationEntityV1? = null

    var _status: String = TaskStatusV1.Other.toString()

    @delegate:Ignore
    var status: TaskStatusV1 by enumName(::_status)

    var medicationRequest: MedicationRequestEntityV1? = null
    var medicationDispenses: RealmList<MedicationDispenseEntityV1> = realmListOf()

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
            yield(medicationDispenses)
            yield(communications)
        }
}
