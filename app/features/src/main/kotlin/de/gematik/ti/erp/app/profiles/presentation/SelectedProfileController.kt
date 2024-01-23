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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetSelectedProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance

class SelectedProfileController(
    private val profileId: ProfileIdentifier?,
    private val getSelectedProfile: GetSelectedProfileUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val scope: CoroutineScope
) {

    private val selectedProfile by lazy {
        profileId?.let {
            getSelectedProfile(id = it).stateIn(scope, SharingStarted.Lazily, DEFAULT_EMPTY_PROFILE)
        } ?: run { MutableStateFlow(DEFAULT_EMPTY_PROFILE) }
    }

    private val activeProfile by lazy {
        getActiveProfileUseCase().stateIn(scope, SharingStarted.Lazily, DEFAULT_EMPTY_PROFILE)
    }

    val selectedProfileState
        @Composable
        get() = selectedProfile.collectAsStateWithLifecycle()

    val activeProfileState
        @Composable
        get() = activeProfile.collectAsStateWithLifecycle()
}

@Composable
fun rememberSelectedProfileController(
    profileId: ProfileIdentifier? = null
): SelectedProfileController {
    val getSelectedProfile by rememberInstance<GetSelectedProfileUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        SelectedProfileController(
            profileId = profileId,
            getSelectedProfile = getSelectedProfile,
            getActiveProfileUseCase = getActiveProfileUseCase,
            scope = scope
        )
    }
}
