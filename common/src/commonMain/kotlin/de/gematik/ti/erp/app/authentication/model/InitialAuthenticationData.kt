/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.authentication.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

@Stable
sealed interface InitialAuthenticationData {
    val profile: ProfilesUseCaseData.Profile
}

data class HealthCard(val can: String, override val profile: ProfilesUseCaseData.Profile) :
    InitialAuthenticationData

data class SecureElement(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData
data class External(
    val authenticatorId: String,
    val authenticatorName: String,
    override val profile: ProfilesUseCaseData.Profile
) : InitialAuthenticationData

data class None(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData
