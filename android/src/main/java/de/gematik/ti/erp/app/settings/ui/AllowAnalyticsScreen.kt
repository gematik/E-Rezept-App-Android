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

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import java.util.Locale

@Composable
fun AllowAnalyticsScreen(onAllowAnalytics: (Boolean) -> Unit) {
    val context = LocalContext.current
    val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
    val allowText = annotatedStringResource(
        R.string.settings_tracking_allow_info,
        annotatedStringBold(allowStars)
    ).toString()
    val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                title = stringResource(R.string.settings_tracking_allow_title),
            ) { onAllowAnalytics(false) }
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer24()
                TextButton(
                    onClick = {
                        onAllowAnalytics(false)
                        Toast.makeText(context, disAllowToast, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.settings_tracking_not_allow).uppercase(Locale.getDefault()))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        onAllowAnalytics(true)
                        Toast.makeText(context, allowText, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.settings_tracking_allow).uppercase(Locale.getDefault()))
                }
                Spacer24()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = (PaddingDefaults.XLarge * 2)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.settings_tracking_dialog_title),
                style = MaterialTheme.typography.h6,
                color = AppTheme.colors.neutral999,
            )
            Spacer8()
            Text(
                stringResource(R.string.settings_tracking_dialog_text_1),
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral999,
            )
            Spacer8()
            Text(
                stringResource(R.string.settings_tracking_dialog_text_2),
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral999
            )
            Spacer8()
            Text(
                stringResource(R.string.settings_tracking_dialog_text_3),
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral999,
            )
            Spacer16()
        }
    }
}
