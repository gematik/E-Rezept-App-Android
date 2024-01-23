/*
 * Copyright (c) 2024 gematik GmbH
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.presentation.rememberMainScreenController
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.prescription.ui.rememberRefreshPrescriptionsController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.settings.AuditEventsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ConnectBottomBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Requirement(
    "O.Auth_5#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Screen displaying the audit events."
)
@Composable
fun AuditEventsScreen(
    profileId: ProfileIdentifier,
    onShowCardWall: () -> Unit,
    auditEventsController: AuditEventsController,
    tokenValid: Boolean,
    onBack: () -> Unit
) {
    val header = stringResource(R.string.autitEvents_headline)
    val auditItems = auditEventsController.auditEventPagingFlow.collectAsLazyPagingItems()

    val listState = rememberLazyListState()
    val mainScreenController = rememberMainScreenController()
    val refreshPrescriptionsController = rememberRefreshPrescriptionsController(mainScreenController)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        auditEventsController.refresh(profileId, false, {}, {})
    }

    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Profile.AuditEvents.AuditEventsScreen),
        listState = listState,
        topBarTitle = header,
        onBack = onBack,
        bottomBar = {
            if (!tokenValid) {
                ConnectBottomBar(
                    infoText = stringResource(R.string.audit_events_connect_info)
                ) {
                    scope.launch {
                        refreshPrescriptionsController.refresh(
                            profileId = profileId,
                            isUserAction = true,
                            onUserNotAuthenticated = {},
                            onShowCardWall = onShowCardWall
                        )
                    }
                }
            }
        },
        navigationMode = NavigationBarMode.Back
    ) { innerPadding ->

        RefreshScaffold(
            profileId = profileId,
            onUserNotAuthenticated = {},
            mainScreenController = mainScreenController,
            onShowCardWall = onShowCardWall
        ) {
            if (auditItems.itemCount == 0) {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    state = listState
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(PaddingDefaults.Medium).fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.no_audit_events_header),
                                modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventHeader),
                                style = AppTheme.typography.subtitle1
                            )
                            if (tokenValid) {
                                SpacerSmall()
                                Text(
                                    stringResource(R.string.no_audit_events_empty_protocol_list_info),
                                    style = AppTheme.typography.body2l,
                                    modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventInfo),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    state = listState,
                    contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                ) {
                    items(
                        count = auditItems.itemCount,
                        key = auditItems.itemKey { it.auditId }
                    ) { index ->
                        val auditEvent = auditItems[index]
                        auditEvent?.let {
                            Column(
                                modifier = Modifier.padding(PaddingDefaults.Medium)
                                    .testTag(TestTag.Profile.AuditEvents.AuditEvent)
                            ) {
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
}
