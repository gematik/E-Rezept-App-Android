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

package de.gematik.ti.erp.app.redeem.model

import androidx.annotation.StringRes
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.core.R

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
enum class RedeemPrescriptionDialogMessageState(
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    SUCCESS(R.string.server_return_code_200_title, R.string.server_return_code_200),
    INCORRECT_DATA_STRUCTURE(R.string.server_return_code_400_title, R.string.server_return_code_400),
    JSON_VIOLATED(R.string.server_return_code_title_failure, R.string.server_return_code_401),
    UNABLE_TO_REDEEM(R.string.server_return_code_title_failure, R.string.server_return_code_404),
    TIMEOUT(R.string.server_return_code_408_title, R.string.server_return_code_408),
    CONFLICT(R.string.server_return_code_409_title, R.string.server_return_code_409),
    GONE(R.string.server_return_code_410_title, R.string.server_return_code_410),
    NOT_FOUND(R.string.prescription_not_found_on_server_title, R.string.prescription_not_found_on_server),
    UNKNOWN(R.string.prescription_unknown_problem_title, R.string.prescription_unknown_problem),
    MULTIPLE_PRESCRIPTIONS_FAILED(R.string.server_return_code_title_failure, R.string.several_return_code);

    @Requirement(
        "O.Plat_4#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "String resources are used tp show the mapped errors."
    )
    companion object {
        fun BaseRedeemState.toDialogMessageState(): RedeemPrescriptionDialogMessageState {
            return when (this) {
                is BaseRedeemState.Success -> SUCCESS
                is BaseRedeemState.Error -> errorState.toDialogMessageState()
                else -> UNKNOWN
            }
        }

        private fun HttpErrorState.toDialogMessageState(): RedeemPrescriptionDialogMessageState =
            when (this) {
                HttpErrorState.BadRequest -> INCORRECT_DATA_STRUCTURE
                HttpErrorState.Conflict -> CONFLICT
                HttpErrorState.Gone -> GONE
                HttpErrorState.Unauthorized -> JSON_VIOLATED
                HttpErrorState.RequestTimeout -> TIMEOUT
                HttpErrorState.NotFound -> NOT_FOUND
                // TODO: Add more ui states
                HttpErrorState.ServerError, HttpErrorState.TooManyRequest, HttpErrorState.MethodNotAllowed, HttpErrorState.Forbidden,
                is HttpErrorState.ErrorWithCause, HttpErrorState.Unknown -> UNKNOWN
            }
    }
}
