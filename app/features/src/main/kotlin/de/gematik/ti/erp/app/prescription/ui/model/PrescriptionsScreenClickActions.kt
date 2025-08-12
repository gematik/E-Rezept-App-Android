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

package de.gematik.ti.erp.app.prescription.ui.model

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

data class PrescriptionsScreenContentClickAction(
    val onClickLogin: (ProfilesUseCaseData.Profile) -> Unit,
    val onClickAvatar: (ProfilesUseCaseData.Profile) -> Unit,
    val onClickArchive: () -> Unit,
    val onClickPrescription: (String, Boolean, Boolean) -> Unit,
    val onChooseAuthenticationMethod: (ProfilesUseCaseData.Profile) -> Unit,
    val onClickRedeem: () -> Unit,
    val onClickRefresh: () -> Unit
)

data class MultiProfileTopAppBarClickAction(
    val onClickAddProfile: () -> Unit,
    val onClickChangeProfileName: (ProfilesUseCaseData.Profile) -> Unit,
    val onClickAddScannedPrescription: () -> Unit,
    val onSwitchActiveProfile: (ProfilesUseCaseData.Profile) -> Unit,
    val onElevateTopAppBar: (Boolean) -> Unit
)

data class ConsentClickAction(
    val onGetChargeConsent: (ProfileIdentifier) -> Unit
)
