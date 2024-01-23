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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.Requirement

interface PrescriptionServiceState

@Requirement(
    "O.Source_3",
    "O.Source_4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Error messages are localized using the `PrescriptionServiceErrorState " +
        "Search for `PrescriptionServiceErrorState`or State.Error to see all instances." +
        "Most errors are localized with static text. Logging is only active on debug builds."
)
interface PrescriptionServiceErrorState : PrescriptionServiceState

@Stable
sealed interface GeneralErrorState : PrescriptionServiceErrorState {
    object NetworkNotAvailable : GeneralErrorState
    class ServerCommunicationFailedWhileRefreshing(val code: Int) : GeneralErrorState
    object FatalTruststoreState : GeneralErrorState
    object NoneEnrolled : GeneralErrorState
    object UserNotAuthenticated : GeneralErrorState
}

@Immutable
data class RefreshedState(val nrOfNewPrescriptions: Int) : PrescriptionServiceState
