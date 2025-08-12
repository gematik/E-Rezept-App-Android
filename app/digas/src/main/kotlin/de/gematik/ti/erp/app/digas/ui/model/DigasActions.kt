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

package de.gematik.ti.erp.app.digas.ui.model

import de.gematik.ti.erp.app.diga.model.DigaStatus

data class DigasActions(
    val onClickOnReady: (isRedeemAgain: Boolean) -> Unit = {},
    val onClickOnOpenAppWithRedeemCode: () -> Unit = {},
    val onClickOnCompletedSuccessfully: () -> Unit = {},
    val onClickRefresh: () -> Unit = {},
    val onClickOnReadyForSelfArchive: (currentStatus: DigaStatus?) -> Unit = {},
    val onClickOnArchiveRevert: (currentStatus: DigaStatus?) -> Unit = {},
    val onClickCopy: () -> Unit = {},
    val onClickDelete: () -> Unit = {},
    val onShowHowLongValidBottomSheet: () -> Unit = {},
    val onShowSupportBottomSheet: () -> Unit = {},
    val onNavigateToDescriptionScreen: () -> Unit = {},
    val onNavigateToPatient: () -> Unit = {},
    val onNavigateToPractitioner: () -> Unit = {},
    val onNavigateToOrganization: () -> Unit = {},
    val onNavigateToTechnicalInformation: () -> Unit = {},
    val onClickOnDigaOpen: () -> Unit = {},
    val onRegisterFeedBack: () -> Unit = {},
    val onNavigatetoBfarm: () -> Unit = {},
    val onNavigateToInsuranceSearch: () -> Unit = {},
    val onShowContributionInfoBottomSheet: () -> Unit = {}
)
