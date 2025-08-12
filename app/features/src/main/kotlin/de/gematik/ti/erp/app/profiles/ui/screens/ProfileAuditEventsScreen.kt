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

package de.gematik.ti.erp.app.profiles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.base.ClipBoardCopy.copyToClipboardWithHaptic
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.fhir.audit.model.FhirAuditEventErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberAuditEventsController
import de.gematik.ti.erp.app.profiles.ui.components.AuditEventsLoading
import de.gematik.ti.erp.app.pulltorefresh.PullToRefresh
import de.gematik.ti.erp.app.pulltorefresh.extensions.trigger
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.ConnectBottomBar
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Requirement(
    "O.Auth_6#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Screen displaying the audit events."
)
class ProfileAuditEventsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val dialog = LocalDialog.current
        val pullToRefreshState = rememberPullToRefreshState()
        val intentHandler = LocalIntentHandler.current

        val profileId = remember { ProfileRoutes.getProfileId(navBackStackEntry) }

        val auditController = rememberAuditEventsController(profileId)
        val activeProfile by auditController.activeProfile.collectAsStateWithLifecycle()
        val auditEvents = auditController.auditEvents.collectAsLazyPagingItems()
        val isSsoTokenValid by auditController.isSsoTokenValidForSelectedProfile.collectAsStateWithLifecycle()

        navBackStackEntry.onReturnAction(ProfileRoutes.ProfileAuditEventsScreen) {
            auditController.refreshCombinedProfile()
        }

        ChooseAuthenticationNavigationEventsListener(auditController, navController)
        with(auditController) {
            refreshStartedEvent.listen {
                pullToRefreshState.endRefresh()
            }
        }

        AuthenticationFailureDialog(
            event = auditController.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )

        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                auditController.refreshCombinedProfile()
                auditController.refreshAuditEvents()
            }

            if (isSsoTokenValid) {
                // refresh audit events
                auditController.refreshAuditEvents()
            }
        }

        with(pullToRefreshState) {
            trigger(
                onStartRefreshing = { startRefresh() },
                block = {
                    if (isSsoTokenValid) {
                        auditController.refreshAuditEvents()
                    }
                },
                onNavigation = {
                    if (!isSsoTokenValid) {
                        activeProfile.data?.let { auditController.chooseAuthenticationMethod(it) }
                    }
                }

            )
        }

        AuditEventsScaffold(
            listState = listState,
            pullToRefreshState = pullToRefreshState,
            isSsoTokenValid = isSsoTokenValid,
            auditEvents = auditEvents,
            onBack = { navController.popBackStack() },
            onInvalidSsoToken = { activeProfile.data?.let { auditController.chooseAuthenticationMethod(it) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditEventsScaffold(
    listState: LazyListState,
    pullToRefreshState: PullToRefreshState,
    isSsoTokenValid: Boolean,
    auditEvents: LazyPagingItems<FhirAuditEventErpModel>,
    onInvalidSsoToken: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Profile.AuditEvents.AuditEventsScreen),
        listState = listState,
        topBarTitle = stringResource(R.string.autitEvents_headline),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        onBack = onBack,
        bottomBar = {
            if (!isSsoTokenValid) {
                ConnectBottomBar(
                    infoText = stringResource(R.string.audit_events_connect_info)
                ) {
                    onInvalidSsoToken()
                }
            }
        },
        navigationMode = NavigationBarMode.Back
    ) { _ ->
        RefreshAuditEventsContent(
            listState = listState,
            pullToRefreshState = pullToRefreshState,
            isSsoTokenValid = isSsoTokenValid,
            auditEvents = auditEvents
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshAuditEventsContent(
    listState: LazyListState,
    pullToRefreshState: PullToRefreshState,
    isSsoTokenValid: Boolean,
    auditEvents: LazyPagingItems<FhirAuditEventErpModel>
) {
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            state = listState
        ) {
            auditEvents.apply {
                AuditEventsScreenContent(this)

                when {
                    loadState.isLoadingDataOnRefresh() -> ShimmerLoadingAuditEvents()
                    loadState.isLoadingDataOnAppend() -> LastItemLoadingIndicator()
                    loadState.isErrorState() -> AuditEventsEmptyScreenContent(isSsoTokenValid)
                    else -> {
                        // show nothing
                    }
                }
            }
        }
        PullToRefresh(
            modifier = Modifier.align(Alignment.TopCenter),
            pullToRefreshState = pullToRefreshState
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.ShimmerLoadingAuditEvents() {
    item {
        AuditEventsLoading()
    }
}

@Suppress("FunctionName")
private fun LazyListScope.LastItemLoadingIndicator() {
    item {
        Center {
            CircularProgressIndicator()
        }
    }
}

@Suppress("FunctionName")
internal fun LazyListScope.AuditEventsEmptyScreenContent(
    isSsoTokenValid: Boolean
) {
    item {
        Column(
            modifier = Modifier
                .padding(PaddingDefaults.Medium)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.no_audit_events_header),
                modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventHeader),
                style = AppTheme.typography.subtitle1
            )
            SpacerSmall()
            Text(
                text = if (isSsoTokenValid) {
                    stringResource(R.string.no_audit_events_empty_protocol_list_info)
                } else {
                    stringResource(R.string.no_audit_events_not_logged_in_protocol_list_info)
                },
                style = AppTheme.typography.body2l,
                modifier = Modifier.testTag(TestTag.Profile.AuditEvents.NoAuditEventInfo),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Requirement(
    "O.Auth_6#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "UI component to display the audit events."
)
@Suppress("FunctionName")
internal fun LazyListScope.AuditEventsScreenContent(
    auditEvents: LazyPagingItems<FhirAuditEventErpModel>
) {
    items(
        count = auditEvents.itemCount,
        key = auditEvents.itemKey { "${it.id}-${it.timestamp}-${it.uuid}}".hashCode() }
    ) { index ->
        val auditEvent = auditEvents[index]
        auditEvent?.let { AuditEvent(it) }
    }
}

@Composable
private fun AuditEvent(auditEvent: FhirAuditEventErpModel) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val telematikIdText = stringResource(R.string.telematik_id)
    val kvnrText = stringResource(R.string.insurance_information_insurance_identifier)

    val lines = mutableListOf<String>()
    lines += auditEvent.description
    auditEvent.telematikId?.takeIf { it.isNotBlank() }?.let { lines += "$telematikIdText: $it" }
    auditEvent.kvnrNumber?.takeIf { it.isNotBlank() }?.let { lines += "$kvnrText: $it" }
    val textToCopy = lines.joinToString(separator = "\n")

    Column(
        modifier = Modifier
            .padding(PaddingDefaults.Medium)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        copyToClipboardWithHaptic(
                            label = "AuditEvent",
                            text = textToCopy,
                            context = context,
                            hapticFeedback = hapticFeedback
                        )
                    }
                )
            }
            .testTag(TestTag.Profile.AuditEvents.AuditEvent)
    ) {
        Text(auditEvent.description, style = AppTheme.typography.body2)

        AnimatedVisibility(auditEvent.telematikId.isNotNullOrEmpty()) {
            SpacerSmall()
            Text(
                "$telematikIdText: ${auditEvent.telematikId}",
                style = AppTheme.typography.body2
            )
        }

        AnimatedVisibility(auditEvent.kvnrNumber.isNotNullOrEmpty()) {
            SpacerSmall()
            Text(
                "$kvnrText: ${auditEvent.kvnrNumber}",
                style = AppTheme.typography.body2
            )
        }

        val timestamp = remember {
            auditEvent.timestamp.value
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toJavaLocalDateTime()
        }
        SpacerSmall()
        Text(
            phrasedDateString(date = timestamp),
            style = AppTheme.typography.body2l
        )
    }
}

private fun CombinedLoadStates.isLoadingDataOnRefresh() = refresh is LoadState.Loading

private fun CombinedLoadStates.isLoadingDataOnAppend() = append is LoadState.Loading

private fun CombinedLoadStates.isErrorState() = refresh is LoadState.Error || append is LoadState.Error

@Suppress("UnusedPrivateMember")
@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
private fun AuditEventsListPreview() {
    val items = listOf(
        FhirAuditEventErpModel(
            id = "123",
            description = "User A downloaded prescription with Task ID 160.000.12.2",
            timestamp = FhirTemporal.Instant(Instant.parse("2023-10-01T12:00:00Z")),
            uuid = "uuid-123",
            taskId = "160.000.12.2",
            telematikId = "1.2.3.4.5.6",
            kvnrNumber = "X123456789"
        ),
        FhirAuditEventErpModel(
            id = "456",
            description = "Arztpraxis Bernd Ulmen activated prescription with Task ID 160.000.12.3",
            timestamp = FhirTemporal.Instant(Instant.parse("2023-10-02T09:00:00Z")),
            uuid = "uuid-456",
            taskId = "160.000.12.3",
            telematikId = "6.5.4.3.2.1",
            kvnrNumber = ""
        )
    )

    val pagingData = flowOf(PagingData.from(items))
    val lazyPagingItems = pagingData.collectAsLazyPagingItems()

    PreviewAppTheme {
        AuditEventsScaffold(
            listState = rememberLazyListState(),
            pullToRefreshState = rememberPullToRefreshState(),
            auditEvents = lazyPagingItems,
            isSsoTokenValid = true,
            onInvalidSsoToken = {},
            onBack = {}
        )
    }
}
