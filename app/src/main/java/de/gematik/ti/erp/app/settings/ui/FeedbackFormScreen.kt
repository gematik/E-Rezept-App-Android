/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import java.util.Locale

@Composable
fun FeedbackForm(navController: NavController) {
    var sendEnabled by remember { mutableStateOf(false) }
    var body by rememberSaveable { mutableStateOf("") }
    var subject = "Feedback aus der E-Rezept App"
    val mailAddress = stringResource(R.string.settings_contact_mail_address)

    val context = LocalContext.current
    val darkMode = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.settings_feedback_form_headline),
            ) { navController.popBackStack() }
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        context.handleIntent(
                            provideEmailIntent(
                                mailAddress,
                                body = buildBodyWithDeviceInfo(body, darkMode),
                                subject = subject
                            )
                        )
                    },
                    enabled = sendEnabled,
                    shape = RoundedCornerShape(PaddingDefaults.Small)
                ) {
                    Text(stringResource(R.string.settings_feedback_form_send).uppercase(Locale.getDefault()))
                }
                SpacerMedium()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingDefaults.Medium)
        ) {
            Text(stringResource(R.string.settings_feedback_form_header), style = MaterialTheme.typography.h6)

            SpacerMedium()

            OutlinedTextField(
                value = body,
                onValueChange = {
                    body = it
                    sendEnabled = body.isNotBlank()
                },
                textStyle = MaterialTheme.typography.body2,
                placeholder = {
                    Text(
                        stringResource(R.string.settings_feedback_form_placeholder),
                        style = MaterialTheme.typography.body2
                    )
                },
                shape = RoundedCornerShape(PaddingDefaults.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp)
            )

            CompositionLocalProvider(
                LocalTextStyle provides AppTheme.typography.body2l,
            ) {
                SpacerSmall()
                Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                    Text(stringResource(R.string.seetings_feedback_form_additional_data_info))
                    val os = annotatedStringResource(
                        R.string.seetings_feedback_form_additional_data_os,
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        Build.VERSION.SECURITY_PATCH
                    )
                    val device = annotatedStringResource(
                        R.string.seetings_feedback_form_additional_data_device,
                        Build.MANUFACTURER,
                        Build.MODEL,
                        Build.PRODUCT
                    )
                    Text(os)
                    Text(device)
                    val darkModeText = stringResource(
                        if (darkMode) {
                            R.string.seetings_feedback_form_additional_data_darkmode_on
                        } else {
                            R.string.seetings_feedback_form_additional_data_darkmode_off
                        }
                    )
                    Text(darkModeText)
                    Text(
                        annotatedStringResource(
                            R.string.seetings_feedback_form_additional_data_language,
                            Locale.getDefault().displayName
                        )
                    )
                }
            }
        }
    }
}

private fun buildBodyWithDeviceInfo(userBody: String, darkMode: Boolean): String =
    """$userBody
      |
      |
      |Systeminformationen
      |
      |Betriebssystem: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) (PATCH ${Build.VERSION.SECURITY_PATCH})
      |Modell: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})
      |App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_HASH})
      |DarkMode: ${if (darkMode) "an" else "aus"}
      |Sprache: ${Locale.getDefault().displayName}
      |
    """.trimMargin()
