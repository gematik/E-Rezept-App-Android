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

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@ExperimentalCoroutinesApi
class ProfilesUseCaseTest {

    private val expectedProfiles = listOf(
        ProfileEntity(name = "Tester", color = ProfileColorNames.TREE),
        ProfileEntity(name = "Tester1", color = ProfileColorNames.PINK),
        ProfileEntity(name = "Tester2", color = ProfileColorNames.SPRING_GRAY),
        ProfileEntity(name = "Tester3", color = ProfileColorNames.SUN_DEW)
    )
    private val expectedProfile = ProfileEntity(id = 2, name = "Tester2", color = ProfileColorNames.SPRING_GRAY)
    private val expectedActiveProfile = ActiveProfile(profileName = "Tester2")

    private lateinit var profilesUseCase: ProfilesUseCase

    @MockK
    lateinit var profilesRepository: ProfilesRepository

    @MockK
    lateinit var idpRepository: IdpRepository

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val ssoToken = mockk<SingleSignOnToken>()
        every { ssoToken.isValid(any()) } returns true
        every { ssoToken.validOn } returns Instant.now().plusSeconds(1000)

        every { profilesRepository.profiles() } returns flowOf(expectedProfiles)
        every { profilesRepository.activeProfile() } returns flowOf(expectedActiveProfile)
        every { profilesRepository.getProfileById(2) } returns flowOf(expectedProfile)
        coEvery { profilesRepository.updateLastAuthenticated(any(), any()) } answers {}
        coEvery { idpRepository.getSingleSignOnToken(any()) } returns flowOf(ssoToken)
        coEvery { idpRepository.decryptedAccessToken(any()) } returns flowOf("")

        profilesUseCase = ProfilesUseCase(profilesRepository, idpRepository, coroutineRule.testDispatchProvider)
    }

    @Test
    fun `profiles - should return list of four profiles`() = runTest {
        profilesUseCase.profiles.first().let {
            assertEquals(expectedProfiles.size, it.size)
        }
    }

    @Test
    fun `active profile name - should return tester 2`() = runTest {
        profilesUseCase.activeProfileName().first().let {
            assertEquals(expectedActiveProfile.profileName, it)
        }
    }

    @Test
    fun `active profile - should return expected active profile`() =
        runTest {
            profilesUseCase.activeProfile().first().let {
                assertEquals(expectedActiveProfile, it)
            }
        }

    @Test
    fun `get profile by id (2) - should return expected profile (2)`() =
        runTest {
            profilesUseCase.getProfileById(2).first().let {
                assertEquals(expectedProfile, it)
            }
        }
}
