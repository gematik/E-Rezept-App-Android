package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.Profile
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProfilesUseCaseTest {

    private val expectedProfiles = listOf(
        Profile(name = "Tester"),
        Profile(name = "Tester1"),
        Profile(name = "Tester2"),
        Profile(name = "Tester3")
    )
    private val expectedProfile = Profile(id = 2, name = "Tester2")
    private val expectedActiveProfile = ActiveProfile(profileName = "Tester2")
    private val expectedDefaultProfile = Profile(
        id = 0,
        name = "",
        insuranceNumber = ""
    )

    private lateinit var profilesUseCase: ProfilesUseCase

    @MockK
    lateinit var profilesRepository: ProfilesRepository

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        profilesUseCase = ProfilesUseCase(profilesRepository)

        every { profilesRepository.profiles() } returns flowOf(expectedProfiles)
        every { profilesRepository.activeProfile() } returns flowOf(expectedActiveProfile)
        every { profilesRepository.getProfileById(2) } returns flowOf(expectedProfile)
    }

    @Test
    fun `profiles - should return list of four profiles`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.profiles().map {
                assertEquals(expectedProfiles.size, it.size)
            }
        }

    @Test
    fun `active profile name - should return tester 2`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.activeProfileName().map {
                assertEquals(expectedActiveProfile.profileName, it)
            }
        }

    @Test
    fun `generate default profile - should return expected default profile`() =
        coroutineRule.testDispatcher.runBlockingTest {
            val defaultProfile = profilesUseCase.generateDefaultProfile()
            assertEquals(expectedDefaultProfile, defaultProfile)
        }

    @Test
    fun `active profile - should return expected active profile`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.activeProfile().map {
                assertEquals(expectedActiveProfile, it)
            }
        }

    @Test
    fun `get profile by id (2) - should return expected profile (2)`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.getProfileById(2).map {
                assertEquals(expectedProfile, it)
            }
        }
}
