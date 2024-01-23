/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.profiles

import de.gematik.ti.erp.app.profiles.repository.DefaultProfilesRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DecryptAccessTokenUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetSelectedProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesWithPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ResetProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToPKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val profilesModule = DI.Module("profilesModule") {
    bindProvider { AddProfileUseCase(instance()) }
    bindProvider { DeleteProfileUseCase(instance(), instance()) }
    bindProvider { GetActiveProfileUseCase(instance()) }
    bindProvider { GetProfilesUseCase(instance()) }
    bindProvider { ResetProfileUseCase(instance(), instance()) }
    bindProvider { SwitchActiveProfileUseCase(instance()) }
    bindProvider { UpdateProfileUseCase(instance()) }
    bindProvider { DecryptAccessTokenUseCase(instance()) }
    bindProvider { LogoutProfileUseCase(instance()) }
    bindProvider { SwitchProfileToPKVUseCase(instance()) }
    bindProvider { GetSelectedProfileUseCase(instance()) }

    bindProvider { ProfilesWithPairedDevicesUseCase(instance(), instance()) }
    bindProvider { ProfilesUseCase(instance(), instance()) }
}

val profileRepositoryModule = DI.Module("profileRepositoryModule", allowSilentOverride = true) {
    bindProvider<ProfileRepository> { DefaultProfilesRepository(instance(), instance()) }
}
