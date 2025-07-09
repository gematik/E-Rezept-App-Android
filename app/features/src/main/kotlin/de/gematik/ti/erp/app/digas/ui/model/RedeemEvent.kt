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

import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.error.code.DigaErrorCode
import de.gematik.ti.erp.app.error.diga.DigaErrorCodeMapper

sealed interface RedeemEvent {
    data object Success : RedeemEvent
    data object SuccessAgain : RedeemEvent
    data object AlreadyRedeemed : RedeemErrorEvent
    data object DirectoryError : RedeemErrorEvent
    data object LocalError : RedeemErrorEvent
    data class HttpError(val error: HttpErrorState) : RedeemErrorEvent
}

sealed interface RedeemErrorEvent : RedeemEvent

sealed interface RedeemDialogEvent {
    data object Success : RedeemDialogEvent
    data object SuccessAgain : RedeemDialogEvent
    data object DirectoryError : RedeemDialogEvent
    data class GenericError(val errorCode: String) : RedeemDialogEvent

    companion object {
        fun RedeemEvent.toDialogEvent(): RedeemDialogEvent = when (this) {
            RedeemEvent.Success -> Success
            RedeemEvent.SuccessAgain -> SuccessAgain
            RedeemEvent.DirectoryError -> DirectoryError
            RedeemEvent.AlreadyRedeemed -> GenericError(
                DigaErrorCodeMapper.buildErrorCode(
                    DigaErrorCode.ALREADY_REDEEMED_ERROR
                )
            )

            is RedeemEvent.HttpError -> {
                GenericError(
                    DigaErrorCodeMapper.buildErrorCode(
                        DigaErrorCode.httpError(error.errorCode)
                    )
                )
            }

            RedeemEvent.LocalError -> GenericError(
                DigaErrorCodeMapper.buildErrorCode(
                    DigaErrorCode.DATABASE_ERROR
                )
            )
        }
    }
}
