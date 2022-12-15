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

package de.gematik.ti.erp.app.vau.di

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.di.ScopedRealm
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.interceptor.DefaultCryptoConfig
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import de.gematik.ti.erp.app.vau.repository.VauLocalDataSource
import de.gematik.ti.erp.app.vau.repository.VauRemoteDataSource
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststore
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreTimeSourceProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import org.bouncycastle.cert.X509CertificateHolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindings.Scope
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.time.Duration
import java.time.Instant

fun vauModule(scope: Scope<Any?>) = DI.Module("VAU Module") {
    bindInstance {
        TruststoreConfig {
            if (BuildKonfig.INTERNAL) {
                BuildKonfig.APP_TRUST_ANCHOR_BASE64_TU
            } else {
                BuildKonfig.APP_TRUST_ANCHOR_BASE64_PU
            }
        }
    }
    bind { scoped(scope).singleton { VauRemoteDataSource(instance()) } }
    bind { scoped(scope).singleton { VauLocalDataSource(instance<ScopedRealm>().realm) } }
    bind { scoped(scope).singleton { VauRepository(instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { DefaultCryptoConfig() } }
    bind { scoped(scope).singleton { VauChannelInterceptor(instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { TruststoreUseCase(instance(), instance(), instance(), instance()) } }
    bind { scoped(scope).singleton<Any, TruststoreTimeSourceProvider> { { Instant.now() } } }
    bind {
        scoped(scope).singleton {
            { untrustedOCSPList: UntrustedOCSPList,
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
    }
}
