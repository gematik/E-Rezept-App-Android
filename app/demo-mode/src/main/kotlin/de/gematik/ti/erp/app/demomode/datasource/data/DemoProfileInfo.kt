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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.EXPIRY_DATE
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.START_DATE
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.datetime.Instant
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import java.util.UUID

object DemoProfileInfo {
    private const val CAN = "123123"
    private val byteArray = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    private val HEALTH_CERTIFICATE = X509CertificateHolder(byteArray)
    private val singleSignOnToken = IdpData.SingleSignOnToken(
        token = UUID.randomUUID().toString(),
        expiresOn = EXPIRY_DATE,
        validOn = START_DATE
    )

    // TODO: Add demo mode for different modes of sign-on scopes
    private val cardToken = IdpData.DefaultToken(
        token = singleSignOnToken,
        cardAccessNumber = CAN,
        healthCardCertificate = HEALTH_CERTIFICATE
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
        insuranceType = InsuranceTypeV1.PKV, // Note: Private insurance account
        avatar = ProfilesData.Avatar.FemaleDoctorWithPhone,
        lastAuthenticated = null
    )

    /**
     * This [demoProfile02] always starts with orders, so if modifying please take care of that too
     */
    internal val demoProfile02 = profile(
        profileName = "Max Mustermann",
        isActive = false,
        insuranceType = InsuranceTypeV1.GKV,
        avatar = ProfilesData.Avatar.OldManOfColor,
        lastAuthenticated = null
    )

    private fun profile(
        profileName: String,
        isActive: Boolean = true,
        color: ProfilesData.ProfileColorNames = ProfilesData.ProfileColorNames.BLUE_MOON,
        avatar: ProfilesData.Avatar = ProfilesData.Avatar.FemaleDeveloper,
        insuranceType: InsuranceTypeV1 = InsuranceTypeV1.GKV,
        lastAuthenticated: Instant? = null,
        singleSignOnTokenScope: IdpData.SingleSignOnTokenScope? = cardToken
    ) = ProfilesData.Profile(
        id = UUID.randomUUID().toString(),
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

    internal fun String.create() = profile(profileName = this)
}
