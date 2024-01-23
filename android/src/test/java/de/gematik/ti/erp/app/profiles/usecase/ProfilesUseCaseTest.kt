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

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFails

@ExperimentalCoroutinesApi
class ProfilesUseCaseTest {
    private lateinit var profilesUseCase: ProfilesUseCase

    @MockK(relaxed = true)
    lateinit var profilesRepository: ProfilesRepository

    @MockK
    lateinit var idpRepository: IdpRepository

    @MockK
    lateinit var auditEventsRepository: AuditEventsRepository

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    val profile = ProfilesUseCaseData.Profile(
        id = "1234567890",
        name = "Test",
        insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(),
        active = false,
        color = ProfilesData.ProfileColorNames.PINK,
        lastAuthenticated = null,
        ssoTokenScope = null,
        personalizedImage = null,
        avatarFigure = ProfilesData.AvatarFigure.PersonalizedImage
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        profilesUseCase = ProfilesUseCase(
            profilesRepository = profilesRepository,
            idpRepository = idpRepository
        )
    }

    @Test
    fun `update profile name - should sanitize new name`() = runTest {
        profilesUseCase.updateProfileName(profile.id, "    T es t  ")

        coVerify(exactly = 1) { profilesRepository.updateProfileName(profile.id, "T es t") }
    }

    @Test
    fun `update profile with empty name - should not update name`() = runTest {
        assertFails {
            profilesUseCase.updateProfileName(profile.id, "")
        }

        coVerify(exactly = 0) { profilesRepository.updateProfileName(any(), any()) }
    }

    @Test
    fun `replace last profile`() = runTest {
        assertFails {
            profilesUseCase.removeAndSaveProfile(profile, "")
        }
    }
}
