/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.medicationplan.ui.preview.mocks

import de.gematik.ti.erp.app.medicationplan.ui.preview.medicationPlanPreviewCurrentTime
import de.gematik.ti.erp.app.profiles.model.ProfilesData

val PROFILE1 = ProfilesData.Profile(
    id = "PROFILE_ID1",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = null,
    name = "Erna Mustermann",
    insurantName = "Erna Mustermann",
    insuranceIdentifier = "AOK",
    insuranceType = ProfilesData.InsuranceType.GKV,
    isConsentDrawerShown = true,
    lastAuthenticated = medicationPlanPreviewCurrentTime,
    lastTaskSynced = medicationPlanPreviewCurrentTime,
    active = true,
    singleSignOnTokenScope = null
)

val PROFILE2 = PROFILE1.copy(
    id = "PROFILE_ID2",
    name = "Max Mustermann",
    avatar = ProfilesData.Avatar.Grandfather
)