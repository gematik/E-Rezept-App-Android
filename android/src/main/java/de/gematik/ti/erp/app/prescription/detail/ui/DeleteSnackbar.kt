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

package de.gematik.ti.erp.app.prescription.detail.ui

import android.content.Context
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.ui.GeneralErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState

fun deleteErrorMessage(context: Context, deleteState: PrescriptionServiceErrorState): String? =
    when (deleteState) {
        GeneralErrorState.NetworkNotAvailable ->
            context.getString(R.string.error_message_network_not_available)
        is GeneralErrorState.ServerCommunicationFailedWhileRefreshing ->
            context.getString(R.string.error_message_server_communication_failed).format(deleteState.code)
        GeneralErrorState.FatalTruststoreState ->
            context.getString(R.string.error_message_vau_error)
        is DeletePrescriptions.State.Error.PrescriptionWorkflowBlocked ->
            context.getString(R.string.logout_delete_in_progress)
        is DeletePrescriptions.State.Error.PrescriptionNotFound ->
            context.getString(R.string.prescription_not_found)
        is GeneralErrorState.NoneEnrolled ->
            context.getString(R.string.no_auth_enrolled)
        else -> null
    }
