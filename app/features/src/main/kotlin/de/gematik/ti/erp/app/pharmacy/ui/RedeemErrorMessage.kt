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

package de.gematik.ti.erp.app.pharmacy.ui

import android.content.Context
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ErpServiceErrorState
import de.gematik.ti.erp.app.api.GeneralErrorState
import de.gematik.ti.erp.app.features.R

@Requirement(
    "O.Plat_4#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "String resources are used tp show the mapped errors."
)
fun ErpServiceErrorState.redeemErrorMessage(context: Context): String? =
    when (this) {
        GeneralErrorState.NetworkNotAvailable -> context.getString(R.string.error_message_network_not_available)
        is GeneralErrorState.ServerCommunicationFailedWhileRefreshing ->
            context.getString(R.string.error_message_server_communication_failed).format(this.code)

        GeneralErrorState.FatalTruststoreState -> context.getString(R.string.error_message_vau_error)
        // is RedeemPrescriptionState.Unknown -> context.getString(R.string.redeem_online_error_uploading)
        is GeneralErrorState.NoneEnrolled -> context.getString(R.string.no_auth_enrolled)
        else -> null
    }
