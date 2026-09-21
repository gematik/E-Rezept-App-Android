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

package de.gematik.ti.erp.app.mocks.profile

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.days

class ProfileDataSource {

    private val can = "123123"
    private val byteArray = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    private val healthCertificate = X509CertificateHolder(byteArray)
    private val singleSignOnToken = SingleSignOnTokenErpModel(
        token = UUID.randomUUID().toString(),
        expiresOn = Clock.System.now().plus(200.days),
        validOn = Clock.System.now().plus(20.days)
    )

    private val firstProfile = ProfileErpModel(
        id = "1",
        name = "Max Mustermann",
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.BLUE_MOON,
            avatar = Avatar.ManWithPhone,
            image = null
        ),
        isConsentDrawerShown = true,
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = "Max Mustermann",
            insuranceIdentifier = null,
            insuranceName = "AOK",
            insuranceType = InsuranceType.GKV,
            organizationIdentifier = null
        ),
        lastAuthenticated = Clock.System.now(),
        lastAuditEventSynced = null,
        lastTaskSynced = null,
        active = true,
        isNewlyCreated = false,
        userAuthentication = UserAuthenticationErpModel.HealthCard(
            singleSignOnTokenErpModel = singleSignOnToken,
            cardAccessNumber = can,
            healthCardCertificate = healthCertificate.encoded
        )
    )

    val profiles = MutableStateFlow(mutableListOf(firstProfile))
}
