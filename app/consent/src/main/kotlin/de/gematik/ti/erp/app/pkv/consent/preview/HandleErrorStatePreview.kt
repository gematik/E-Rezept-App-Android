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

package de.gematik.ti.erp.app.pkv.consent.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.pkv.consent.model.ConsentViewState

class HandleErrorStatePreviewParameterProvider : PreviewParameterProvider<ConsentErrorPreviewData> {
    override val values: Sequence<ConsentErrorPreviewData> = sequenceOf(
        ConsentErrorPreviewData(
            name = "RequestTimeout",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.RequestTimeout
            )
        ),
        ConsentErrorPreviewData(
            name = "ServerError",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.ServerError
            )
        ),
        ConsentErrorPreviewData(
            name = "TooManyRequest",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.TooManyRequest
            )
        ),
        ConsentErrorPreviewData(
            name = "BadRequest",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.BadRequest
            )
        ),
        ConsentErrorPreviewData(
            name = "Forbidden",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.Forbidden
            )
        ),
        ConsentErrorPreviewData(
            name = "Unauthorized",
            viewState = ConsentViewState(
                state = ConsentState.ValidState.Loading,
                errorState = HttpErrorState.Unauthorized
            )
        )
    )
}

data class ConsentErrorPreviewData(
    val name: String,
    val viewState: ConsentViewState
)
