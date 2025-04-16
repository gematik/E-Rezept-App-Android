/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.idp.model.IdpData.AlternateAuthenticationWithoutToken
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.mapper.toModel
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

/**
 * Gets all the profiles from the [repository]
 */
class GetProfilesUseCase(
    private val repository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(): Flow<List<Profile>> =
        repository.profiles().mapNotNull { profiles ->
            profiles.map { it.toModel() }
        }.distinctUntilChanged()
            .onEach { profiles ->
                profiles.forEach { profile ->
                    when {
                        profile.ssoTokenScope != null &&
                            profile.ssoTokenScope !is AlternateAuthenticationWithoutToken &&
                            profile.lastAuthenticated == null -> {
                            profile.ssoTokenScope?.token?.let { token ->
                                repository.updateLastAuthenticated(profile.id, token.validOn)
                            }
                        }
                    }
                }
            }.flowOn(dispatcher)
}
