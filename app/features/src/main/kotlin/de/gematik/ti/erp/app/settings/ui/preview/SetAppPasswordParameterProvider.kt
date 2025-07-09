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

package de.gematik.ti.erp.app.settings.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.PasswordEvaluation
import de.gematik.ti.erp.app.utils.compose.PasswordScore
import de.gematik.ti.erp.app.utils.compose.presentation.PasswordFieldsData

data class SetAppPasswordParameter(
    val name: String,
    val passwordFieldsState: PasswordFieldsData
)

class SetAppPasswordParameterProvider : PreviewParameterProvider<SetAppPasswordParameter> {
    override val values: Sequence<SetAppPasswordParameter>
        get() = sequenceOf(
            SetAppPasswordParameter(
                name = "Weak",
                passwordFieldsState = PasswordFieldsData(
                    password = "password",
                    repeatedPassword = "password",
                    passwordEvaluation = PasswordEvaluation(
                        PasswordScore.Weak,
                        "password is weak"
                    ),
                    passwordIsValidAndConsistent = false,
                    repeatedPasswordHasError = false
                )
            ),
            SetAppPasswordParameter(
                name = "Strong",
                passwordFieldsState = PasswordFieldsData(
                    password = "password123",
                    repeatedPassword = "password123",
                    passwordEvaluation = PasswordEvaluation(
                        PasswordScore.Strong,
                        "password123 is strong"
                    ),
                    passwordIsValidAndConsistent = true,
                    repeatedPasswordHasError = false
                )
            ),
            SetAppPasswordParameter(
                name = "VeryStrong",
                passwordFieldsState = PasswordFieldsData(
                    password = "passwordIsVERYStrong",
                    repeatedPassword = "passwordIsVERYStrong",
                    passwordEvaluation = PasswordEvaluation(
                        PasswordScore.VeryStrong,
                        "password is very Strong"
                    ),
                    passwordIsValidAndConsistent = true,
                    repeatedPasswordHasError = false
                )
            ),
            SetAppPasswordParameter(
                name = "InConsistent",
                passwordFieldsState = PasswordFieldsData(
                    password = "1234",
                    repeatedPassword = "0123",
                    passwordEvaluation = PasswordEvaluation(
                        PasswordScore.VeryWeak,
                        "password is very weak"
                    ),
                    passwordIsValidAndConsistent = false,
                    repeatedPasswordHasError = true
                )
            )
        )
}
