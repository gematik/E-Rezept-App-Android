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

package de.gematik.ti.erp.app.authentication.usecase

import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profile.model.ProfileErpModel.Companion.validateRequirementForLastAuthUpdateRequired
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseAuthenticationDataUseCase(
    private val profileRepository: ProfileRepository,
    private val idpRepository: IdpRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        profileId: ProfileIdentifier
    ): Flow<UserAuthenticationErpModel> =
        withContext(dispatcher) {
            idpRepository.getUserAuthentication(profileId)
                .mapNotNull { userAuthentication ->
                    profileRepository.getProfileById(profileId)
                        .first()
                        .validateRequirementForLastAuthUpdateRequired { id, lastAuthenticated ->
                            launch {
                                profileRepository.updateLastAuthenticated(
                                    id,
                                    lastAuthenticated
                                )
                            }
                        }

                    Napier.i(
                        tag = "Authentication State",
                        message = "userAuthentication for choosing authentication ${userAuthentication.singleSignOnTokenErpModel?.token}"
                    )

                    userAuthentication
                }
        }
}
