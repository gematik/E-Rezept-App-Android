/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.byteArrayBase64
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class IdpConfigurationEntityV1 : RealmObject {
    var authorizationEndpoint: String = ""
    var ssoEndpoint: String = ""
    var tokenEndpoint: String = ""
    var pairingEndpoint: String = ""
    var authenticationEndpoint: String = ""
    var pukIdpEncEndpoint: String = ""
    var pukIdpSigEndpoint: String = ""

    var _certificateX509Base64: String = ""

    @delegate:Ignore
    var certificateX509: ByteArray by byteArrayBase64(::_certificateX509Base64)

    var expirationTimestamp: RealmInstant = RealmInstant.MIN
    var issueTimestamp: RealmInstant = RealmInstant.MIN

    var externalAuthorizationIDsEndpoint: String? = null
    var thirdPartyAuthorizationEndpoint: String? = null
}
