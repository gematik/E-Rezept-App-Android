/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.vau

import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.interceptor.DefaultCryptoConfig
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import de.gematik.ti.erp.app.vau.repository.VauLocalDataSource
import de.gematik.ti.erp.app.vau.repository.VauRemoteDataSource
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststore
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststoreProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreTimeSourceProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.cert.X509CertificateHolder
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import kotlin.time.Duration

const val NetworkSecurePreferencesTag = "NetworkSecurePreferences"

val vauModule = DI.Module("vauModule") {
    bindSingleton {
        val endpointHelper = instance<EndpointHelper>()
        TruststoreConfig(endpointHelper::getTrustAnchor)
    }
    bindSingleton { VauRemoteDataSource(instance()) }
    bindSingleton { VauLocalDataSource(instance()) }
    bindSingleton { VauRepository(instance(), instance(), instance()) }
    bindSingleton { DefaultCryptoConfig() }
    bindSingleton {
        VauChannelInterceptor(
            endpointHelper = instance(),
            truststore = instance(),
            cryptoConfig = instance(),
            networkSecPrefs = instance(NetworkSecurePreferencesTag),
            sessionLog = instance()
        )
    }
    bindSingleton<TruststoreTimeSourceProvider> { { Clock.System.now() } }
    bindSingleton<TrustedTruststoreProvider> {
        {
                untrustedOCSPList: UntrustedOCSPList,
                untrustedCertList: UntrustedCertList,
                trustAnchor: X509CertificateHolder,
                ocspResponseMaxAge: Duration,
                timestamp: Instant ->
            TrustedTruststore.create(
                untrustedOCSPList = untrustedOCSPList,
                untrustedCertList = untrustedCertList,
                trustAnchor = trustAnchor,
                ocspResponseMaxAge = ocspResponseMaxAge,
                timestamp = timestamp
            )
        }
    }
    bindSingleton { TruststoreUseCase(instance(), instance(), instance(), instance()) }
}
