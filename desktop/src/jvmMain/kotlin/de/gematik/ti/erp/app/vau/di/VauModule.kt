package de.gematik.ti.erp.app.vau.di

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
import java.time.Duration
import java.time.Instant
import org.bouncycastle.cert.X509CertificateHolder
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val vauModule = DI.Module("VAU Module") {
    bindInstance { TruststoreConfig() }
    bindSingleton { VauRemoteDataSource(instance()) }
    bindSingleton { VauLocalDataSource() }
    bindSingleton { VauRepository(instance(), instance(), instance()) }
    bindSingleton { DefaultCryptoConfig() }
    bindSingleton { VauChannelInterceptor(instance(), instance(), instance()) }
    bindSingleton<TruststoreTimeSourceProvider> { { Instant.now() } }
    bindSingleton<TrustedTruststoreProvider> {
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
    bindSingleton { TruststoreUseCase(instance(), instance(), instance(), instance()) }
}
