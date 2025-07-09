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

package de.gematik.ti.erp.app.utils.compose.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nulabinc.zxcvbn.Zxcvbn
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.utils.compose.PasswordEvaluation
import de.gematik.ti.erp.app.utils.compose.PasswordEvaluatorHolder
import de.gematik.ti.erp.app.utils.compose.PasswordScore
import de.gematik.ti.erp.app.utils.compose.usecase.EvaluatePasswordUseCase
import de.gematik.ti.erp.app.utils.compose.usecase.ObservePasswordEvaluationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

const val TIMEOUT = 5000L

open class PasswordFieldsController(
    observePasswordEvaluationUseCase: ObservePasswordEvaluationUseCase
) : Controller() {
    private val _password = MutableStateFlow("")
    private val _repeatedPassword = MutableStateFlow("")

    private val passwordEvaluation = observePasswordEvaluationUseCase(_password)
        .stateIn(
            controllerScope,
            SharingStarted.WhileSubscribed(TIMEOUT),
            PasswordEvaluation(PasswordScore.Uninitialised, "")
        )

    val passwordFieldsState = combine(
        _password,
        _repeatedPassword,
        passwordEvaluation
    ) { password, repeatedPassword, passwordEval ->
        PasswordFieldsData(
            password = password,
            repeatedPassword = repeatedPassword,
            passwordEvaluation = passwordEval,
            repeatedPasswordHasError = password.isNotBlank() &&
                repeatedPassword.isNotBlank() &&
                !password.startsWith(repeatedPassword),
            passwordIsValidAndConsistent = password.isNotBlank() &&
                password == repeatedPassword &&
                passwordEval.isStrongEnough
        )
    }.stateIn(
        controllerScope,
        SharingStarted.WhileSubscribed(TIMEOUT),
        PasswordFieldsData(
            "",
            "",
            PasswordEvaluation(PasswordScore.Uninitialised, ""),
            false,
            false
        )
    )

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
fun rememberPasswordFieldsController(): PasswordFieldsController? {
    val context = LocalContext.current

    val passwordEvaluatorState by produceState<Zxcvbn?>(initialValue = null, context) {
        value = PasswordEvaluatorHolder.getInstance(context)
    }

    return passwordEvaluatorState?.let { zxcvbn ->
        remember(zxcvbn) {
            val evaluatePasswordUseCase = EvaluatePasswordUseCase(zxcvbn)
            val observePasswordEvaluationUseCase = ObservePasswordEvaluationUseCase(evaluatePasswordUseCase)
            PasswordFieldsController(observePasswordEvaluationUseCase)
        }
    }
}
