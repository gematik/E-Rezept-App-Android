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
@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.prescription.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.prescription.usecase.GetActivePrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedPrescriptionsUseCase
import de.gematik.ti.erp.app.profiles.presentation.ProfileController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance

@Stable
class PrescriptionsController(
    private val activePrescriptionsUseCase: GetActivePrescriptionsUseCase,
    private val archivedPrescriptionsUseCase: GetArchivedPrescriptionsUseCase,
    private val profileId: ProfileIdentifier,
    scope: CoroutineScope
) {
    private val activePrescriptions by lazy {
        activePrescriptionsUseCase(profileId).stateIn(scope, Eagerly, emptyList())
    }

    private val archivedPrescriptions by lazy {
        archivedPrescriptionsUseCase(profileId).stateIn(scope, Eagerly, emptyList())
    }

    val activePrescriptionsState
        @Composable
        get() = activePrescriptions.collectAsStateWithLifecycle(emptyList())
    val archivedPrescriptionsState
        @Composable
        get() = archivedPrescriptions.collectAsStateWithLifecycle(emptyList())
}

@Composable
fun rememberPrescriptionsController(): PrescriptionsController {
    val activePrescriptionsUseCase by rememberInstance<GetActivePrescriptionsUseCase>()
    val archivedPrescriptionsUseCase by rememberInstance<GetArchivedPrescriptionsUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val scope = rememberCoroutineScope()

    val activeProfile by getActiveProfileUseCase().collectAsStateWithLifecycle(DEFAULT_EMPTY_PROFILE)

    return rememberSaveable(activeProfile.id, saver = complexAutoSaver()) {
        PrescriptionsController(
            profileId = activeProfile.id,
            activePrescriptionsUseCase = activePrescriptionsUseCase,
            archivedPrescriptionsUseCase = archivedPrescriptionsUseCase,
            scope = scope
        )
    }
}
