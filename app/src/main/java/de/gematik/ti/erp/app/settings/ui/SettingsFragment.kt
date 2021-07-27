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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.gematik.ti.erp.app.core.ComposeBaseFragment

enum class SettingsScrollTo {
    None,
    Authentication,
    DemoMode
}

@AndroidEntryPoint
class SettingsFragment : ComposeBaseFragment() {
    private val settingsViewModel by viewModels<SettingsViewModel>()
    private val navArgs: SettingsFragmentArgs by navArgs()

    override val content = @Composable {
        SettingsScreen(navArgs.scrollTo, settingsViewModel)
    }
}
