package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.db.entities.ProfileColors
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
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@ExperimentalCoroutinesApi
class ProfilesUseCaseTest {

    private val expectedProfiles = listOf(
        ProfileEntity(name = "Tester", color = ProfileColors.TREE),
        ProfileEntity(name = "Tester1", color = ProfileColors.PINK),
        ProfileEntity(name = "Tester2", color = ProfileColors.SPRING_GRAY),
        ProfileEntity(name = "Tester3", color = ProfileColors.SUN_DEW)
    )
    private val expectedProfile = ProfileEntity(id = 2, name = "Tester2", color = ProfileColors.SPRING_GRAY)
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
    fun `profiles - should return list of four profiles`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.profiles.first().let {
                assertEquals(expectedProfiles.size, it.size)
            }
        }

    @Test
    fun `active profile name - should return tester 2`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.activeProfileName().first().let {
                assertEquals(expectedActiveProfile.profileName, it)
            }
        }

    @Test
    fun `active profile - should return expected active profile`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.activeProfile().first().let {
                assertEquals(expectedActiveProfile, it)
            }
        }

    @Test
    fun `get profile by id (2) - should return expected profile (2)`() =
        coroutineRule.testDispatcher.runBlockingTest {
            profilesUseCase.getProfileById(2).first().let {
                assertEquals(expectedProfile, it)
            }
        }
}
