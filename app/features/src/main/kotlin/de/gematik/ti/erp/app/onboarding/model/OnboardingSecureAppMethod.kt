/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.onboarding.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.settings.ui.checkPassword
import kotlinx.parcelize.Parcelize
@Immutable
sealed class OnboardingSecureAppMethod {
    @Immutable
    @Parcelize
    data class Password(val password: String, val repeatedPassword: String, val score: Int) :
        OnboardingSecureAppMethod(),
        Parcelable {
        val checkedPassword: String?
            get() =
                if (checkPassword(password, repeatedPassword, score)) {
                    password
                } else {
                    null
                }
    }

    @Parcelize
    object DeviceSecurity : OnboardingSecureAppMethod(), Parcelable

    @Parcelize
    object None : OnboardingSecureAppMethod(), Parcelable
}
