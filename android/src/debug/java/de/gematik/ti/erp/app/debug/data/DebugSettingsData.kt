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

package de.gematik.ti.erp.app.debug.data

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
    val pharmacyServiceUrl: String,
    val pharmacyServiceActive: Boolean,
    val bearerToken: String,
    val bearerTokenIsSet: Boolean,
    val fakeNFCCapabilities: Boolean,
    val cardAccessNumberIsSet: Boolean,
    val multiProfile: Boolean,
    val activeProfileId: ProfileIdentifier,
    val virtualHealthCardCert: String,
    val virtualHealthCardPrivateKey: String
) : Parcelable

enum class Environment {
    PU, TU, RU, RUDEV, TR
}
