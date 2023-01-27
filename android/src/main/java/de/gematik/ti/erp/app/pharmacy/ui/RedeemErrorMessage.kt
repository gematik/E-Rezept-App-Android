/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import android.content.Context
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.ui.GenerellErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState

fun redeemErrorMessage(context: Context, redeemState: PrescriptionServiceErrorState): String? =
    when (redeemState) {
        GenerellErrorState.NetworkNotAvailable ->
            context.getString(R.string.error_message_network_not_available)
        is GenerellErrorState.ServerCommunicationFailedWhileRefreshing ->
            context.getString(R.string.error_message_server_communication_failed).format(redeemState.code)
        GenerellErrorState.FatalTruststoreState ->
            context.getString(R.string.error_message_vau_error)
        is RedeemPrescriptionsController.State.Error.Unknown ->
            context.getString(R.string.redeem_online_error_uploading)
        is GenerellErrorState.NoneEnrolled ->
            context.getString(R.string.no_auth_enrolled)
        else -> null
    }
