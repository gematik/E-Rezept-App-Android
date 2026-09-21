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

package de.gematik.ti.erp.app.demomode.model

import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Instant
import java.util.UUID

data class DemoModeProfile(
    val demoModeId: UUID,
    val id: ProfileIdentifier,
    val color: ProfileColorNames,
    val avatar: Avatar,
    val personalizedImage: ByteArray? = null,
    val name: String,
    val insurantName: String? = null,
    val insuranceIdentifier: String? = null,
    val insuranceName: String? = null,
    val insuranceType: InsuranceType,
    val lastAuthenticated: Instant? = null,
    val lastAuditEventSynced: Instant? = null,
    val lastTaskSynced: Instant? = null,
    val active: Boolean = false,
    val userAuthentication: UserAuthenticationErpModel?
)

fun MutableList<DemoModeProfile>.toProfiles() = map(DemoModeProfile::toProfile).toMutableList()

fun DemoModeProfile.toProfile() = ProfileErpModel(
    id = id,
    name = name,
    profileImageData = ProfileImageDataErpModel(
        image = personalizedImage,
        avatar = avatar,
        color = color
    ),
    isConsentDrawerShown = true,
    insuranceData = ProfileInsuranceDataErpModel(
        insuranceIdentifier = insuranceIdentifier,
        insuranceName = insuranceName,
        insuranceType = insuranceType,
        insurantName = insurantName,
        organizationIdentifier = null
    ),
    lastAuthenticated = lastAuthenticated,
    lastAuditEventSynced = lastAuditEventSynced,
    lastTaskSynced = lastTaskSynced,
    active = active,
    isNewlyCreated = false,
    userAuthentication = userAuthentication ?: UserAuthenticationErpModel.NotInitialized
)
