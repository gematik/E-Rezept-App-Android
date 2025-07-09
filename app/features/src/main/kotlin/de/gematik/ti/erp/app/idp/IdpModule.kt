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

package de.gematik.ti.erp.app.idp

import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.idp.repository.AccessTokenDataSource
import de.gematik.ti.erp.app.idp.repository.DefaultIdpRepository
import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.AuthenticateWithExternalHealthInsuranceAppUseCase
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.idp.usecase.DefaultIdpUseCase
import de.gematik.ti.erp.app.idp.usecase.GetHealthInsuranceAppIdpsUseCase
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpAlternateAuthenticationUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpBasicUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpCryptoProvider
import de.gematik.ti.erp.app.idp.usecase.IdpDeviceInfoProvider
import de.gematik.ti.erp.app.idp.usecase.IdpPreferenceProvider
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.idp.usecase.RemoveAuthenticationUseCase
import kotlinx.coroutines.sync.Mutex
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

const val NetworkSecurePreferencesTag = "NetworkSecurePreferences"
private const val IdpLockTag = "IdpLockTag"

val idpModule = DI.Module("idpModule") {
    bindProvider { IdpLocalDataSource(instance()) }
    bindProvider { IdpPairingRepository(instance()) }
    bindProvider {
        val endpointHelper = instance<EndpointHelper>()
        IdpRemoteDataSource(instance()) { endpointHelper.getIdpScope() }
    }
    bindProvider { IdpCryptoProvider() }
    bindProvider { IdpDeviceInfoProvider() }
    bindProvider {
        IdpPreferenceProvider().apply {
            sharedPreferences = instance(NetworkSecurePreferencesTag)
        }
    }
    bindSingleton { AccessTokenDataSource() }
    bindProvider<IdpRepository> { DefaultIdpRepository(instance(), instance(), instance()) }
}

val idpUseCaseModule = DI.Module("idpUseCaseModule", allowSilentOverride = true) {
    bindSingleton(IdpLockTag) { Mutex() }
    bindSingleton { IdpBasicUseCase(instance(), instance()) }
    bindSingleton<IdpUseCase> {
        DefaultIdpUseCase(
            repository = instance(),
            pairingRepository = instance(),
            altAuthUseCase = instance(),
            profilesRepository = instance(),
            basicUseCase = instance(),
            cryptoProvider = instance(),
            lock = instance(IdpLockTag)
        )
    }
    bindProvider { IdpAlternateAuthenticationUseCase(instance(), instance(), instance()) }
    bindProvider { GetHealthInsuranceAppIdpsUseCase(instance(), instance()) }
    bindProvider { GetUniversalLinkForHealthInsuranceAppsUseCase(instance(), instance(), instance()) }
    bindProvider {
        AuthenticateWithExternalHealthInsuranceAppUseCase(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(IdpLockTag)
        )
    }
    bindProvider { RemoveAuthenticationUseCase(instance()) }
    bindProvider { ChooseAuthenticationDataUseCase(instance(), instance()) }
}
