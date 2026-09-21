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

package de.gematik.ti.erp.app.mocks.profile.model

import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Instant

val MODEL_PROFILE = ProfileErpModel(
    id = "id",
    name = "name",
    active = true,
    isNewlyCreated = false,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = byteArrayOf(0x00, 0x01, 0x02)
    ),
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = null
    ),
    isConsentDrawerShown = false,
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    lastAuditEventSynced = null,
    lastTaskSynced = null,
    userAuthentication = UserAuthenticationErpModel.NotInitialized
)
