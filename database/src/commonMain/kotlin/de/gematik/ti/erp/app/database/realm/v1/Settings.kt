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

package de.gematik.ti.erp.app.database.realm.v1

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.realm.utils.Cascading
import de.gematik.ti.erp.app.database.realm.utils.byteArrayBase64
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import kotlinx.datetime.LocalDateTime

// TODO remove after migration 38
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

// end remove
class PharmacySearchEntityV1 : RealmObject {
    var name: String = ""
    var locationEnabled: Boolean = false
    var filterReady: Boolean = false
    var filterDeliveryService: Boolean = false
    var filterOnlineService: Boolean = false
    var filterOpenNow: Boolean = false
}

@Requirement(
    "O.Data_1#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "All user permissions are set to false when the app starts and are changed " +
        "only when the user modifies them."
)
@Requirement(
    "A_24525#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Tracking is disabled by default."
)
class SettingsEntityV1 : RealmObject, Cascading {
    @Deprecated("Removed after migration 38, use AuthenticationEntityV1")
    var _authenticationMethod: String = SettingsAuthenticationMethodV1.Unspecified.toString()

    @Deprecated("Removed after migration 38, use AuthenticationEntityV1")
    fun authenticationMethod() = runCatching { SettingsAuthenticationMethodV1.valueOf(_authenticationMethod) }
        .getOrNull() ?: SettingsAuthenticationMethodV1.Unspecified

    @Deprecated("Removed after migration 38, use AuthenticationEntityV1")
    var authenticationFails: Int = 0

    @Deprecated("Removed after migration 38, use AuthenticationEntityV1")
    var password: PasswordEntityV1? = PasswordEntityV1()

    var authentication: AuthenticationEntityV1? = AuthenticationEntityV1()

    var zoomEnabled: Boolean = false
    var welcomeDrawerShown: Boolean = false

    // last updated time
    var time: RealmInstant = LocalDateTime(2021, 10, 15, 0, 0).toRealmInstant()

    var mainScreenTooltipsShown: Boolean = false

    var pharmacySearch: PharmacySearchEntityV1? = PharmacySearchEntityV1()

    var userHasAcceptedInsecureDevice: Boolean = false

    var userHasAcceptedIntegrityNotOk: Boolean = false

    var dataProtectionVersionAccepted: RealmInstant = LocalDateTime(2021, 10, 15, 0, 0).toRealmInstant()

    var latestAppVersionName: String = ""
    var latestAppVersionCode: Int = -1

    var onboardingLatestAppVersionName: String = ""
    var onboardingLatestAppVersionCode: Int = -1

    var shippingContact: ShippingContactEntityV1? = null
    var mlKitAccepted: Boolean = false

    @Requirement(
        "O.Data_13#1",
        "O.Resi_1#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Default settings does not allow screenshots",
        codeLines = 3
    )
    // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
    @Requirement(
        "O.Data_13#1",
        "O.Resi_1#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Default settings does not allow screenshots",
        codeLines = 3
    )
    var screenshotsAllowed: Boolean = false

    var trackingAllowed: Boolean = false

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            pharmacySearch?.let { yield(it) }
            password?.let { yield(it) }
            shippingContact?.let { yield(it) }
        }
}
