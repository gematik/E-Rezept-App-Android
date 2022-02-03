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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R

import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuditEventsScreen(
    profileName: String,
    viewModel: SettingsViewModel,
    lastAuthenticated: Instant?,
    tokenValid: Boolean,
    onBack: () -> Unit
) {
    val header = stringResource(id = R.string.autitEvents_headline)
    val auditEventPagingFlow = remember { viewModel.loadAuditEventsForProfile(profileName) }
    val pagingItems = auditEventPagingFlow.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                title = header,
                onBack = onBack
            )
        },
    ) { innerPadding ->

        val infoText = if (lastAuthenticated == null) {
            stringResource(R.string.no_audit_events_not_authenticated_info)
        } else {
            stringResource(R.string.no_audit_events_empty_protocol_list_info)
        }

        if (lastAuthenticated == null || pagingItems.itemCount == 0) {
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.no_audit_events_header),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    infoText,
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            }
        } else {

            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars,
                    applyBottom = true
                )
            ) {

                if (!tokenValid) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(
                                    top = PaddingDefaults.Medium,
                                    bottom = PaddingDefaults.Small,
                                    start = PaddingDefaults.Medium,
                                    end = PaddingDefaults.Medium
                                )
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val lastAuthenticatedDate = remember {
                                LocalDateTime.ofInstant(lastAuthenticated, ZoneId.systemDefault())
                            }

                            Text(
                                stringResource(
                                    id = R.string.audit_events_updated_at,
                                    phrasedDateString(date = lastAuthenticatedDate)
                                ),
                                style = AppTheme.typography.captionl,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                itemsIndexed(pagingItems) { _, auditEvent ->
                    auditEvent?.let {
                        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                            if (auditEvent.medicationText != null) {
                                Text(
                                    auditEvent.medicationText,
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                            Text(auditEvent.text, style = MaterialTheme.typography.body2)

                            val timestamp = remember {
                                auditEvent.timeStamp.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                            }

                            Text(
                                phrasedDateString(date = timestamp),
                                style = AppTheme.typography.body2l
                            )
                        }
                    }
                }
            }
        }
    }
}
