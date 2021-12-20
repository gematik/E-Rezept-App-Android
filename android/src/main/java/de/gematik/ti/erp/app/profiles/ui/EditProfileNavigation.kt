package de.gematik.ti.erp.app.profiles.ui

import TokenScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode

object ProfileDestinations {
    object Profile : Route("profile")
    object Token : Route("token")
}

@Composable
fun EditProfileNavGraph(
    state: SettingsScreen.State,
    navController: NavHostController,
    onBack: () -> Unit,
    profile: ProfilesUseCaseData.Profile,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    mainNavController: NavController,
) {

    NavHost(navController = navController, startDestination = ProfileDestinations.Profile.route) {
        composable(ProfileDestinations.Profile.route) {
            EditProfileScreenContent(
                onClickToken = { navController.navigate(ProfileDestinations.Token.path()) },
                ssoTokenValid = profile.ssoTokenValid(),
                onClickLogIn = {
                    settingsViewModel.switchProfile(profile.name)
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(settingsViewModel.isCanAvailable(profile))
                    )
                },
                onBack = onBack,
                state = state,
                settingsViewModel = settingsViewModel,
                selectedProfile = profile,
                onRemoveProfile = onRemoveProfile
            )
        }
        composable(ProfileDestinations.Token.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                TokenScreen(
                    onBack = { navController.popBackStack() },
                    ssoToken = profile.ssoToken?.token,
                    accessToken = profile.accessToken,
                )
            }
        }
    }
}
