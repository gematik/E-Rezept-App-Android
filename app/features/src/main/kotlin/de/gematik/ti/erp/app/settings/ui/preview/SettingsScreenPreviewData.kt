/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui.preview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.profiles.presentation.ProfileController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData

val LocalIsPreviewMode = compositionLocalOf { false }

data class SettingsScreenPreviewData(
    val name: String,
    val profiles: List<ProfilesUseCaseData.Profile>,
    val buildConfig: BuildConfigInformation,
    val zoomState: MutableState<SettingStatesData.ZoomState>,
    val screenShotsState: MutableState<Boolean>
)

object MockBuildConfigInformation : BuildConfigInformation {
    override fun versionName(): String = "1.25.0-RC2-debug"
    override fun versionCode(): String = "3598"
    override fun model(): String = "samsung SM-S921B (e1sxeea)"
    override fun language(): String = "en"

    @Composable
    override fun inDarkTheme(): String = "an"
    override fun nfcInformation(context: Context): String = "available"
    override fun isMockedApp(): Boolean = false
}

class SettingsScreenPreviewProvider : PreviewParameterProvider<SettingsScreenPreviewData> {
    override val values: Sequence<SettingsScreenPreviewData> = sequenceOf(
        SettingsScreenPreviewData(
            name = "SettingsScreen",
            profiles = listOf(
                DEFAULT_EMPTY_PROFILE.copy(name = "Max Mustermann")
            ),
            buildConfig = MockBuildConfigInformation,
            zoomState = mutableStateOf(SettingStatesData.defaultZoomState),
            screenShotsState = mutableStateOf(false)
        ),
        SettingsScreenPreviewData(
            name = "SettingsScreen",
            profiles = listOf(
                DEFAULT_EMPTY_PROFILE
            ),
            buildConfig = MockBuildConfigInformation,
            zoomState = mutableStateOf(SettingStatesData.ZoomState(zoomEnabled = true)),
            screenShotsState = mutableStateOf(true)
        )
    )
}
