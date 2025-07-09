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

package de.gematik.ti.erp.app.appupdate.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateFlagUseCase
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.openAppPlayStoreLink
import org.kodein.di.compose.rememberInstance

class AppUpdateScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val useCase by rememberInstance<ChangeAppUpdateFlagUseCase>()
        UpdateAppScreenContent { useCase.invoke(false) }
    }
}

@Composable
private fun UpdateAppScreenContent(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val padding = (LocalActivity.current as? BaseActivity)?.applicationInnerPadding

    // TODO: Convert to AnimatedElevationScaffold
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.Medium,
                        start = PaddingDefaults.Medium,
                        end = PaddingDefaults.Medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(R.drawable.ic_onboarding_logo_flag),
                    null,
                    modifier = Modifier.padding(end = SizeDefaults.one)
                )
                Icon(
                    painterResource(R.drawable.ic_onboarding_logo_gematik),
                    null,
                    tint = AppTheme.colors.primary900
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
                    .padding(PaddingDefaults.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.illustration_girl),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(bottom = PaddingDefaults.Small),
                    text = stringResource(R.string.app_update_title_text),
                    style = AppTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.app_update_description_text),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Justify
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    color = AppTheme.colors.neutral300
                )
                SpacerMedium()
                Button(
                    shape = RoundedCornerShape(SizeDefaults.double),
                    onClick = {
                        context.openAppPlayStoreLink()
                    },
                    modifier = Modifier
                        .padding(horizontal = PaddingDefaults.Large)
                ) {
                    Text(
                        stringResource(R.string.app_update_Button_text),
                        Modifier.padding(
                            horizontal = PaddingDefaults.XXLargeMedium,
                            vertical = PaddingDefaults.Small
                        )
                    )
                }
                TextButton(
                    onClick = onClick
                ) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
                SpacerMedium()
            }
        }
    )
}

@LightDarkPreview
@Composable
fun UpdateAppScreenContentPreview() {
    PreviewAppTheme {
        UpdateAppScreenContent {}
    }
}
