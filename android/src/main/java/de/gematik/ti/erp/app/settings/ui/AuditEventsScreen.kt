/*
 * Copyright (c) 2023 gematik GmbH
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.ProfilesController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuditEventsScreen(
    profileId: ProfileIdentifier,
    profilesController: ProfilesController,
    lastAuthenticated: Instant?,
    tokenValid: Boolean,
    onBack: () -> Unit
) {
    val header = stringResource(R.string.autitEvents_headline)
    val auditEventPagingFlow = remember(profileId) { profilesController.loadAuditEventsForProfile(profileId) }
    val pagingItems = auditEventPagingFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Profile.AuditEvents.AuditEventsScreen),
        listState = listState,
        topBarTitle = header,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) { innerPadding ->

        val infoText = if (lastAuthenticated == null) {
            stringResource(R.string.no_audit_events_not_authenticated_info)
        } else {
            stringResource(R.string.no_audit_events_empty_protocol_list_info)
        }

        if (pagingItems.itemCount == 0) {
            LazyColumn(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        stringResource(R.string.no_audit_events_header),
                        modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventHeader),
                        style = AppTheme.typography.subtitle1
                    )
                    SpacerSmall()
                    Text(
                        infoText,
                        style = AppTheme.typography.body2l,
                        modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventInfo),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = listState,
                contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
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
                                lastAuthenticated?.toLocalDateTime(TimeZone.currentSystemDefault())
                                    ?.toJavaLocalDateTime() ?: LocalDateTime.MIN
                            }

                            Text(
                                stringResource(
                                    id = R.string.audit_events_updated_at,
                                    phrasedDateString(date = lastAuthenticatedDate)
                                ),
                                style = AppTheme.typography.caption1l,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                itemsIndexed(pagingItems) { _, auditEvent ->
                    auditEvent?.let {
                        Column(
                            modifier = Modifier.padding(PaddingDefaults.Medium)
                                .testTag(TestTag.Profile.AuditEvents.AuditEvent)
                        ) {
                            auditEvent.medicationText?.let {
                                Text(
                                    it,
                                    style = AppTheme.typography.subtitle1
                                )
                            }

                            Text(auditEvent.description, style = AppTheme.typography.body2)

                            val timestamp = remember {
                                auditEvent.timestamp
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toJavaLocalDateTime()
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
