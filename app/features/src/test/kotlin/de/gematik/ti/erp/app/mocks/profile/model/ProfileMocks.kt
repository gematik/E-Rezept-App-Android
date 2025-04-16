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

package de.gematik.ti.erp.app.mocks.profile.model

import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.datetime.Instant

val MODEL_PROFILE = ProfilesUseCaseData.Profile(
    id = "id",
    name = "name",
    insurance = ProfileInsuranceInformation(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = byteArrayOf(0x00, 0x01, 0x02),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = null
)
