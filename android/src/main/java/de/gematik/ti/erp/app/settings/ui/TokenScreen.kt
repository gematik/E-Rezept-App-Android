/*
 * Copyright (c) 2022 gematik GmbH
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

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar

@Composable
fun TokenScreen(onBack: () -> Unit, ssoToken: String?, accessToken: String?) {
    val header = stringResource(id = R.string.token_headline)

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                title = header,
                onBack = { onBack() }
            )
        },
    ) {
        val accessTokenTitle = stringResource(id = R.string.access_token_title)
        val singleSignOnTokenTitle = stringResource(id = R.string.single_sign_on_token_title)

        LazyColumn(
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                applyBottom = true
            )
        ) {
            item {
                TokenLabel(
                    title = accessTokenTitle,
                    text = accessToken ?: stringResource(id = R.string.no_access_token),
                    tokenAvailable = accessToken != null
                )
                Divider(modifier = Modifier.padding(start = PaddingDefaults.Medium))
            }
            item {
                TokenLabel(
                    title = singleSignOnTokenTitle,
                    text = ssoToken
                        ?: stringResource(id = R.string.no_single_sign_on_token),
                    tokenAvailable = ssoToken != null
                )
            }
        }
    }
}

@Composable
private fun TokenLabel(title: String, text: String, tokenAvailable: Boolean) {

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val copied = stringResource(R.string.copied)
    val description = if (tokenAvailable) {
        stringResource(R.string.copy_content_description)
    } else {
        stringResource(R.string.copied)
    }

    val mod = if (tokenAvailable) {
        Modifier
            .clickable(onClick = {
                if (tokenAvailable) {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast
                        .makeText(context, "$title $copied", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .semantics { contentDescription = description }
    } else {
        Modifier
    }

    Row(
        modifier = mod
    ) {
        Row(
            modifier = Modifier
                .sizeIn(maxHeight = 200.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.subtitle1)
                LazyColumn() {
                    item {
                        Text(text, style = MaterialTheme.typography.body2)
                    }
                }
            }

            if (tokenAvailable) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = PaddingDefaults.Medium)
                ) {
                    Icon(Icons.Outlined.ContentCopy, null, tint = AppTheme.colors.neutral400)
                }
            }
        }
    }
}
