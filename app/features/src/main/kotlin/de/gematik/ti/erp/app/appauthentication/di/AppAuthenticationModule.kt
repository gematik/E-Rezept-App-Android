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

package de.gematik.ti.erp.app.appauthentication.di

import de.gematik.ti.erp.app.appauthentication.repository.AppAuthenticationRepository
import de.gematik.ti.erp.app.appauthentication.repository.DefaultAppAuthenticationRepository
import de.gematik.ti.erp.app.appauthentication.usecase.DisableDeviceSecurityUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.EnableDeviceSecurityUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.GetAppAuthenticationUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.IncrementNumberOfAuthenticationFailuresUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.ResetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.ResetNumberOfAuthenticationFailuresUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.ResetPasswordUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.InitialiseAppAuthenticationWithChosenMethodUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.SetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.SetPasswordUseCase
import de.gematik.ti.erp.app.database.datastore.appauthentication.AppAuthenticationLocalDataSourceV2
import de.gematik.ti.erp.app.database.datastore.appauthentication.appAuthenticationLocalDataSourceV2
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val appAuthenticationModule = DI.Module("appAuthenticationModule") {
    bindProvider<AppAuthenticationRepository> {
        DefaultAppAuthenticationRepository(instance(), instance())
    }
    bindSingleton<AppAuthenticationLocalDataSourceV2> { appAuthenticationLocalDataSourceV2(instance()) }
    bindProvider { GetAppAuthenticationUseCase(instance()) }
    bindProvider { InitialiseAppAuthenticationWithChosenMethodUseCase(instance()) }
    bindProvider { EnableDeviceSecurityUseCase(instance()) }
    bindProvider { DisableDeviceSecurityUseCase(instance()) }
    bindProvider { SetPasswordUseCase(instance()) }
    bindProvider { ResetPasswordUseCase(instance()) }
    bindProvider { SetAuthenticationTimeOutSystemUptimeUseCase(instance()) }
    bindProvider { ResetAuthenticationTimeOutSystemUptimeUseCase(instance()) }
    bindProvider { IncrementNumberOfAuthenticationFailuresUseCase(instance()) }
    bindProvider { ResetNumberOfAuthenticationFailuresUseCase(instance()) }
}
