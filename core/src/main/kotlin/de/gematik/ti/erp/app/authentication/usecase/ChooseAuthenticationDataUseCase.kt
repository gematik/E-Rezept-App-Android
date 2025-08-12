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

import de.gematik.ti.erp.app.authentication.model.Biometric
import de.gematik.ti.erp.app.authentication.model.External
import de.gematik.ti.erp.app.authentication.model.HealthCard
import de.gematik.ti.erp.app.authentication.model.InitialAuthenticationData
import de.gematik.ti.erp.app.authentication.model.None
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.mapper.toModel
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.validateRequirementForLastAuthUpdateRequired
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
    ): Flow<InitialAuthenticationData> =
        withContext(dispatcher) {
            idpRepository.authenticationData(profileId)
                .mapNotNull { idpAuthenticationData ->
                    val profile = profileRepository.getProfileById(profileId)
                        .first()
                        .toModel()
                        .validateRequirementForLastAuthUpdateRequired { id, lastAuthenticated ->
                            launch {
                                profileRepository.updateLastAuthenticated(
                                    id,
                                    lastAuthenticated
                                )
                            }
                        }
                    val ssoTokenScope = idpAuthenticationData.singleSignOnTokenScope

                    Napier.i(
                        tag = "Authentication State",
                        message = "ssoTokenScope for choosing authentication ${ssoTokenScope?.token?.token}"
                    )

                    when (ssoTokenScope) {
                        is IdpData.ExternalAuthenticationToken -> External(
                            authenticatorId = ssoTokenScope.authenticatorId,
                            authenticatorName = ssoTokenScope.authenticatorName,
                            profile = profile
                        )

                        is IdpData.AlternateAuthenticationToken,
                        is IdpData.AlternateAuthenticationWithoutToken -> Biometric(profile = profile)

                        is IdpData.DefaultToken -> HealthCard(
                            can = ssoTokenScope.cardAccessNumber,
                            profile = profile
                        )

                        null -> None(profile = profile)
                    }
                }
        }
}
