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

package de.gematik.ti.erp.app.idp

import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.di.NetworkSecurePreferencesTag
import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpAlternateAuthenticationUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpBasicUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpCryptoProvider
import de.gematik.ti.erp.app.idp.usecase.IdpDeviceInfoProvider
import de.gematik.ti.erp.app.idp.usecase.IdpPreferenceProvider
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val idpModule = DI.Module("idpModule") {
    bindProvider { IdpLocalDataSource(instance()) }
    bindProvider { IdpPairingRepository(instance()) }
    bindProvider {
        val endpointHelper = instance<EndpointHelper>()
        IdpRemoteDataSource(instance()) { endpointHelper.getIdpScope() }
    }
    bindProvider { IdpAlternateAuthenticationUseCase(instance(), instance(), instance()) }
    bindProvider { IdpCryptoProvider() }
    bindProvider { IdpDeviceInfoProvider() }
    bindProvider {
        IdpPreferenceProvider().apply {
            sharedPreferences = instance(NetworkSecurePreferencesTag)
        }
    }
    bindSingleton { IdpRepository(instance(), instance()) }
    bindSingleton { IdpBasicUseCase(instance(), instance()) }
    bindSingleton {
        IdpUseCase(
            repository = instance(),
            pairingRepository = instance(),
            altAuthUseCase = instance(),
            profilesRepository = instance(),
            basicUseCase = instance(),
            preferences = instance(),
            cryptoProvider = instance()
        )
    }
}
