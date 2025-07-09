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

package de.gematik.ti.erp.app.authentication.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

@Stable
sealed interface InitialAuthenticationData {
    val profile: ProfilesUseCaseData.Profile
}

data class HealthCard(val can: String, override val profile: ProfilesUseCaseData.Profile) :
    InitialAuthenticationData

data class Biometric(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData
data class External(
    val authenticatorId: String,
    val authenticatorName: String,
    override val profile: ProfilesUseCaseData.Profile
) : InitialAuthenticationData

data class None(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData
