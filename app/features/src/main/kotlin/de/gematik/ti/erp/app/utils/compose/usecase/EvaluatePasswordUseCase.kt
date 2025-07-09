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

package de.gematik.ti.erp.app.utils.compose.usecase

import com.nulabinc.zxcvbn.Zxcvbn
import de.gematik.ti.erp.app.utils.compose.PasswordEvaluation
import de.gematik.ti.erp.app.utils.compose.PasswordScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EvaluatePasswordUseCase(
    private val zxcvbn: Zxcvbn,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Suppress("MagicNumber")
    suspend operator fun invoke(password: String): PasswordEvaluation = withContext(dispatcher) {
        val passwordStrengthEvaluation = zxcvbn.measure(password)

        when (passwordStrengthEvaluation.score) {
            0 -> PasswordEvaluation(PasswordScore.Uninitialised, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
            1 -> PasswordEvaluation(PasswordScore.VeryWeak, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
            2 -> PasswordEvaluation(PasswordScore.Weak, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
            3 -> PasswordEvaluation(PasswordScore.Strong, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
            4 -> PasswordEvaluation(PasswordScore.VeryStrong, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
            else -> PasswordEvaluation(PasswordScore.Uninitialised, passwordStrengthEvaluation.feedback.suggestions.joinToString("\n").trim())
        }
    }
}
