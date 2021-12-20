package de.gematik.ti.erp.app.communication.di

import de.gematik.ti.erp.app.communication.repository.CommunicationRepository
import de.gematik.ti.erp.app.communication.repository.LocalDataSource
import de.gematik.ti.erp.app.communication.repository.RemoteDataSource
import de.gematik.ti.erp.app.communication.usecase.CommunicationUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindings.Scope
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

fun communicationModule(scope: Scope<Any?>) = DI.Module("Communication Module") {
    bind { scoped(scope).singleton { RemoteDataSource(instance()) } }
    bind { scoped(scope).singleton { LocalDataSource() } }
    bind { scoped(scope).singleton { CommunicationRepository(instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { CommunicationUseCase(instance(), instance()) } }
}
