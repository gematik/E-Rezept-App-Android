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

package de.gematik.ti.erp.app.database.realm.v1.userauthentication

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.database.realm.v1.profile.ProfileEntityV1
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserAuthenticationLocalDataSourceV1(private val realm: Realm) : UserAuthenticationLocalDataSource {
    override fun getUserAuthenticationForProfile(profileIdentifier: ProfileIdentifier): Flow<UserAuthenticationErpModel> =
        realm.query<ProfileEntityV1>("id = $0", profileIdentifier)
            .first()
            .asFlow()
            .map { profile ->
                profile.obj?.idpAuthenticationData?.toUserAuthenticationErpModel() ?: UserAuthenticationErpModel.NotInitialized
            }

    @Requirement(
        "A_21328#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Save the SSO token to database that is encrypted."
    )
    override suspend fun saveUserAuthenticationForProfile(
        profileIdentifier: ProfileIdentifier,
        userAuthentication: UserAuthenticationErpModel
    ) {
        realm.writeToRealm<ProfileEntityV1, Unit>("id == $0", profileIdentifier) { profile ->
            val entity = userAuthentication.toIdpAuthenticationDataEntityV1()
            profile.idpAuthenticationData = copyToRealm(entity)
        }
    }

    override suspend fun invalidateSingleSignOnTokenForProfile(profileIdentifier: ProfileIdentifier) {
        realm.writeToRealm<ProfileEntityV1, Unit>("id == $0", profileIdentifier) { profile ->
            profile.idpAuthenticationData = profile.idpAuthenticationData.apply {
                this?.singleSignOnToken = null
            }
        }
    }

    override suspend fun deleteUserAuthenticationDataForProfile(profileIdentifier: ProfileIdentifier) {
        realm.writeToRealm<ProfileEntityV1, Unit>("id == $0", profileIdentifier) { profile ->
            profile.idpAuthenticationData = copyToRealm(IdpAuthenticationDataEntityV1())
        }
    }
}
