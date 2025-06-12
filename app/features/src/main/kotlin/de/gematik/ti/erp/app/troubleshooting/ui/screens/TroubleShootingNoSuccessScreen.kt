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

package de.gematik.ti.erp.app.troubleshooting.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingCloseButton
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingContactUsButton
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingScaffold
import de.gematik.ti.erp.app.troubleshooting.ui.preview.TroubleShootingScreenPreviewData
import de.gematik.ti.erp.app.troubleshooting.ui.preview.TroubleShootingScreenPreviewProvider
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.buildFeedbackBodyWithDeviceInfo
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.openMailClient
import org.kodein.di.compose.rememberInstance

class TroubleShootingNoSuccessScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val buildConfig by rememberInstance<BuildConfigInformation>()
        val context = LocalContext.current
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_no_success_title),
            onBack = { navController.popBackStack() },
            bottomBarButton = {
                TroubleShootingCloseButton(
                    onClick = {
                        navController.popBackStack(
                            TroubleShootingRoutes.TroubleShootingIntroScreen.route,
                            inclusive = true
                        )
                    }
                )
            }
        ) {
            TroubleShootingNoSuccessScreenContent(
                context = context,
                buildConfig = buildConfig
            )
        }
    }
}

@Composable
private fun TroubleShootingNoSuccessScreenContent(
    context: Context,
    buildConfig: BuildConfigInformation
) {
    val mailAddress = stringResource(R.string.settings_contact_mail_address)
    val subject = stringResource(R.string.settings_feedback_mail_subject)
    val body = buildFeedbackBodyWithDeviceInfo(
        darkMode = buildConfig.inDarkTheme(),
        language = buildConfig.language(),
        versionName = buildConfig.versionName(),
        nfcInfo = buildConfig.nfcInformation(context),
        phoneModel = buildConfig.model()
    )

    Column {
        Text(
            text = stringResource(R.string.cdw_troubleshooting_no_success_body),
            style = AppTheme.typography.body1
        )
        SpacerLarge()
        TroubleShootingContactUsButton(
            Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                openMailClient(context, mailAddress, body, subject)
            }
        )
    }
}

@LightDarkPreview
@Composable
fun TroubleShootingNoSuccessScreenPreview(
    @PreviewParameter(TroubleShootingScreenPreviewProvider::class) previewData: TroubleShootingScreenPreviewData
) {
    PreviewAppTheme {
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_no_success_title),
            onBack = { },
            bottomBarButton = {
                TroubleShootingCloseButton(
                    onClick = { }
                )
            }
        ) {
            TroubleShootingNoSuccessScreenContent(
                context = LocalContext.current,
                buildConfig = previewData.buildConfig
            )
        }
    }
}
