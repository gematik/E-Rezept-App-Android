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

package de.gematik.ti.erp.app.appsecurity.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.appsecurity.usecase.SetShouldShowAndroid13DeprecationWarningUseCase
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import kotlin.getValue

class Android13DeprecationController(
    private val setAndroid13DeprecationWarningSeenUseCase: SetShouldShowAndroid13DeprecationWarningUseCase
) : Controller() {

    fun hasSeenDeprecationWarning() {
        controllerScope.launch {
            setAndroid13DeprecationWarningSeenUseCase(false)
        }
    }
}

@Composable
fun rememberAndroid13DeprecationController(): Android13DeprecationController {
    val setAndroid13DeprecationOverrideUseCase: SetShouldShowAndroid13DeprecationWarningUseCase by rememberInstance()

    return remember {
        Android13DeprecationController(
            setAndroid13DeprecationWarningSeenUseCase = setAndroid13DeprecationOverrideUseCase
        )
    }
}
