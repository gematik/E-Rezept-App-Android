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

import de.gematik.ti.erp.app.database.realm.utils.Cascading
import de.gematik.ti.erp.app.database.realm.utils.byteArrayBase64
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class AuthenticationEntityV1 : RealmObject, Cascading {
    var password: AuthenticationPasswordEntityV1? = null
    var deviceSecurity: Boolean = false
    var failedAuthenticationAttempts: Int = 0
    var authenticationTimeOutSystemUptime: Long? = null

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            password?.let { yield(it) }
        }
}

class AuthenticationPasswordEntityV1 : RealmObject {
    var _salt: String = ""

    @delegate:Ignore
    var salt: ByteArray by byteArrayBase64(::_salt)

    var _hash: String = ""

    @delegate:Ignore
    var hash: ByteArray by byteArrayBase64(::_hash)

    fun setSalt(salt: String) {
        this._salt = salt
    }

    fun setHash(hash: String) {
        this._hash = hash
    }
}
