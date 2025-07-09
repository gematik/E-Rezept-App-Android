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

package de.gematik.ti.erp.app.digas.model

import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.datetime.Instant

object TestProfileData {
    val MOCK_PROFILE = ProfilesUseCaseData.Profile(
        id = "test-profile-1",
        name = "Test User",
        insurance = ProfileInsuranceInformation(
            insuranceIdentifier = "123456789",
            insuranceName = "Test Insurance",
            insuranceType = ProfilesUseCaseData.InsuranceType.GKV
        ),
        isActive = true,
        color = ProfilesData.ProfileColorNames.BLUE_MOON,
        avatar = ProfilesData.Avatar.PersonalizedImage,
        image = null,
        lastAuthenticated = Instant.parse("2024-03-20T10:00:00Z"),
        ssoTokenScope = null
    )
}
