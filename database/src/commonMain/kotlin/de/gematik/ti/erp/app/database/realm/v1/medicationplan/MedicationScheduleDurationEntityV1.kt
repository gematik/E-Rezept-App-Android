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

package de.gematik.ti.erp.app.db.entities.v1.medicationplan

import de.gematik.ti.erp.app.database.realm.utils.enumName
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class MedicationScheduleDurationEntityV1 : RealmObject {
    var _type: String = MedicationScheduleDurationTypeV1.ENDLESS.name

    @delegate:Ignore
    var type: MedicationScheduleDurationTypeV1 by enumName(::_type)
    var startDate: RealmInstant = RealmInstant.MIN
    var endDate: RealmInstant = RealmInstant.MAX
}

enum class MedicationScheduleDurationTypeV1 {
    ENDLESS,
    END_OF_PACK,
    PERSONALIZED
}
