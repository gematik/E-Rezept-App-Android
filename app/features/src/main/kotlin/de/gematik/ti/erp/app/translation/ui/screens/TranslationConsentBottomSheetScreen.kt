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

package de.gematik.ti.erp.app.translation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.ContextExtensions.getCurrentLocaleAsDisplayLanguage
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.presentation.rememberTranslationConsentController
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar

class TranslationConsentBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(withCloseButton = true) {
    @Composable
    override fun Content() {
        val snackbar = LocalSnackbar.current
        val controller = rememberTranslationConsentController()
        val currentLanguage = context.getCurrentLocaleAsDisplayLanguage()
        val downloadState by controller.languageDownloadState.collectAsStateWithLifecycle()

        controller.onConsentEvent.listen {
            navController.navigateUp()
        }
        controller.onDownloadFailedEvent.listen {
            navController.navigateUp()
            // inform that the language model did not download
            if (BuildConfigExtension.isInternalDebug) {
                snackbar.show(
                    text = it,
                    actionTextId = R.string.snackbar_close
                )
            }
        }
        TranslationConsentContent(
            currentLanguage = currentLanguage,
            downloadState = downloadState,
            onConsentChange = controller::toggleTranslationConsent
        )
    }
}

@Composable
private fun TranslationConsentContent(
    currentLanguage: String,
    downloadState: LanguageDownloadState,
    onConsentChange: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Base content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = SizeDefaults.triple)
        ) {
            // Title
            Center(
                modifier = Modifier.padding(top = SizeDefaults.double)
            ) {
                Text(
                    text = stringResource(R.string.offline_translation_consent_title),
                    style = AppTheme.typography.h6,
                    color = AppTheme.colors.primary600
                )
            }

            SpacerMedium()

            // Description
            Text(
                text = stringResource(R.string.offline_translation_consent_text, currentLanguage),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )

            SpacerMedium()

            // Confirm / Action button
            Button(
                onClick = { onConsentChange(true) },
                enabled = downloadState !is LanguageDownloadState.Downloading,
                colors = ButtonDefaults.buttonColors(
                    contentColor = AppTheme.colors.neutral000,
                    containerColor = AppTheme.colors.primary600
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SizeDefaults.sevenfoldAndHalf)
            ) {
                Text(
                    text = stringResource(R.string.offline_translation_consent_allow),
                    color = AppTheme.colors.neutral000
                )
            }

            SpacerSmall()

            OutlinedButton(
                onClick = { onConsentChange(false) },
                enabled = downloadState !is LanguageDownloadState.Downloading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.colors.primary600
                ),
                border = BorderStroke(
                    width = SizeDefaults.eighth,
                    color = AppTheme.colors.primary600
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SizeDefaults.sevenfoldAndHalf)
            ) {
                Text(
                    text = stringResource(R.string.offline_translation_consent_deny),
                    color = AppTheme.colors.primary600
                )
            }
        }

        // Overlay loading indicator
        if (downloadState is LanguageDownloadState.Downloading) {
            Box(
                modifier = Modifier
                    .background(AppTheme.colors.neutral000.copy(alpha = 0.6f))
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .zIndex(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.padding(SizeDefaults.fifteenfoldAndHalf),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.primary600)
                    SpacerSmall()
                    Text(
                        text = stringResource(R.string.offline_translation_downloading),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.primary600
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
internal fun TranslationConsentContentPreview() {
    PreviewAppTheme {
        TranslationConsentContent(
            currentLanguage = "Englisch",
            onConsentChange = {},
            downloadState = LanguageDownloadState.NotStarted
        )
    }
}

@LightDarkPreview
@Composable
internal fun TranslationConsentContentLoadingPreview() {
    PreviewAppTheme {
        TranslationConsentContent(
            currentLanguage = "Englisch",
            onConsentChange = {},
            downloadState = LanguageDownloadState.Downloading
        )
    }
}
