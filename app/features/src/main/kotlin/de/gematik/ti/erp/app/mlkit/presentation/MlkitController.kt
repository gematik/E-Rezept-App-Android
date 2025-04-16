/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.mlkit.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.settings.usecase.AcceptMLKitUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class MlKitController(
    private val acceptMLKitUseCase: AcceptMLKitUseCase,
    private val scope: CoroutineScope
) {
    fun acceptMLKit() = scope.launch {
        acceptMLKitUseCase()
    }
}

@Composable
fun rememberMlKitController(): MlKitController {
    val acceptMLKitUseCase by rememberInstance<AcceptMLKitUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        MlKitController(
            acceptMLKitUseCase = acceptMLKitUseCase,
            scope = scope
        )
    }
}
