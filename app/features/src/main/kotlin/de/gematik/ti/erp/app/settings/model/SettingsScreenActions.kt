/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.settings.model

import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

data class SettingsActions(
    val healthCardClickActions: HealthCardClickActions,
    val legalClickActions: LegalClickActions,
    val debugClickActions: DebugClickActions,
    val exploreClickActions: ExploreClickActions,
    val contactClickActions: ContactClickActions,
    val personalSettingsClickActions: PersonalSettingsClickActions,
    val onClickEditProfile: (ProfileIdentifier) -> Unit
)

data class HealthCardClickActions(
    val onClickUnlockEgk: (UnlockMethod) -> Unit,
    val onClickOrderHealthCard: () -> Unit
)

data class LegalClickActions(
    val onClickLegalNotice: () -> Unit,
    val onClickDataProtection: () -> Unit,
    val onClickOpenSourceLicences: () -> Unit,
    val onClickAdditionalLicences: () -> Unit,
    val onClickTermsOfUse: () -> Unit
)

data class ExploreClickActions(
    val onClickOrganDonationRegister: () -> Unit,
    val onToggleDemoMode: () -> Unit,
    val onClickForum: () -> Unit,
    val onClickGesundBund: () -> Unit
)

data class ContactClickActions(
    val onClickPoll: () -> Unit,
    val onClickMail: () -> Unit,
    val onClickCall: () -> Unit
)

data class PersonalSettingsClickActions(
    val onToggleEnableZoom: (Boolean) -> Unit,
    val onToggleScreenshots: (Boolean) -> Unit,
    val onClickProductImprovementSettings: () -> Unit,
    val onClickDeviceSecuritySettings: () -> Unit,
    val onClickLanguageSettings: () -> Unit,
    val onClickMedicationPlan: () -> Unit
)

data class DebugClickActions(
    val onClickDebug: () -> Unit,
    val onClickBottomSheetShowcase: () -> Unit,
    val onClickDemoTracking: () -> Unit
)
