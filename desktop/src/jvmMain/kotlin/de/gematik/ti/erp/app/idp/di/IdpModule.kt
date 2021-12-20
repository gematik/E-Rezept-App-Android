package de.gematik.ti.erp.app.idp.di

import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpBasicUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val idpModule = DI.Module("IDP Module") {
    bindInstance { IdpLocalDataSource() }
    bindSingleton { IdpRemoteDataSource(instance()) }
    bindSingleton { IdpRepository(instance(), instance(), instance()) }
    bindSingleton { IdpBasicUseCase(instance(), instance()) }
    bindSingleton { IdpUseCase(instance(), instance()) }
}
