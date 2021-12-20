/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.prescription.repository.LocalDataSource
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val localDataSource: LocalDataSource
) {

    fun loadCommunications(profile: CommunicationProfile, profileName: String) =
        localDataSource.loadCommunications(profile, profileName)

    fun loadUnreadCommunications(profile: CommunicationProfile, profileName: String) =
        localDataSource.loadUnreadCommunications(profile, profileName)

    suspend fun setCommunicationAcknowledgedStatus(communicationId: String, consumed: Boolean) {
        localDataSource.setCommunicationsAcknowledgedStatus(communicationId, consumed)
    }
}
