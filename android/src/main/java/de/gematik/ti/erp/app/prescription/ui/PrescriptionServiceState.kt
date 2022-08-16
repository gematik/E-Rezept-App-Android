/*
 * Copyright (c) 2022 gematik GmbH
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

interface PrescriptionServiceState

interface PrescriptionServiceErrorState : PrescriptionServiceState

@Stable
sealed interface GenerellErrorState : PrescriptionServiceErrorState {
    object NetworkNotAvailable : GenerellErrorState
    class ServerCommunicationFailedWhileRefreshing(val code: Int) : GenerellErrorState
    object FatalTruststoreState : GenerellErrorState
    object NoneEnrolled : GenerellErrorState
    object UserNotAuthenticated : GenerellErrorState
}

@Immutable
data class RefreshedState(val nrOfNewPrescriptions: Int) : PrescriptionServiceState
