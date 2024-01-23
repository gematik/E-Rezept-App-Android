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

package de.gematik.ti.erp.app.profiles.usecase.mapper

import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

fun ProfilesData.Profile.toModel() =
    ProfilesUseCaseData.Profile(
        id = id,
        name = name,
        insurance = ProfileInsuranceInformation(
            insurantName = insurantName ?: "",
            insuranceIdentifier = insuranceIdentifier ?: "",
            insuranceName = insuranceName ?: "",
            insuranceType = when (insuranceType) {
                InsuranceTypeV1.None -> ProfilesUseCaseData.InsuranceType.NONE
                InsuranceTypeV1.GKV -> ProfilesUseCaseData.InsuranceType.GKV
                InsuranceTypeV1.PKV -> ProfilesUseCaseData.InsuranceType.PKV
            }
        ),
        active = active,
        color = color,
        avatar = avatar,
        image = personalizedImage,
        lastAuthenticated = lastAuthenticated,
        ssoTokenScope = singleSignOnTokenScope
    )
