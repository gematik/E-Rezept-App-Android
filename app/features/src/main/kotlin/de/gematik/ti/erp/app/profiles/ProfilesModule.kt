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

package de.gematik.ti.erp.app.profiles

import de.gematik.ti.erp.app.profiles.repository.DefaultProfilesRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DecryptAccessTokenUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeletePairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetSelectedProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.IsProfilePKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileInsuranceTypeUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val profilesModule = DI.Module("profilesModule") {
    bindProvider { AddProfileUseCase(instance()) }
    bindProvider { DeleteProfileUseCase(instance(), instance(), instance()) }
    bindProvider { GetActiveProfileUseCase(instance()) }
    bindProvider { GetProfileByIdUseCase(instance()) }
    bindProvider { GetProfilesUseCase(instance()) }
    bindProvider { SwitchActiveProfileUseCase(instance()) }
    bindProvider { UpdateProfileUseCase(instance()) }
    bindProvider { DecryptAccessTokenUseCase(instance()) }
    bindProvider { LogoutProfileUseCase(instance()) }
    bindProvider { SwitchProfileInsuranceTypeUseCase(instance()) }
    bindProvider { IsProfilePKVUseCase(instance()) }
    bindProvider { GetSelectedProfileUseCase(instance()) }
    bindProvider { GetPairedDevicesUseCase(instance()) }
    bindProvider { DeletePairedDevicesUseCase(instance()) }
    bindProvider { ProfilesUseCase(instance()) }
}

val profileRepositoryModule = DI.Module("profileRepositoryModule", allowSilentOverride = true) {
    bindProvider<ProfileRepository> { DefaultProfilesRepository(instance()) }
}
