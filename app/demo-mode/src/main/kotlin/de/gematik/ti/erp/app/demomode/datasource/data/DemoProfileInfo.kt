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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.START_DATE
import de.gematik.ti.erp.app.demomode.model.DemoModeProfile
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.datetime.Instant
import java.util.UUID

object DemoProfileInfo {
    private const val AUTHENTICATOR_NAME = "Gematik Versicherung"
    private val singleSignOnToken = IdpData.SingleSignOnToken(
        token = UUID.randomUUID().toString(),
        expiresOn = EXPIRY_DATE,
        validOn = START_DATE
    )

    // TODO: Add demo mode for different modes of sign-on scopes
    private val token = IdpData.ExternalAuthenticationToken(
        token = singleSignOnToken,
        authenticatorName = AUTHENTICATOR_NAME,
        authenticatorId = UUID.randomUUID().toString()
    )
    private val HEALTH_INSURANCE_COMPANIES = listOf(
        "GesundheitsVersichert AG",
        "HeilungsHüter Versicherung",
        "VitalSchutz GmbH",
        "GesundheitsRundum Versicherung",
        "MediSicher Deutschland",
        "PflegePlus Versicherungsgruppe",
        "GesundheitsVorsorge AG",
        "HeilHaus Versicherungen",
        "LebenFit Krankenversicherung",
        "GesundheitsZirkel Versicherung"
    )

    private fun insuranceNumberGenerator(): String {
        val letter = ('A'..'Z').random()
        val randomNumber = (10000000..99999999).random()
        return "$letter$randomNumber"
    }

    internal val demoProfile01 = profile(
        profileName = "Erika Mustermann",
        isActive = true,
        color = ProfilesData.ProfileColorNames.SUN_DEW,
        insuranceType = ProfilesData.InsuranceType.GKV,
        avatar = listOf(
            ProfilesData.Avatar.FemaleDoctor,
            ProfilesData.Avatar.FemaleDoctorWithPhone,
            ProfilesData.Avatar.WomanWithHeadScarf,
            ProfilesData.Avatar.WomanWithPhone,
            ProfilesData.Avatar.Grandmother,
            ProfilesData.Avatar.FemaleDeveloper
        ).random(),
        lastAuthenticated = null
    )

    /**
     * This [demoProfile02] always starts with orders, so if modifying please take care of that too
     */
    internal val demoProfile02 = profile(
        profileName = "Max Mustermann",
        isActive = false,
        insuranceType = ProfilesData.InsuranceType.GKV,
        avatar = listOf(
            ProfilesData.Avatar.OldManOfColor,
            ProfilesData.Avatar.Grandfather,
            ProfilesData.Avatar.ManWithPhone,
            ProfilesData.Avatar.WheelchairUser,
            ProfilesData.Avatar.MaleDoctorWithPhone
        ).random(),
        lastAuthenticated = null
    )

    internal fun demoEmptyProfile(name: String) = profile(
        name
    )

    private fun profile(
        profileName: String,
        isActive: Boolean = true,
        color: ProfilesData.ProfileColorNames = ProfilesData.ProfileColorNames.entries.toTypedArray().random(),
        avatar: ProfilesData.Avatar = ProfilesData.Avatar.entries.toTypedArray().random(),
        insuranceType: ProfilesData.InsuranceType = ProfilesData.InsuranceType.GKV,
        lastAuthenticated: Instant? = null,
        singleSignOnTokenScope: IdpData.SingleSignOnTokenScope? = token
    ): DemoModeProfile {
        val uuid = UUID.randomUUID()
        return DemoModeProfile(
            demoModeId = uuid,
            id = uuid.toString(),
            name = profileName,
            color = color,
            avatar = avatar,
            insuranceIdentifier = insuranceNumberGenerator(),
            insuranceType = insuranceType,
            insurantName = profileName,
            insuranceName = HEALTH_INSURANCE_COMPANIES.random(),
            singleSignOnTokenScope = singleSignOnTokenScope,
            active = isActive,
            lastAuthenticated = lastAuthenticated
        )
    }

    internal fun String.create(): DemoModeProfile = profile(profileName = this)
}
