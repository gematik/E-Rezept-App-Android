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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.datasource.data

import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Instant

object MockProfileInfo {
    private const val HEALTH_INSURANCE_COMPANIES = "GesundheitsVersichert AG"
    private const val INSURANCE_NUMBER = "10000000"

    internal val mockProfile01 = profile(
        profileName = "Erika Mustermann",
        isActive = true,
        color = ProfileColorNames.SUN_DEW,
        insuranceType = InsuranceType.PKV,
        avatar = Avatar.FemaleDoctor,
        lastAuthenticated = null
    )

    private fun profile(
        profileName: String,
        isActive: Boolean = true,
        color: ProfileColorNames = ProfileColorNames.SUN_DEW,
        avatar: Avatar = Avatar.FemaleDoctor,
        insuranceType: InsuranceType = InsuranceType.GKV,
        lastAuthenticated: Instant? = null
    ): ProfileErpModel {
        return ProfileErpModel(
            id = "1",
            name = profileName,
            profileImageData = ProfileImageDataErpModel(
                color = color,
                avatar = avatar,
                image = null
            ),
            isConsentDrawerShown = false,
            insuranceData = ProfileInsuranceDataErpModel(
                insurantName = profileName,
                insuranceIdentifier = INSURANCE_NUMBER,
                insuranceName = HEALTH_INSURANCE_COMPANIES,
                insuranceType = insuranceType,
                organizationIdentifier = null
            ),
            lastAuthenticated = lastAuthenticated,
            lastAuditEventSynced = null,
            lastTaskSynced = null,
            active = isActive,
            isNewlyCreated = false,
            userAuthentication = UserAuthenticationErpModel.NotInitialized
        )
    }

    internal fun String.create() = profile(profileName = this)
}
