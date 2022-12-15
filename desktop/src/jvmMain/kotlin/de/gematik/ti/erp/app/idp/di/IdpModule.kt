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

package de.gematik.ti.erp.app.idp.di

import de.gematik.ti.erp.app.cardwall.AuthenticationUseCase
import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpBasicUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindings.Scope
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

fun idpModule(scope: Scope<Any?>) = DI.Module("IDP Module") {
    bind { scoped(scope).singleton { IdpLocalDataSource() } }
    bind { scoped(scope).singleton { IdpRemoteDataSource(instance()) } }
    bind { scoped(scope).singleton { IdpRepository(instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { IdpBasicUseCase(instance(), instance()) } }
    bind { scoped(scope).singleton { IdpUseCase(instance(), instance()) } }
    bind { scoped(scope).singleton { AuthenticationUseCase(instance()) } }
}
