/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.onboarding.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.gematik.ti.erp.app.core.ComposeBaseFragment
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import javax.inject.Inject

const val TEXT = "text"
const val NEW_USER = "newUser"

@AndroidEntryPoint
class OnboardingFragment : ComposeBaseFragment() {

    @Inject
    @ApplicationPreferences
    lateinit var sharedPreferences: SharedPreferences
    private val settingsViewModel by viewModels<SettingsViewModel>()
    override val content = @Composable { OnboardingScreen(settingsViewModel, ::saveNewUser) }

    private fun saveNewUser() {
        sharedPreferences.edit().putBoolean(NEW_USER, false).apply()
    }

    override fun onResume() {
        super.onResume()

        if (!sharedPreferences.getBoolean(NEW_USER, true)) {
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToMainScreenFragment())
        }
    }
}
