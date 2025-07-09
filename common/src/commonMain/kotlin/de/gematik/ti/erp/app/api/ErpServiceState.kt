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

package de.gematik.ti.erp.app.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.Requirement

interface ErpServiceState

@Requirement(
    "O.Source_3#1",
    "O.Source_4#1",
    "O.Plat_4#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Error messages are localized using the `ErpServiceErrorState` " +
        "Search for `ErpServiceErrorState`or State.Error to see all instances." +
        "Most errors are localized with static text. Logging is only active on debug builds."
)
@Requirement(
    "A_19937#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Local mapping of errors"
)
@Deprecated("Use HttpErrorState instead")
interface ErpServiceErrorState : ErpServiceState

@Deprecated("Use HttpErrorState instead")
@Stable
sealed interface GeneralErrorState : ErpServiceErrorState {
    data object NetworkNotAvailable : GeneralErrorState
    class ServerCommunicationFailedWhileRefreshing(val code: Int) : GeneralErrorState
    data object FatalTruststoreState : GeneralErrorState
    data object NoneEnrolled : GeneralErrorState
    data object UserNotAuthenticated : GeneralErrorState
    data object RedirectUrlForExternalAuthenticationWrong : GeneralErrorState
}

@Immutable
data class RefreshedState(val nrOfNewPrescriptions: Int) : ErpServiceState
