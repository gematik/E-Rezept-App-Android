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

package de.gematik.ti.erp.app.redeem.model

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.features.R

@Requirement(
    "A_20085#3",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Error messages on redeem prescriptions are localized"
)
@Requirement(
    "O.Plat_4#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "String resources are used tp show the mapped errors."
)
sealed class RedeemPrescriptionDialogMessageState(
    open val redeemDialogParameters: RedeemDialogParameters
) {
    data class Success(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_200_title,
            description = R.string.server_return_code_200
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class IncorrectDataStructure(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_400_title,
            description = R.string.server_return_code_400
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class JsonViolated(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_title_failure,
            description = R.string.server_return_code_401
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class UnableToRedeem(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_title_failure,
            description = R.string.server_return_code_404
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class Timeout(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_408_title,
            description = R.string.server_return_code_408
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class Conflict(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_409_title,
            description = R.string.server_return_code_409
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class Gone(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_410_title,
            description = R.string.server_return_code_410
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class NotFound(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_no_code_title,
            description = R.string.server_return_no_code
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class Unknown(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_no_code_title,
            description = R.string.server_return_no_code
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    data class MultiplePrescriptionsFailed(
        override val redeemDialogParameters: RedeemDialogParameters = RedeemDialogParameters(
            title = R.string.server_return_code_title_failure,
            description = R.string.several_return_code
        )
    ) : RedeemPrescriptionDialogMessageState(redeemDialogParameters)

    companion object {
        fun RedeemedPrescriptionState.toDialogMessageState(): RedeemPrescriptionDialogMessageState {
            return when (this) {
                is RedeemedPrescriptionState.Success -> Success()
                is RedeemedPrescriptionState.Error -> errorState.toDialogMessageState()
                else -> Unknown()
            }
        }

        private fun HttpErrorState.toDialogMessageState(): RedeemPrescriptionDialogMessageState =
            when (this) {
                HttpErrorState.BadRequest -> IncorrectDataStructure()
                HttpErrorState.Conflict -> Conflict()
                HttpErrorState.Gone -> Gone()
                HttpErrorState.Unauthorized -> JsonViolated()
                HttpErrorState.RequestTimeout -> Timeout()
                HttpErrorState.NotFound -> NotFound()
                // TODO: Add more ui states
                HttpErrorState.ServerError, HttpErrorState.TooManyRequest, HttpErrorState.MethodNotAllowed, HttpErrorState.Forbidden,
                is HttpErrorState.ErrorWithCause, HttpErrorState.Unknown -> Unknown()
            }
    }
}
