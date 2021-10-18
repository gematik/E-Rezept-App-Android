package de.gematik.ti.erp.app.profiles

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.tracking.Tracker
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
class ProfilesIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsUseCase: SettingsUseCase
    private lateinit var profilesUseCase: ProfilesUseCase
    private lateinit var profilesRepository: ProfilesRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var idpRepository: IdpRepository
    private lateinit var toggleManager: FeatureToggleManager

    private lateinit var demoUseCase: DemoUseCase
    private lateinit var tracker: Tracker
    private lateinit var appPrefs: SharedPreferences
    private lateinit var navController: TestNavHostController
    private lateinit var db: AppDatabase
    val context = ApplicationProvider.getApplicationContext<Context>()

    private val moshi = Moshi.Builder().add(OCSPAdapter()).add(X509Adapter()).build()

    @Before
    fun init() {
        navController = TestNavHostController(
            context
        )
        appPrefs = mockk()
        tracker = mockk()
        demoUseCase = mockk()
        idpRepository = mockk()
        toggleManager = mockk()

        every { demoUseCase.demoModeActive } returns MutableStateFlow(false)
        every { tracker.trackingAllowed } returns MutableStateFlow(false)
        every { appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false) } returns (true)
        every { toggleManager.featureState(Features.MULTI_PROFILE.featureName) } returns flowOf(true)
        every { idpRepository.decryptedAccessToken } returns null

        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        )
            .addTypeConverter(TruststoreConverter(moshi))
            .build()

        settingsRepository = SettingsRepository(db)
        profilesRepository = ProfilesRepository(db)
        profilesUseCase = ProfilesUseCase(profilesRepository)
        profilesUseCase.activeProfileName()

        settingsUseCase = SettingsUseCase(context, settingsRepository, idpRepository, profilesRepository, appPrefs, demoUseCase, profilesUseCase)

        viewModel =
            SettingsViewModel(settingsUseCase, demoUseCase, profilesUseCase, tracker, appPrefs, toggleManager)
    }

    @After
    fun closeDB() {
        db.close()
    }

    @Test
    fun testProfiles() {
        runBlocking {
            coEvery { idpRepository.getSingleSignOnToken(any()) } returns null

            composeTestRule.setContent {
                AppTheme {
                    SettingsScreen(SettingsScrollTo.None, navController, viewModel)
                }
            }

            composeTestRule.onNodeWithTag("Profiles").assertIsDisplayed()
            // TODO: test profiles (add + delete) when UI is ready
        }
    }
}
