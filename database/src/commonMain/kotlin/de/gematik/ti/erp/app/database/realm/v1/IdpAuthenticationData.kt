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

import de.gematik.ti.erp.app.database.realm.utils.byteArrayBase64Nullable
import de.gematik.ti.erp.app.database.realm.utils.enumName
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

enum class SingleSignOnTokenScopeV1 {
    Default,
    AlternateAuthentication,
    ExternalAuthentication
}

class IdpAuthenticationDataEntityV1 :
    RealmObject {
    var singleSignOnToken: String? = null

    var _singleSignOnTokenScope: String = SingleSignOnTokenScopeV1.Default.toString()

    @delegate:Ignore
    var singleSignOnTokenScope: SingleSignOnTokenScopeV1 by enumName(::_singleSignOnTokenScope)

    var cardAccessNumber: String = ""

    var _healthCardCertificate: String? = null

    @delegate:Ignore
    var healthCardCertificate: ByteArray? by byteArrayBase64Nullable(::_healthCardCertificate)

    var _aliasOfSecureElementEntry: String? = null

    @delegate:Ignore
    var aliasOfSecureElementEntry: ByteArray? by byteArrayBase64Nullable(::_aliasOfSecureElementEntry)

    var externalAuthenticatorId: String? = null
    var externalAuthenticatorName: String? = null
}
