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

package de.gematik.ti.erp.app.utils.compose.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.io.Resource
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.utils.compose.PasswordEvaluation
import de.gematik.ti.erp.app.utils.compose.PasswordScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

const val TIMEOUT = 5000L

open class PasswordFieldsController(
    private val passwordStrengthEvaluator: Zxcvbn
) : Controller() {
    private val _password = MutableStateFlow("")
    private val _repeatedPassword = MutableStateFlow("")

    val passwordFieldsState = combine(
        _password,
        _repeatedPassword
    ) {
            password, repeatedPassword ->
        val passwordEvaluation = evaluatePassword(password)
        PasswordFieldsData(
            password = password,
            repeatedPassword = repeatedPassword,
            passwordEvaluation = passwordEvaluation,
            repeatedPasswordHasError = password.isNotBlank() && repeatedPassword.isNotBlank() && !password.startsWith(repeatedPassword),
            passwordIsValidAndConsistent = password.isNotBlank() && password == repeatedPassword && passwordEvaluation.isStrongEnough
        )
    }.stateIn(
        controllerScope,
        SharingStarted.WhileSubscribed(TIMEOUT),
        PasswordFieldsData(
            "",
            "",
            PasswordEvaluation(
                PasswordScore.Uninitialised,
                ""
            ),
            false,
            false
        )
    )

    @Suppress("MagicNumber")
    private fun evaluatePassword(password: String): PasswordEvaluation {
        val passwordStrengthEvaluation = passwordStrengthEvaluator.measure(password)
        return when (passwordStrengthEvaluation.score) {
            0 -> PasswordEvaluation(
                PasswordScore.Uninitialised,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
            1 -> PasswordEvaluation(
                PasswordScore.VeryWeak,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
            2 -> PasswordEvaluation(
                PasswordScore.Weak,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
            3 -> PasswordEvaluation(
                PasswordScore.Strong,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
            4 -> PasswordEvaluation(
                PasswordScore.VeryStrong,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
            else -> PasswordEvaluation(
                PasswordScore.Uninitialised,
                passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim()
            )
        }
    }

    fun onPasswordChange(password: String) {
        _password.update { password }
        _repeatedPassword.update { "" }
    }

    fun onRepeatedPasswordChange(repeatedPassword: String) {
        _repeatedPassword.update { repeatedPassword }
    }
}

data class PasswordFieldsData(
    val password: String,
    val repeatedPassword: String,
    val passwordEvaluation: PasswordEvaluation,
    val repeatedPasswordHasError: Boolean,
    val passwordIsValidAndConsistent: Boolean
)

@Composable
fun rememberPasswordFieldsController(): PasswordFieldsController {
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
        PasswordFieldsController(
            passwordStrengthEvaluator = passwordStrengthEvaluator
        )
    }
}
