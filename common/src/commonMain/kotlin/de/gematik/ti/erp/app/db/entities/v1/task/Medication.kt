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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.enumName
import de.gematik.ti.erp.app.db.entities.temporalAccessorNullable
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.ext.realmListOf

// https://simplifier.net/erezept/~resources?category=Profile&sortBy=RankScore_desc
// BTM = Betäubungsmittel, AMVV = Arzneimittelverschreibungsverordnung
enum class MedicationCategoryV1 {
    ARZNEI_UND_VERBAND_MITTEL,
    BTM,
    AMVV,
    SONSTIGES,
    UNKNOWN
}

class MedicationEntityV1 : RealmObject, Cascading {
    var text: String = ""
    var _medicationCategory: String = MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL.toString()

    @delegate:Ignore
    var medicationCategory: MedicationCategoryV1 by enumName(::_medicationCategory)
    var form: String? = null
    var amount: RatioEntityV1? = null
    var vaccine: Boolean = false
    var manufacturingInstructions: String? = null
    var packaging: String? = null
    var normSizeCode: String? = null

    @Deprecated("use IdentifierEntityV1")
    var uniqueIdentifier: String? = null // PZN
    var identifier: IdentifierEntityV1? = IdentifierEntityV1()
    var lotNumber: String? = null

    var _expirationDate: String? = null

    @delegate:Ignore
    var expirationDate: FhirTemporal? by temporalAccessorNullable(::_expirationDate)

    var ingredientMedications: RealmList<MedicationEntityV1> = realmListOf()
    var ingredients: RealmList<IngredientEntityV1> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(ingredients)
            identifier?.let { yield(it) }
            amount?.let { yield(it) }
        }
}
