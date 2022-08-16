/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.attestation

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import de.gematik.ti.erp.app.attestation.repository.AttestationLocalDataSource
import de.gematik.ti.erp.app.attestation.repository.AttestationRemoteDataSource
import de.gematik.ti.erp.app.attestation.repository.SafetynetAttestationRepository
import de.gematik.ti.erp.app.attestation.usecase.SafetynetUseCase
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val attestationModule = DI.Module("attestationModule") {
    bindSingleton { SafetyNet.getClient(instance<Context>()) }

    bindSingleton { SafetynetAttestation(instance(), instance()) }
    bindSingleton { SafetyNetAttestationReportGenerator() }
    bindSingleton { AttestationLocalDataSource(instance()) }
    bindSingleton { AttestationRemoteDataSource(instance()) }
    bindSingleton { SafetynetAttestationRepository(instance(), instance()) }
    bindSingleton { SafetynetUseCase(instance(), instance(), instance()) }
}
