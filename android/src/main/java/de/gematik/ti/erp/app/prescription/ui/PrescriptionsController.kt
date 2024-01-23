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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.prescription.usecase.GetActivePrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedPrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import kotlinx.coroutines.flow.Flow
import org.kodein.di.compose.rememberInstance

@Stable
class PrescriptionsController(
    private val activePrescriptionsUseCase: GetActivePrescriptionsUseCase,
    private val archivedPrescriptionsUseCase: GetArchivedPrescriptionsUseCase,
    private val profileId: ProfileIdentifier
) {
    private val activePrescriptions: Flow<List<Prescription>>
        get() = activePrescriptionsUseCase(profileId)
    private val archivedPrescriptions: Flow<List<Prescription>>
        get() = archivedPrescriptionsUseCase(profileId)
    val activePrescriptionsState
        @Composable
        get() = activePrescriptions.collectAsState(emptyList())
    val archivedPrescriptionsState
        @Composable
        get() = archivedPrescriptions.collectAsState(emptyList())
}

@Composable
fun rememberPrescriptionsController(): PrescriptionsController {
    val activePrescriptionsUseCase by rememberInstance<GetActivePrescriptionsUseCase>()
    val archivedPrescriptionsUseCase by rememberInstance<GetArchivedPrescriptionsUseCase>()
    val activeProfile = LocalProfileHandler.current.activeProfile

    return rememberSaveable(activeProfile.id, saver = complexAutoSaver()) {
        PrescriptionsController(
            activePrescriptionsUseCase = activePrescriptionsUseCase,
            archivedPrescriptionsUseCase = archivedPrescriptionsUseCase,
            profileId = activeProfile.id
        )
    }
}
