/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.usecase.activeProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import org.kodein.di.compose.rememberInstance

@Stable
class PrescriptionState(
    prescriptionUseCase: PrescriptionUseCase,
    profileId: ProfileIdentifier
) {
    private val prescriptionFlow = combine(
        prescriptionUseCase.scannedActiveRecipes(profileId),
        prescriptionUseCase.syncedActiveRecipes(profileId)
    ) { lowDetail, fullDetail ->
        (lowDetail + fullDetail)
    }

    private val stateFlow: Flow<PrescriptionScreenData.State> =
        combine(
            prescriptionFlow,
            prescriptionUseCase.redeemedPrescriptions(profileId)
        ) { prescriptions, redeemed ->
            PrescriptionScreenData.State(
                prescriptions = prescriptions,
                redeemedPrescriptions = redeemed
            )
        }.distinctUntilChanged()

    val state
        @Composable
        get() = stateFlow.collectAsState(PrescriptionScreenData.EmptyState)
}

@Composable
fun rememberPrescriptionState(): PrescriptionState {
    val prescriptionUseCase by rememberInstance<PrescriptionUseCase>()
    val activeProfile = LocalProfileHandler.current.activeProfile

    return rememberSaveable(activeProfile.id, saver = complexAutoSaver()) {
        PrescriptionState(
            prescriptionUseCase = prescriptionUseCase,
            profileId = activeProfile.id
        )
    }
}
