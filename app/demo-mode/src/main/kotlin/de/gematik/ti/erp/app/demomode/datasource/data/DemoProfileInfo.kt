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
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Instant
import java.util.UUID

object DemoProfileInfo {
    private const val AUTHENTICATOR_NAME = "Gematik Versicherung"
    private val singleSignOnToken = SingleSignOnTokenErpModel(
        token = UUID.randomUUID().toString(),
        expiresOn = EXPIRY_DATE,
        validOn = START_DATE
    )

    private val token = UserAuthenticationErpModel.External(
        singleSignOnTokenErpModel = singleSignOnToken,
        externalAuthenticatorId = UUID.randomUUID().toString(),
        externalAuthenticatorName = AUTHENTICATOR_NAME
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
        color = ProfileColorNames.SUN_DEW,
        insuranceType = InsuranceType.PKV,
        avatar = listOf(
            Avatar.FemaleDoctor,
            Avatar.FemaleDoctorWithPhone,
            Avatar.WomanWithHeadScarf,
            Avatar.WomanWithPhone,
            Avatar.Grandmother,
            Avatar.FemaleDeveloper
        ).random(),
        lastAuthenticated = null
    )

    /**
     * This [demoProfile02] always starts with orders, so if modifying please take care of that too
     */
    internal val demoProfile02 = profile(
        profileName = "Max Mustermann",
        isActive = false,
        insuranceType = InsuranceType.GKV,
        avatar = listOf(
            Avatar.OldManOfColor,
            Avatar.Grandfather,
            Avatar.ManWithPhone,
            Avatar.WheelchairUser,
            Avatar.MaleDoctorWithPhone
        ).random(),
        lastAuthenticated = null
    )

    internal fun demoEmptyProfile(name: String) = profile(
        name
    )

    private fun profile(
        profileName: String,
        isActive: Boolean = true,
        color: ProfileColorNames = ProfileColorNames.entries.toTypedArray().random(),
        avatar: Avatar = Avatar.entries.toTypedArray().random(),
        insuranceType: InsuranceType = InsuranceType.GKV,
        lastAuthenticated: Instant? = null,
        userAuthenticationErpModel: UserAuthenticationErpModel? = token
    ): DemoModeProfile {
        val uuid = UUID.randomUUID()
        return DemoModeProfile(
            demoModeId = uuid,
            id = uuid.toString(),
            name = profileName,
            color = color,
            avatar = avatar,
            insurantName = profileName,
            insuranceIdentifier = insuranceNumberGenerator(),
            insuranceName = HEALTH_INSURANCE_COMPANIES.random(),
            insuranceType = insuranceType,
            lastAuthenticated = lastAuthenticated,
            userAuthentication = userAuthenticationErpModel,
            active = isActive
        )
    }

    internal fun String.create() = profile(profileName = this)
}
