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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.byteArrayBase64
import de.gematik.ti.erp.app.db.entities.enumName
import de.gematik.ti.erp.app.db.entities.v1.SettingsAuthenticationMethodV1.Unspecified
import de.gematik.ti.erp.app.db.toRealmInstant
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import kotlinx.datetime.LocalDateTime

enum class SettingsAuthenticationMethodV1 {
    HealthCard,
    DeviceSecurity,
    Password,
    Unspecified
}

class PasswordEntityV1 : RealmObject {
    var _salt: String = ""

    @delegate:Ignore
    var salt: ByteArray by byteArrayBase64(::_salt)

    var _hash: String = ""

    @delegate:Ignore
    var hash: ByteArray by byteArrayBase64(::_hash)

    fun reset() {
        _salt = ""
        _hash = ""
    }
}

class PharmacySearchEntityV1 : RealmObject {
    var name: String = ""
    var locationEnabled: Boolean = false
    var filterReady: Boolean = false
    var filterDeliveryService: Boolean = false
    var filterOnlineService: Boolean = false
    var filterOpenNow: Boolean = false
}

@Requirement(
    "O.Data_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The settings of the app offer maximum data protection and security on first app start." +
        "User preferences are asked without discrimination within the onboarding process"
)
@Requirement(
    "O.Data_13",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "default settings are not allow screenshots"
)
class SettingsEntityV1 : RealmObject, Cascading {
    var _authenticationMethod: String = Unspecified.toString()

    @delegate:Ignore
    var authenticationMethod: SettingsAuthenticationMethodV1 by enumName(::_authenticationMethod)

    var authenticationFails: Int = 0
    var zoomEnabled: Boolean = false
    var welcomeDrawerShown: Boolean = false
    var mainScreenTooltipsShown: Boolean = false

    var pharmacySearch: PharmacySearchEntityV1? = PharmacySearchEntityV1()

    var userHasAcceptedInsecureDevice: Boolean = false
    var dataProtectionVersionAccepted: RealmInstant = LocalDateTime(2021, 10, 15, 0, 0).toRealmInstant()

    var password: PasswordEntityV1? = PasswordEntityV1()

    var latestAppVersionName: String = ""
    var latestAppVersionCode: Int = -1

    var onboardingLatestAppVersionName: String = ""
    var onboardingLatestAppVersionCode: Int = -1

    var shippingContact: ShippingContactEntityV1? = null
    var mlKitAccepted: Boolean = false

    // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
    var screenshotsAllowed: Boolean = false

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            pharmacySearch?.let { yield(it) }
            password?.let { yield(it) }
            shippingContact?.let { yield(it) }
        }
}
