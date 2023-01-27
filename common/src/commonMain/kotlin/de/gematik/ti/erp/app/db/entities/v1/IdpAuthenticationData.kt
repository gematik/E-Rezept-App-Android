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

import de.gematik.ti.erp.app.db.entities.byteArrayBase64Nullable
import de.gematik.ti.erp.app.db.entities.enumName
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

enum class SingleSignOnTokenScopeV1 {
    Default,
    AlternateAuthentication,
    ExternalAuthentication
}

class IdpAuthenticationDataEntityV1 : RealmObject {
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
