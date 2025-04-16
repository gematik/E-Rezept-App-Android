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

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.io.Resource
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import de.gematik.ti.erp.app.settings.usecase.SetPasswordUseCase
import de.gematik.ti.erp.app.utils.compose.presentation.PasswordFieldsController
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PasswordSettingsController(
    private val setPasswordUseCase: SetPasswordUseCase,
    passwordStrengthEvaluator: Zxcvbn
) : PasswordFieldsController(passwordStrengthEvaluator) {
    fun setAppPassword() = controllerScope.launch {
        setPasswordUseCase.invoke(passwordFieldsState.value.password)
    }
}

@Composable
fun rememberPasswordSettingsController(): PasswordSettingsController {
    val setPasswordUseCase by rememberInstance<SetPasswordUseCase>()

    val context = LocalContext.current
    val assetManager = context.assets
    val germanDictionaryFile = assetManager.open("german_dictionary.txt")
    val germanDictionaryResource = Resource { germanDictionaryFile }
    val passwordStrengthEvaluator = ZxcvbnBuilder().dictionaries(
        StandardDictionaries.loadAllDictionaries()
    )
        .dictionary(DictionaryLoader("german_dictionary", germanDictionaryResource).load())
        .build()

    return remember {
        PasswordSettingsController(
            setPasswordUseCase = setPasswordUseCase,
            passwordStrengthEvaluator = passwordStrengthEvaluator
        )
    }
}
