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

package de.gematik.ti.erp.app.consent.repository

import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.realm.kotlin.Realm

class ConsentLocalDataSource(
    private val realm: Realm
) {
    suspend fun saveGiveConsentDrawerShown(profileId: ProfileIdentifier) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.isConsentDrawerShown = true
            }
        }
    }

    fun getConsentDrawerShown(profileIdentifier: ProfileIdentifier): Boolean =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileIdentifier)
            ?.isConsentDrawerShown ?: false
}
