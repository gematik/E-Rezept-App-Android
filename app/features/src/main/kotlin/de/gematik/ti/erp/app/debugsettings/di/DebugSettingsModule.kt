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

package de.gematik.ti.erp.app.debugsettings.di

import de.gematik.ti.erp.app.debugsettings.data.repository.DebugSettingsRepository
import de.gematik.ti.erp.app.debugsettings.data.repository.DefaultDebugSettingsRepository
import de.gematik.ti.erp.app.debugsettings.data.repository.local.DebugSettingsLocalDataSource
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.GetShowTelematikIdStateUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyBackendServiceSelectionUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyGetSearchAccessTokenUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacySearchAccessTokenModifierUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.ToggleShowTelematikIdStateUseCase
import de.gematik.ti.erp.app.debugsettings.usecase.BreakSsoTokenUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val debugSettingsModule = DI.Module("debugSettingsModule") {

    bindProvider { DebugSettingsLocalDataSource(instance()) }
    bindProvider<DebugSettingsRepository> { DefaultDebugSettingsRepository(instance()) }

    bindProvider { GetShowTelematikIdStateUseCase(instance()) }
    bindProvider { ToggleShowTelematikIdStateUseCase(instance()) }
    bindProvider { BreakSsoTokenUseCase(instance(), instance()) }
    bindProvider { PharmacyBackendServiceSelectionUseCase(instance()) }
    bindProvider { PharmacyGetSearchAccessTokenUseCase(instance()) }
    bindProvider { PharmacySearchAccessTokenModifierUseCase(instance()) }
}
