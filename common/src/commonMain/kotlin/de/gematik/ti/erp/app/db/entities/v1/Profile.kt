/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.byteArrayBase64Nullable
import de.gematik.ti.erp.app.db.entities.enumName
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

enum class ProfileColorNamesV1 {
    SPRING_GRAY,
    SUN_DEW,
    PINK,
    TREE,
    BLUE_MOON
}

// tag::ProfileEntity[]

// Info: don't change any figure name because names are saved into database
enum class AvatarFigureV1 {
    PersonalizedImage,
    FemaleDoctor,
    WomanWithHeadScarf,
    Grandfather,
    BoyWithHealthCard,
    OldManOfColor,
    WomanWithPhone,
    Grandmother,
    ManWithPhone,
    WheelchairUser,
    Baby,
    MaleDoctorWithPhone,
    FemaleDoctorWithPhone,
    FemaleDeveloper
}

enum class InsuranceTypeV1 {
    GKV,
    PKV,
    None
}

class ProfileEntityV1 : RealmObject, Cascading {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var name: String = ""

    var _avatarFigure: String = AvatarFigureV1.PersonalizedImage.toString()

    @delegate:Ignore
    var avatarFigure: AvatarFigureV1 by enumName(::_avatarFigure)

    var _insuranceType: String = InsuranceTypeV1.None.toString()

    @delegate:Ignore
    var insuranceType: InsuranceTypeV1 by enumName(::_insuranceType)

    var _colorName: String = ProfileColorNamesV1.SPRING_GRAY.toString()

    @delegate:Ignore
    var color: ProfileColorNamesV1 by enumName(::_colorName)

    var _personalizedImage: String? = null

    @delegate:Ignore
    var personalizedImage: ByteArray? by byteArrayBase64Nullable(::_personalizedImage)

    var insurantName: String? = null
    var insuranceIdentifier: String? = null
    var insuranceName: String? = null

    var lastAuthenticated: RealmInstant? = null
    var lastAuditEventSynced: RealmInstant? = null
    var lastTaskSynced: RealmInstant? = null

    var active: Boolean = false

    var syncedTasks: RealmList<SyncedTaskEntityV1> = realmListOf()
    var scannedTasks: RealmList<ScannedTaskEntityV1> = realmListOf()
    var invoices: RealmList<PKVInvoiceEntityV1> = realmListOf()

    var idpAuthenticationData: IdpAuthenticationDataEntityV1? = null
    var auditEvents: RealmList<AuditEventEntityV1> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(syncedTasks)
            yield(scannedTasks)
            yield(auditEvents)
            yield(invoices)
            idpAuthenticationData?.let { yield(it) }
        }
}
// end::ProfileEntity[]
