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

package de.gematik.ti.erp.app.demomode.model

import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.datetime.Instant
import java.util.UUID

data class DemoModeProfile(
    val demoModeId: UUID,
    val id: ProfileIdentifier,
    val color: ProfilesData.ProfileColorNames,
    val avatar: ProfilesData.Avatar,
    val personalizedImage: ByteArray? = null,
    val name: String,
    val insurantName: String? = null,
    val insuranceIdentifier: String? = null,
    val insuranceName: String? = null,
    val insuranceType: ProfilesData.InsuranceType,
    val lastAuthenticated: Instant? = null,
    val lastAuditEventSynced: Instant? = null,
    val lastTaskSynced: Instant? = null,
    val active: Boolean = false,
    val singleSignOnTokenScope: IdpData.SingleSignOnTokenScope?
)

fun MutableList<DemoModeProfile>.toProfiles() = map(DemoModeProfile::toProfile).toMutableList()

fun DemoModeProfile.toProfile() = ProfilesData.Profile(
    id = id,
    color = color,
    avatar = avatar,
    personalizedImage = personalizedImage,
    name = name,
    insurantName = insurantName,
    insuranceIdentifier = insuranceIdentifier,
    insuranceName = insuranceName,
    insuranceType = insuranceType,
    lastAuthenticated = lastAuthenticated,
    lastAuditEventSynced = lastAuditEventSynced,
    lastTaskSynced = lastTaskSynced,
    active = active,
    isConsentDrawerShown = true,
    singleSignOnTokenScope = singleSignOnTokenScope
)
