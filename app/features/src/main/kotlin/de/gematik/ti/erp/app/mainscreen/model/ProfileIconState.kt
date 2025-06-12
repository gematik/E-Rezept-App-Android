/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.mainscreen.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

/**
 * Represents the visual appearance of the main screen avatar.
 *
 * @property background The background color of the avatar.
 * @property imageVector The image vector (icon) to be displayed inside the avatar.
 * @property tint The tint color applied to the image vector.
 */
data class ProfileAvatarAppearance(
    val background: Color,
    val imageVector: ImageVector,
    val tint: Color
)

/**
 * Represents the visual appearance of a profile chip.
 *
 * @property color The background color of the profile chip.
 * @property icon The icon displayed within the profile chip.
 */
data class ProfileChipAppearance(
    val color: Color,
    val icon: ImageVector
)

/**
 * A sealed class representing different states of the profile icon,
 * with customizable appearances for both the avatar and profile chip.
 */
sealed class ProfileIconState {

    /**
     * Provides the default appearance of the profile chip.
     * Can be overridden by specific states to provide custom styling.
     */
    @Composable
    open fun chip(): ProfileChipAppearance = ProfileChipAppearance(
        color = AppTheme.colors.neutral400,
        icon = Icons.Rounded.Cloud
    )

    /**
     * Provides the default appearance of the avatar.
     * Can be overridden by specific states to provide custom styling.
     */
    @Composable
    open fun avatar(): ProfileAvatarAppearance = ProfileAvatarAppearance(
        background = AppTheme.colors.neutral400,
        imageVector = Icons.Rounded.Cloud,
        tint = AppTheme.colors.neutral400
    )

    /**
     * Represents the "Refreshing" state of the profile icon.
     * Displays a rotating refresh icon and highlights the avatar and chip in the primary theme colors.
     */
    data object IsRefreshing : ProfileIconState() {

        @Composable
        override fun chip() = ProfileChipAppearance(
            icon = Icons.Outlined.Autorenew,
            color = AppTheme.colors.primary400
        )

        @Composable
        override fun avatar() = ProfileAvatarAppearance(
            background = AppTheme.colors.primary200,
            imageVector = Icons.Outlined.Autorenew,
            tint = AppTheme.colors.primary500
        )
    }

    /**
     * Represents the "Offline" state of the profile icon.
     * Displays an offline cloud icon with neutral background colors.
     */
    data object IsOffline : ProfileIconState() {

        @Composable
        override fun chip() = ProfileChipAppearance(
            icon = Icons.Rounded.CloudOff,
            color = AppTheme.colors.neutral400
        )

        @Composable
        override fun avatar() = ProfileAvatarAppearance(
            background = AppTheme.colors.neutral200,
            imageVector = Icons.Rounded.Close,
            tint = AppTheme.colors.neutral600
        )
    }

    /**
     * Represents the "Online" state of the profile icon.
     * Displays a cloud-done icon and uses green theme colors for the chip and avatar.
     */
    data object IsOnline : ProfileIconState() {

        @Composable
        override fun chip() = ProfileChipAppearance(
            icon = Icons.Rounded.CloudDone,
            color = AppTheme.colors.green400
        )

        @Composable
        override fun avatar() = ProfileAvatarAppearance(
            background = AppTheme.colors.green200,
            imageVector = Icons.Rounded.Check,
            tint = AppTheme.colors.green500
        )
    }

    /**
     * Represents the "Error" state of the profile icon.
     * Displays a warning icon for the chip and a checkmark for the avatar, both styled in yellow.
     */
    data object IsError : ProfileIconState() {

        @Composable
        override fun chip() = ProfileChipAppearance(
            icon = ImageVector.vectorResource(id = R.drawable.ic_cloud_warning),
            color = AppTheme.colors.yellow400
        )

        @Composable
        override fun avatar() = ProfileAvatarAppearance(
            background = AppTheme.colors.yellow500,
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_checkmark),
            tint = AppTheme.colors.yellow200
        )
    }

    /**
     * Computes and remembers the current state of the profile icon based on the profile's refresh state,
     * network connectivity, and SSO token validity.
     *
     * This composable method observes and reacts to changes in the provided `ProfileStateWrapper` and `Profile`
     * objects, ensuring recomposition happens only when necessary.
     *
     * @param profileLifecycleState A [ProfileLifecycleState] containing the state flows for refresh status and network connectivity.
     * @param activeProfile The currently active [ProfilesUseCaseData.Profile], which includes the SSO token scope data.
     * @return The current [ProfileIconState], which can be:
     * - [ProfileIconState.IsOffline]: The SSO token is invalid or a fallback state.
     * - [ProfileIconState.IsError]: The device is offline.
     * - [ProfileIconState.IsRefreshing]: The network is connected, and a refresh is in progress.
     * - [ProfileIconState.IsOnline]: The network is connected, and the SSO token is valid.
     */
    @Composable
    fun rememberProfileIconState(
        profileLifecycleState: ProfileLifecycleState,
        activeProfile: UiState<ProfilesUseCaseData.Profile>
    ): State<ProfileIconState> {
        // Collect the current refresh state as a StateFlow and observe its lifecycle for updates.
        val isProfileRefreshing by profileLifecycleState.isProfileRefreshing.collectAsStateWithLifecycle()

        // Collect the network connectivity status as a Flow, with a default value of true.
        val isNetworkConnected by profileLifecycleState.networkStatus.collectAsStateWithLifecycle()

        val isTokenValid by profileLifecycleState.isTokenValid.collectAsStateWithLifecycle()

        // Extract the SSO token scope from the active profile, if available.
        val ssoTokenScope = activeProfile.data?.ssoTokenScope

        // Compute the refreshing state, which is true only if the device is connected and a refresh is in progress.
        val isRefreshing = remember(isProfileRefreshing, ssoTokenScope?.token, isNetworkConnected) {
            if (isNetworkConnected) isProfileRefreshing else false
        }

        /**
         * Represents the current state of the profile icon based on various conditions such as
         * network connectivity, token validity, and refresh state.
         *
         * This state is recomposed only when one of its dependencies (`refreshState`,
         * `ssoTokenScope?.token`, or `isNetworkConnected`) changes. The derived state ensures
         * efficient recalculation of `iconState` without unnecessary recompositions.
         *
         * The possible states are:
         * - `ProfileIconState.IsOffline`: Token is invalid, or fallback default.
         * - `ProfileIconState.IsError`: Device is offline (no network connection).
         * - `ProfileIconState.IsRefreshing`: Network is connected, and a refresh operation is ongoing.
         * - `ProfileIconState.IsOnline`: Network is connected, and the token is valid.
         */
        return remember(isProfileRefreshing, ssoTokenScope?.token, isNetworkConnected) {
            derivedStateOf {
                getCurrentProfileIconState(isTokenValid, isNetworkConnected, isRefreshing)
            }
        }
    }

    /**
     * Determines the current state of the profile icon based on the user's SSO token validity,
     * network connectivity, and refresh state.
     *
     * @param isTokenValid The validity of the Single Sign-On (SSO) token scope that provides token validity information.
     * @param isNetworkConnected Boolean indicating whether the device is connected to the network.
     * @param isProfileRefreshing Boolean indicating whether a refresh operation is currently active.
     * @return The current [ProfileIconState], which can be one of the following:
     * - [ProfileIconState.IsOffline]: The user token is invalid, or no other conditions are met.
     * - [ProfileIconState.IsError]: The device is offline.
     * - [ProfileIconState.IsRefreshing]: The network is connected, and a refresh is ongoing.
     * - [ProfileIconState.IsOnline]: The network is connected, and the token is valid.
     */
    private fun getCurrentProfileIconState(
        isTokenValid: Boolean,
        isNetworkConnected: Boolean,
        isProfileRefreshing: Boolean
    ): ProfileIconState {
        val currentIconState = when {
            // (1) if the user is invalid, we show that even if there is no network
            !isTokenValid -> IsOffline

            // (2) we check if there is network before deciding to refresh or show online
            !isNetworkConnected -> IsError

            // (3) we show the refresh icon if the network is connected and refreshing
            isNetworkConnected && isProfileRefreshing -> IsRefreshing

            // (4) we show the online icon if the network is connected and the token is valid
            isNetworkConnected && isTokenValid -> IsOnline

            // (5) we show the offline icon if none of the above conditions are met
            else -> IsOffline
        }
        return currentIconState
    }
}
