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

package de.gematik.ti.erp.app.debugsettings.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class DebugSettingsData(
    val eRezeptServiceURL: String,
    val eRezeptActive: Boolean,
    val idpUrl: String,
    val idpActive: Boolean,
    val apoVzdPharmacyServiceUrl: String,
    val fhirVzdPharmacyServiceUrl: String,
    val fhirVzdPharmacySearchAccessTokenUrl: String,
    val pharmacyServiceActive: Boolean,
    val bearerToken: String,
    val bearerTokenIsSet: Boolean,
    val fakeNFCCapabilities: Boolean,
    val cardAccessNumberIsSet: Boolean,
    val multiProfile: Boolean,
    val activeProfileId: ProfileIdentifier,
    val virtualHealthCardCert: String,
    val virtualHealthCardPrivateKey: String,
    val clientId: String
) : Parcelable
