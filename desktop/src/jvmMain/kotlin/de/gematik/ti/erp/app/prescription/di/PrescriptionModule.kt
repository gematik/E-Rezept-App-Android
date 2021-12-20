package de.gematik.ti.erp.app.prescription.di

import de.gematik.ti.erp.app.prescription.repository.LocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteDataSource
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionMapper
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindings.Scope
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

fun prescriptionModule(scope: Scope<Any?>) = DI.Module("Prescription Module") {
    bind { scoped(scope).singleton { RemoteDataSource(instance()) } }
    bind { scoped(scope).singleton { LocalDataSource() } }
    bind { scoped(scope).singleton { PrescriptionRepository(instance(), instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { PrescriptionUseCase(instance(), instance()) } }
    bindInstance { PrescriptionMapper() }
}
