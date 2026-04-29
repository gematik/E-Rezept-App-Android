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

@file:Suppress("UnusedPrivateMember", "MagicNumber")

package de.gematik.ti.erp.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chuckerteam.chucker.api.Chucker
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.presentation.SaveCredentialsController
import de.gematik.ti.erp.app.cardwall.presentation.rememberSaveCredentialsScreenController
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.database.datastore.featuretoggle.FeatureEntity
import de.gematik.ti.erp.app.database.settings.CommunicationDigaVersion
import de.gematik.ti.erp.app.database.settings.CommunicationVersion
import de.gematik.ti.erp.app.database.settings.ConsentVersion
import de.gematik.ti.erp.app.database.settings.EuVersion
import de.gematik.ti.erp.app.debugsettings.encryption.ui.screens.DebugDatabaseEncryptionScreen
import de.gematik.ti.erp.app.debugsettings.logger.ui.screens.DbMigrationLoggerScreen
import de.gematik.ti.erp.app.debugsettings.logger.ui.screens.LoggerScreen.LoggerScreen
import de.gematik.ti.erp.app.debugsettings.navigation.DebugScreenNavigation
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.ui.screens.PharmacyServiceSelectionScreen
import de.gematik.ti.erp.app.debugsettings.pkv.presentation.rememberDebugPkvController
import de.gematik.ti.erp.app.debugsettings.pkv.ui.DebugScreenPKV
import de.gematik.ti.erp.app.debugsettings.presentation.DebugSettingsViewModel
import de.gematik.ti.erp.app.debugsettings.qrcode.QrCodeScannerScreen
import de.gematik.ti.erp.app.debugsettings.timeout.DebugTimeoutScreen
import de.gematik.ti.erp.app.debugsettings.ui.components.ClearTextTrafficSection
import de.gematik.ti.erp.app.debugsettings.ui.components.ClientIdsSection
import de.gematik.ti.erp.app.debugsettings.ui.components.EnvironmentSelector
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.fhir.constant.FhirProfileUrls
import de.gematik.ti.erp.app.material3.components.switchs.GemSwitch
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.launch
import org.kodein.di.bindProvider
import org.kodein.di.compose.rememberViewModel
import org.kodein.di.compose.subDI
import org.kodein.di.instance

@Composable
private fun VersionDisplay(label: String, urls: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = AppTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primary700
        )
        urls.forEach { url ->
            Text(
                text = url,
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral800,
                modifier = Modifier
                    .padding(start = PaddingDefaults.Small)
                    .padding(vertical = PaddingDefaults.Tiny)
            )
        }
        SpacerMedium()
    }
}

/**
 * Material 3 styled card for debug settings with elevation and modern design.
 */
@Composable
fun DebugCard(
    modifier: Modifier = Modifier,
    title: String,
    onReset: (() -> Unit)? = null,
    collapsible: Boolean = false,
    initiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SizeDefaults.double),
        backgroundColor = AppTheme.colors.neutral025,
        elevation = SizeDefaults.half,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral200)
    ) {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium)
        ) {
            // Header with title and optional reset button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (collapsible) Modifier.clickable { isExpanded = !isExpanded }
                        else Modifier
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = AppTheme.colors.neutral900,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onReset?.let {
                        IconButton(
                            onClick = it,
                            modifier = Modifier.size(SizeDefaults.fivefold)
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Reset",
                                tint = AppTheme.colors.primary600
                            )
                        }
                    }

                    if (collapsible) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = AppTheme.colors.neutral600,
                            modifier = Modifier.size(SizeDefaults.triple)
                        )
                    }
                }
            }

            if (isExpanded) {
                SpacerMedium()
                content()
            }
        }
    }
}

/**
 * Modern action button for debug screen with Material 3 styling.
 * Shows loading state and provides visual feedback.
 */
@Composable
private fun DebugActionButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SizeDefaults.sixfold),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(SizeDefaults.oneHalf),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.primary600,
            disabledBackgroundColor = AppTheme.colors.neutral300
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = SizeDefaults.quarter,
            pressedElevation = SizeDefaults.threeQuarter,
            disabledElevation = SizeDefaults.zero
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(SizeDefaults.doubleHalf),
                color = AppTheme.colors.neutral000,
                strokeWidth = SizeDefaults.quarter
            )
            SpacerSmall()
        }

        icon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.doubleHalf),
                tint = AppTheme.colors.neutral000
            )
            SpacerSmall()
        }

        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = if (enabled && !loading) AppTheme.colors.neutral000 else AppTheme.colors.neutral600,
            style = MaterialTheme.typography.button
        )
    }
}

/**
 * Navigation item with chevron indicator for screens/destinations.
 */
@Composable
private fun DebugNavigationItem(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium, horizontal = PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier.size(SizeDefaults.triple),
                    tint = AppTheme.colors.primary600
                )
                SpacerMedium()
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1,
                    color = AppTheme.colors.neutral900
                )

                subtitle?.let {
                    SpacerTiny()
                    Text(
                        text = it,
                        style = MaterialTheme.typography.caption,
                        color = AppTheme.colors.neutral600
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate",
                tint = AppTheme.colors.neutral400
            )
        }
    }
}

/**
 * Helper to show action result as snackbar.
 */
private suspend fun SnackbarHostState.showActionResult(
    success: Boolean,
    successMessage: String,
    errorMessage: String = "Action failed"
) {
    showSnackbar(
        message = if (success) successMessage else errorMessage,
        duration = SnackbarDuration.Short,
        withDismissAction = true
    )
}

@Composable
private fun EditablePathComponentSetButton(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    active: Boolean,
    onValueChange: (String, Boolean) -> Unit,
    onClick: () -> Unit
) {
    val color = if (active) Color.Green else Color.Red
    val buttonText = if (active) "SAVED" else "SET"
    EditablePathComponentWithControl(
        modifier = modifier,
        label = label,
        textFieldValue = text,
        onValueChange = onValueChange,
        content = {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = color),
                enabled = !active

            ) {
                Text(text = buttonText)
            }
        }
    )
}

@Composable
private fun EditablePathComponentWithControl(
    modifier: Modifier,
    label: String,
    textFieldValue: String,
    onValueChange: (String, Boolean) -> Unit,
    content: @Composable ((Boolean) -> Unit) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        TextField(
            value = textFieldValue,
            onValueChange = { onValueChange(it, false) },
            label = { Text(label) },
            maxLines = 3,
            colors = erezeptTextFieldColors(),
            modifier = Modifier
                .weight(1f)
                .padding(end = PaddingDefaults.Medium)
        )

        content { onValueChange(textFieldValue, it) }
    }
}

@Composable
fun DebugScreen(
    settingsNavController: NavController
) {
    val navController = rememberNavController()
    val navMode by navController.navigationModeState(DebugScreenNavigation.DebugMain.path())

    subDI(diBuilder = {
        bindProvider {
            DebugSettingsViewModel(
                endpointHelper = instance(),
                cardWallUseCase = instance(),
                prescriptionUseCase = instance(),
                saveInvoiceUseCase = instance(),
                vauRepository = instance(),
                idpRepository = instance(),
                idpUseCase = instance(),
                profilesUseCase = instance(),
                featureToggleRepository = instance(),
                getAppUpdateManagerFlagUseCase = instance(),
                changeAppUpdateManagerFlagUseCase = instance(),
                markAllUnreadMessagesAsReadUseCase = instance(),
                deletePrescriptionUseCase = instance(),
                getTaskIdsUseCase = instance(),
                getIknrUseCase = instance(),
                updateIknrUseCase = instance(),
                revokeEuConsentUseCase = instance(),
                consentVersionDataStore = instance(),
                communicationVersionDataStore = instance(),
                communicationDigaVersionDataStore = instance(),
                euVersionDataStore = instance(),
                getAndroid8DeprecationOverrideUseCase = instance(),
                setAndroid8DeprecationOverrideUseCase = instance(),
                resetOnboardingUseCase = instance(),
                virtualHealthCardPrivateKeyDataStore = instance(),
                dispatchers = instance()
            )
        }
    }) {
        val viewModel by rememberViewModel<DebugSettingsViewModel>()
        NavHost(
            navController,
            startDestination = DebugScreenNavigation.DebugMain.path()
        ) {
            composable(DebugScreenNavigation.DebugMain.route) {
                NavigationAnimation(mode = navMode) {
                    DebugScreenMain(
                        viewModel = viewModel,
                        onBack = settingsNavController::popBackStack,
                        onClickPKV = {
                            navController.navigate(DebugScreenNavigation.DebugPKV.path())
                        },
                        onClickVzdSelection = {
                            navController.navigate(DebugScreenNavigation.PharmacyVzdSelectionScreen.path())
                        },
                        onClickBioMetricSettings = {
                            navController.navigate(DebugScreenNavigation.DebugTimeout.path())
                        },
                        onScanQrCode = {
                            navController.navigate(DebugScreenNavigation.QrCodeScannerScreen.path())
                        },
                        onClickLogger = {
                            navController.navigate(DebugScreenNavigation.LoggerScreen.path())
                        },
                        onClickDbMigrationLogger = {
                            navController.navigate(DebugScreenNavigation.DebugDbMigrationLoggerScreen.path())
                        },
                        onClickDatabaseEncryption = {
                            navController.navigate(DebugScreenNavigation.DebugDatabaseEncryption.path())
                        },
                        onLoginSuccess = {
                            settingsNavController.navigateAndClearStack(PrescriptionRoutes.PrescriptionListScreen.route)
                        }
                    )
                }
            }
            composable(DebugScreenNavigation.DebugPKV.route) {
                NavigationAnimation(mode = navMode) {
                    DebugScreenPKV(
                        onSaveInvoiceBundle = {
                            viewModel.saveInvoice(it)
                        },
                        onBack = navController::popBackStack
                    )
                }
            }
            composable(DebugScreenNavigation.DebugTimeout.route) {
                DebugTimeoutScreen.Content {
                    navController.popBackStack()
                }
            }
            composable(DebugScreenNavigation.QrCodeScannerScreen.route) {
                QrCodeScannerScreen.Content(
                    onSaveCertificate = { viewModel.onSetVirtualHealthCardCertificate(it) },
                    onSavePrivateKey = { viewModel.onSetVirtualHealthCardPrivateKey(it) },
                    onBack = navController::popBackStack
                )
            }
            composable(DebugScreenNavigation.LoggerScreen.route) {
                LoggerScreen(onBack = navController::popBackStack)
            }
            composable(DebugScreenNavigation.DebugDbMigrationLoggerScreen.route) { navEntry ->
                DbMigrationLoggerScreen(
                    navController = navController,
                    navBackStackEntry = navEntry
                ).Content()
            }
            composable(DebugScreenNavigation.PharmacyVzdSelectionScreen.route) {
                PharmacyServiceSelectionScreen(onBack = navController::popBackStack)
            }
            composable(DebugScreenNavigation.DebugDatabaseEncryption.route) {
                NavigationAnimation(mode = navMode) {
                    DebugDatabaseEncryptionScreen.Content(onBack = navController::popBackStack)
                }
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
fun DebugScreenMain(
    viewModel: DebugSettingsViewModel,
    onBack: () -> Unit,
    onClickPKV: () -> Unit,
    onClickVzdSelection: () -> Unit,
    onClickBioMetricSettings: () -> Unit,
    onScanQrCode: () -> Unit,
    onClickLogger: () -> Unit,
    onClickDbMigrationLogger: () -> Unit,
    onClickDatabaseEncryption: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentActivity = LocalActivity.current
    val listState = rememberLazyListState()
    val modal = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val appUpdateManager by viewModel.appUpdateManager.collectAsStateWithLifecycle()
    val messageMarkingLoading by viewModel.messageMarkingLoading.collectAsStateWithLifecycle()
    val prescriptionDeletionLoading by viewModel.prescriptionDeletionLoading.collectAsStateWithLifecycle()
    val featureState by viewModel.featureToggles.collectAsStateWithLifecycle()
    val iknr by viewModel.iknr.collectAsStateWithLifecycle()
    val onIknrChangedEvent = viewModel.onIknrChangedEvent

    ModalBottomSheetLayout(
        sheetContent = {
            EnvironmentSelector(
                currentSelectedEnvironment = viewModel.getCurrentEnvironment(),
                onSelectEnvironment = { viewModel.selectEnvironment(it) }
            ) {
                scope.launch { viewModel.saveAndRestartApp() }
            }
        },
        sheetState = modal
    ) {
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.DebugMenu.DebugMenuScreen),
            navigationMode = NavigationBarMode.Close,
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            listState = listState,
            topBarTitle = "Secret switches",
            snackbarHost = { SnackbarHost(snackbarHostState) },
            onBack = onBack
        ) { innerPadding ->

            LaunchedEffect(Unit) {
                viewModel.state()
            }

            onIknrChangedEvent.listen {
                snackbarHostState.showSnackbar(
                    message = "IKNR updated",
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .testTag(TestTag.DebugMenu.DebugMenuContent),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                contentPadding = PaddingValues(PaddingDefaults.Medium)
            ) {
                // 1. Environment Selection - FIRST
                item {
                    DebugCard(title = "Environment") {
                        OutlinedDebugButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Select Environment",
                            onClick = { scope.launch { modal.show() } }
                        )
                    }
                }

                // 2. Feature Toggles - SECOND (collapsible)
                item {
                    FeatureToggles(
                        viewModel = viewModel,
                        featureState = featureState,
                        collapsible = true,
                        initiallyExpanded = false
                    ) {
                        DemoModeIntent.startAppWithNormalMode<MainActivity>(currentActivity)
                    }
                }

                // 3. Authentication (Break SSO Token) - THIRD
                item {
                    DebugCard(
                        title = "Authentication"
                    ) {
                        EditablePathComponentSetButton(
                            label = "Bearer Token",
                            text = viewModel.debugSettingsData.bearerToken,
                            active = viewModel.debugSettingsData.bearerTokenIsSet,
                            onValueChange = { text, _ ->
                                viewModel.updateState(
                                    viewModel.debugSettingsData.copy(
                                        bearerToken = text,
                                        bearerTokenIsSet = false
                                    )
                                )
                            },
                            onClick = {
                                viewModel.changeBearerToken(viewModel.debugSettingsData.activeProfileId)
                            }
                        )
                        SpacerMedium()
                        DebugActionButton(
                            text = "Break SSO Token",
                            onClick = {
                                scope.launch {
                                    viewModel.breakSSOToken()
                                    snackbarHostState.showActionResult(
                                        success = true,
                                        successMessage = "SSO Token broken"
                                    )
                                }
                            }
                        )
                    }
                }

                // 4. General (collapsible)
                item {
                    DebugCard(
                        title = "General",
                        collapsible = true,
                        initiallyExpanded = false
                    ) {
                        DebugNavigationItem(
                            icon = painterResource(R.drawable.ic_finger),
                            text = "Biometric settings",
                            onClick = onClickBioMetricSettings
                        )
                        SpacerTiny()
                        DebugNavigationItem(
                            icon = painterResource(R.drawable.ic_pkv),
                            text = "FhirVzd",
                            onClick = onClickVzdSelection
                        )
                    }
                }

                // 5. Logging (collapsible)
                item {
                    DebugCard(
                        title = "Logging",
                        collapsible = true,
                        initiallyExpanded = false
                    ) {
                        DebugNavigationItem(
                            icon = rememberVectorPainter(Icons.Rounded.AddRoad),
                            text = "External Logger",
                            subtitle = "Network and API logs",
                            onClick = {
                                val intent = Chucker.getLaunchIntent(context)
                                context.startActivity(intent)
                            }
                        )
                        SpacerTiny()
                        DebugNavigationItem(
                            icon = rememberVectorPainter(Icons.Rounded.Adb),
                            text = "Internal Logger",
                            subtitle = "Application logs",
                            onClick = onClickLogger
                        )
                        SpacerTiny()
                        DebugNavigationItem(
                            icon = rememberVectorPainter(Icons.Outlined.DataExploration),
                            text = "DB Migration Logger",
                            subtitle = "Database migration history",
                            onClick = onClickDbMigrationLogger
                        )
                    }
                }

                // 6. Database Encryption
                item {
                    DebugCard(title = "Database Encryption") {
                        DebugNavigationItem(
                            icon = rememberVectorPainter(Icons.Rounded.LockReset),
                            text = "DB Encryption",
                            subtitle = "Toggle encryption, view and delete key",
                            onClick = onClickDatabaseEncryption
                        )
                    }
                }

                item {
                    DebugCard(
                        title = "IKNR"
                    ) {
                        Column {
                            Text(
                                text = "Identification Number of the Health Insurance Provider",
                                style = AppTheme.typography.body2l,
                                modifier = Modifier.padding(bottom = PaddingDefaults.Medium)
                            )
                            ErezeptOutlineText(
                                modifier = Modifier.fillMaxWidth(),
                                value = iknr,
                                label = "Modify Iknr",
                                placeholder = "108018007",
                                onValueChange = viewModel::updateIknr,
                                trailingIcon = {
                                    Box(Modifier.padding(SizeDefaults.one)) {
                                        Icon(Icons.Rounded.Bookmarks, null)
                                    }
                                }
                            )
                            SpacerSmall()
                            Text(
                                text = "Set to 101570104",
                                style = AppTheme.typography.body2l,
                                color = AppTheme.colors.primary700,
                                modifier = Modifier
                                    .padding(bottom = PaddingDefaults.Medium)
                                    .clickable { viewModel.updateIknr("101570104") }
                            )
                            DebugActionButton(
                                text = "Save IKNR",
                                icon = rememberVectorPainter(Icons.Rounded.Bookmarks),
                                onClick = viewModel::saveIknr
                            )
                        }
                    }
                }

                item {
                    ConsentVersionSelector(viewModel)
                }

                item {
                    CommunicationVersionSelector(viewModel)
                }

                item {
                    CommunicationDigaVersionSelector(viewModel)
                }

                // FHIR supported versions (auto-updates from enums in FhirVersions)
                item {
                    DebugCard(
                        title = "FHIR Supported Versions",
                        collapsible = true,
                        initiallyExpanded = false
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                        ) {
                            VersionDisplay("Task entry profiles", FhirProfileUrls.TASK_PROFILE_URLS)
                            VersionDisplay("KBV PR ERP Bundle (supported)", FhirProfileUrls.KBV_BUNDLE_PROFILE_URLS)
                            VersionDisplay("KBV EVDGA DeviceRequest", FhirProfileUrls.KBV_DEVICE_REQUEST_PROFILE_URLS)
                            VersionDisplay("MedicationDispense profiles", FhirProfileUrls.MEDICATION_DISPENSE_PROFILE_URLS)
                            VersionDisplay("Consent profiles", FhirProfileUrls.CONSENT_PROFILE_URLS)
                            VersionDisplay("Communication profiles", FhirProfileUrls.COMMUNICATION_PROFILE_URLS)
                            VersionDisplay("Communication DiGA profiles", FhirProfileUrls.COMMUNICATION_DIGA_PROFILE_URLS)
                            VersionDisplay("MedicationRequest profiles", FhirProfileUrls.MEDICATION_REQUEST_PROFILE_URLS)
                            VersionDisplay("EuRedeem profiles", FhirProfileUrls.EUREDEEM_PROFILE_URLS)
                        }
                    }
                }

                item {
                    DebugCard(
                        title = "EU Rezept"
                    ) {
                        // EU Version selector content (without outer card)
                        val euVersion by viewModel.euVersion.collectAsStateWithLifecycle()

                        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
                            Text(
                                text = "Eu Version",
                                style = AppTheme.typography.subtitle1,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Select Eu version for testing",
                                style = MaterialTheme.typography.body2,
                                color = AppTheme.colors.neutral600,
                                lineHeight = 20.sp
                            )

                            // Switch layout for 2 versions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                            ) {
                                listOf(EuVersion.V_1_0, EuVersion.V_1_1).forEach { version ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = SizeDefaults.tenfold)
                                            .clickable { viewModel.setEuVersion(version) },
                                        shape = RoundedCornerShape(SizeDefaults.one),
                                        color = if (euVersion == version) {
                                            AppTheme.colors.primary100
                                        } else {
                                            Color.Transparent
                                        },
                                        border = if (euVersion == version) {
                                            BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
                                        } else {
                                            BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
                                        },
                                        elevation = if (euVersion == version) SizeDefaults.quarter else SizeDefaults.zero
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(PaddingDefaults.Medium),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = version.displayName,
                                                    style = AppTheme.typography.body2,
                                                    color = if (euVersion == version) {
                                                        AppTheme.colors.primary600
                                                    } else {
                                                        AppTheme.colors.neutral999
                                                    },
                                                    fontWeight = if (euVersion == version) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                                // Always render spacer to maintain height
                                                SpacerTiny()
                                                // Current badge or empty space
                                                if (euVersion == version) {
                                                    Surface(
                                                        shape = RoundedCornerShape(SizeDefaults.oneHalf),
                                                        color = AppTheme.colors.primary100
                                                    ) {
                                                        Text(
                                                            text = "Current",
                                                            style = AppTheme.typography.caption1,
                                                            color = AppTheme.colors.primary600,
                                                            modifier = Modifier.padding(
                                                                horizontal = SizeDefaults.one,
                                                                vertical = SizeDefaults.half
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    // Empty spacer with same height as badge to maintain consistent sizing
                                                    Spacer(modifier = Modifier.height(SizeDefaults.doubleHalf))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        SpacerMedium()
                        HorizontalDivider(color = AppTheme.colors.neutral200)
                        SpacerMedium()
                        Text(
                            text = "Revoke Consent",
                            style = AppTheme.typography.subtitle1,
                            fontWeight = FontWeight.SemiBold
                        )
                        SpacerSmall()
                        DebugActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Revoke EU Consent",
                            icon = rememberVectorPainter(Icons.Rounded.LockReset),
                            onClick = {
                                scope.launch {
                                    viewModel.onRevokeEuConsent()
                                    snackbarHostState.showActionResult(
                                        success = true,
                                        successMessage = "EU consent revoked successfully"
                                    )
                                }
                            }
                        )
                    }
                }

                item {
                    DebugCard(
                        title = "Batch Actions"
                    ) {
                        DebugActionButton(
                            text = "Trigger Prescription Refresh",
                            icon = painterResource(R.drawable.ic_prescription_refresh),
                            onClick = {
                                scope.launch {
                                    viewModel.refreshPrescriptions()
                                    snackbarHostState.showActionResult(
                                        success = true,
                                        successMessage = "Prescription refresh triggered"
                                    )
                                }
                            }
                        )
                        SpacerMedium()
                        DebugActionButton(
                            text = "Mark All Messages As Read",
                            loading = messageMarkingLoading,
                            onClick = {
                                viewModel.markAllUnreadMessagesAsRead { result ->
                                    scope.launch {
                                        snackbarHostState.showActionResult(
                                            success = result.isSuccess,
                                            successMessage = "All messages marked as read",
                                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to mark messages as read"
                                        )
                                    }
                                }
                            }
                        )

                        SpacerMedium()
                        DebugActionButton(
                            text = "Delete All Prescriptions",
                            loading = prescriptionDeletionLoading,
                            onClick = {
                                viewModel.deleteAllPrescriptions(
                                    profileId = viewModel.debugSettingsData.activeProfileId,
                                    deleteLocallyOnly = false
                                ) { result ->
                                    scope.launch {
                                        snackbarHostState.showActionResult(
                                            success = result.isSuccess,
                                            successMessage = "Successfully deleted all prescriptions",
                                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete prescriptions"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Virtual Health Card
                item {
                    VirtualHealthCard(
                        viewModel = viewModel,
                        onScanQrCode = onScanQrCode,
                        onLoginSuccess = onLoginSuccess
                    )
                }

                // App Update - near bottom
                item {
                    DebugCard(
                        title = "App Update"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (appUpdateManager) "Using vau backend check" else "Using fake to force update",
                                modifier = Modifier.weight(1f)
                            )
                            GemSwitch(
                                modifier = Modifier.testTag(TestTag.DebugMenu.FakeAppUpdate),
                                checked = appUpdateManager,
                                onCheckedChange = { viewModel.changeAppUpdateManager(it) }
                            )
                        }
                    }
                }

                // Card Wall - near bottom
                item {
                    DebugCard(
                        title = "Card Wall"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fake NFC Capability",
                                modifier = Modifier.weight(1f)
                            )
                            GemSwitch(
                                modifier = Modifier.testTag(TestTag.DebugMenu.FakeNFCCapabilities),
                                checked = viewModel.debugSettingsData.fakeNFCCapabilities,
                                onCheckedChange = { viewModel.allowNfc(it) }
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider()
                }
                item {
                    ClearTextTrafficSection()
                }
                item {
                    HorizontalDivider()
                }
                item {
                    ClientIdsSection(viewModel.debugSettingsData.clientId)
                }
            }
        }
    }
}

@Composable
private fun VirtualHealthCard(
    modifier: Modifier = Modifier,
    viewModel: DebugSettingsViewModel,
    onScanQrCode: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val saveCredentialsController = rememberSaveCredentialsScreenController()

    val virtualHealthCardLoading by viewModel.virtualHealthCardLoading.collectAsStateWithLifecycle()
    val virtualHealthCardError by viewModel.virtualHealthCardError.collectAsStateWithLifecycle()
    val pairingLoading by viewModel.pairingLoading.collectAsStateWithLifecycle()
    val pairingError by viewModel.pairingError.collectAsStateWithLifecycle()

    viewModel.onVirtualHealthCardLoginSuccessEvent.listen { onLoginSuccess() }
    viewModel.onPairingLoginSuccessEvent.listen { onLoginSuccess() }

    DebugCard(modifier, title = "Virtual Health Card") {
        val scope = rememberCoroutineScope()

        virtualHealthCardError?.let { error ->
            AlertDialog(onDismissRequest = {
                viewModel.clearVirtualHealthCardError()
            }, buttons = {
                    Button(onClick = { viewModel.clearVirtualHealthCardError() }) { Text("OK") }
                }, text = { Text(error) })
        }

        pairingError?.let { error ->
            AlertDialog(onDismissRequest = { viewModel.clearPairingError() }, buttons = {
                Button(onClick = { viewModel.clearPairingError() }) { Text("OK") }
            }, text = { Text(error) })
        }

        DebugActionButton(
            text = "Scan Virtual Health Card",
            onClick = onScanQrCode
        )

        SpacerMedium()

        ErezeptOutlineText(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.CertificateField)
                .heightIn(max = SizeDefaults.eighteenfold)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardCert,
            onValueChange = { viewModel.onSetVirtualHealthCardCertificate(it) },
            label = "Certificate in Base64",
            placeholder = "Certificate in Base64"
        )

        val subjectInfo = remember(viewModel.debugSettingsData.virtualHealthCardCert) {
            viewModel.getVirtualHealthCardCertificateSubjectInfo()
        }
        Text(subjectInfo, style = AppTheme.typography.caption1l)

        ErezeptOutlineText(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.PrivateKeyField)
                .heightIn(max = SizeDefaults.eighteenfold)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardPrivateKey,
            onValueChange = { viewModel.onSetVirtualHealthCardPrivateKey(it) },
            label = "Private Key in Base64",
            placeholder = "Private Key in Base64"
        )

        SpacerMedium()

        DebugActionButton(
            modifier = Modifier.testTag(TestTag.DebugMenu.SetVirtualHealthCardButton),
            text = "Login with Virtual Health Card",
            loading = virtualHealthCardLoading,
            onClick = {
                viewModel.loginWithVirtualHealthCard(
                    cardAccessNumber = "123123",
                    certificateBase64 = viewModel.debugSettingsData.virtualHealthCardCert,
                    privateKeyBase64 = viewModel.debugSettingsData.virtualHealthCardPrivateKey
                )
            }
        )

        SpacerMedium()

        DebugActionButton(
            text = "Login with Virtual Health Card with Biometrics",
            loading = pairingLoading,
            onClick = {
                scope.launch {
                    when (val prompt = saveCredentialsController.initializeAndPrompt(useStrongBox = false)) {
                        is SaveCredentialsController.AuthResult.Initialized -> {
                            viewModel.loginWithVirtualHealthCardAndSecureElement(
                                cardAccessNumber = "123123",
                                certificateBase64 = viewModel.debugSettingsData.virtualHealthCardCert,
                                privateKeyBase64 = viewModel.debugSettingsData.virtualHealthCardPrivateKey,
                                aliasOfSecureElementEntry = prompt.aliasOfSecureElementEntry,
                                publicKeyOfSecureElementEntry = prompt.publicKey
                            )
                        }
                        else -> viewModel.onPairingAuthFailed()
                    }
                }
            }
        )
    }
}

@Composable
private fun FeatureToggles(
    modifier: Modifier = Modifier,
    featureState: Set<FeatureEntity>,
    viewModel: DebugSettingsViewModel,
    collapsible: Boolean = false,
    initiallyExpanded: Boolean = true,
    onClick: () -> Unit
) {
    DebugCard(
        modifier,
        title = "Feature Toggles",
        collapsible = collapsible,
        initiallyExpanded = initiallyExpanded
    ) {
        for (feature in featureState) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body1
                )
                GemSwitch(
                    checked = feature.isActive,
                    onCheckedChange = { viewModel.toggleFeature(feature) }
                )
            }
        }

        // Android 8 deprecation override (debug only)
        val android8Override by viewModel.android8DeprecationOverride.collectAsStateWithLifecycle()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Force Android 8 deprecation screen",
                modifier = Modifier.weight(1f)
            )
            GemSwitch(
                checked = android8Override,
                onCheckedChange = { viewModel.setAndroid8DeprecationOverride(it) }
            )
        }

        SpacerMedium()

        DebugActionButton(
            text = "Reset Onboarding (show again on next start)",
            onClick = { viewModel.resetOnboarding() }
        )

        SpacerMedium()

        DebugActionButton(
            text = "Restart if required",
            onClick = onClick
        )
    }
}

/**
 * Generic version selector component that displays versions with switches.
 * Supports both 2-option switch layout and multi-option radio button layout.
 * Enhanced with Material 3 styling and better visual feedback.
 */
@Composable
private fun <T> VersionSelector(
    title: String,
    description: String,
    currentVersion: T,
    versions: List<T>,
    getDisplayName: (T) -> String,
    onVersionSelected: (T) -> Unit,
    useRadioButtons: Boolean = false
) {
    DebugCard(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
            Text(
                text = description,
                style = MaterialTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                lineHeight = 20.sp
            )

            if (useRadioButtons) {
                // Grid layout with 2 columns using FlowRow for many versions
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                    maxItemsInEachRow = 2
                ) {
                    versions.forEach { version ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onVersionSelected(version) },
                            shape = RoundedCornerShape(SizeDefaults.one),
                            color = if (currentVersion == version) {
                                AppTheme.colors.primary100
                            } else {
                                Color.Transparent
                            },
                            border = if (currentVersion == version) {
                                BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
                            } else {
                                BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(PaddingDefaults.Small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.primary700,
                                        unselectedColor = AppTheme.colors.neutral400
                                    ),
                                    selected = currentVersion == version,
                                    onClick = { onVersionSelected(version) }
                                )
                                SpacerSmall()
                                Text(
                                    text = getDisplayName(version),
                                    style = MaterialTheme.typography.body1,
                                    color = if (currentVersion == version) {
                                        AppTheme.colors.primary900
                                    } else {
                                        AppTheme.colors.neutral900
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                    color = AppTheme.colors.neutral200
                )
            } else {
                // Simple switch layout for 2 versions
                versions.forEach { version ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVersionSelected(version) },
                        shape = RoundedCornerShape(SizeDefaults.one),
                        color = if (currentVersion == version) {
                            AppTheme.colors.primary100
                        } else {
                            Color.Transparent
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PaddingDefaults.Medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = getDisplayName(version),
                                style = MaterialTheme.typography.body1,
                                color = if (currentVersion == version) {
                                    AppTheme.colors.primary900
                                } else {
                                    AppTheme.colors.neutral900
                                }
                            )
                            GemSwitch(
                                checked = currentVersion == version,
                                onCheckedChange = { if (it) onVersionSelected(version) }
                            )
                        }
                    }

                    if (version != versions.last()) {
                        SpacerSmall()
                    }
                }
            }

            // Current version badge
            Surface(
                shape = RoundedCornerShape(SizeDefaults.doubleHalf),
                color = AppTheme.colors.primary100,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = "Current: ${getDisplayName(currentVersion)}",
                    style = MaterialTheme.typography.caption.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppTheme.colors.primary700,
                    modifier = Modifier.padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.Small
                    )
                )
            }
        }
    }
}

@Composable
fun ConsentVersionSelector(viewModel: DebugSettingsViewModel) {
    val consentVersion by viewModel.consentVersion.collectAsStateWithLifecycle()
    val pkvController = rememberDebugPkvController()
    val isProfilePKV by pkvController.isProfilePKV.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        pkvController.checkIsProfilePkv()
    }

    DebugCard(title = "PKV (Private Insurance)") {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
            // PKV/GKV Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Insurance Type",
                        style = AppTheme.typography.subtitle2,
                        fontWeight = FontWeight.SemiBold
                    )
                    SpacerTiny()
                    UiStateMachine(state = isProfilePKV, onLoading = {
                        Text(
                            text = "Checking...",
                            style = AppTheme.typography.body2l,
                            color = AppTheme.colors.neutral600
                        )
                    }, onError = {
                            Text(
                                text = "Error checking type",
                                style = AppTheme.typography.body2l,
                                color = AppTheme.colors.red600
                            )
                        }, onContent = { isPkv ->
                            Text(
                                text = if (isPkv) "Private (PKV)" else "Statutory (GKV)",
                                style = AppTheme.typography.body2l,
                                color = AppTheme.colors.primary600,
                                fontWeight = FontWeight.Medium
                            )
                        })
                }

                UiStateMachine(
                    state = isProfilePKV,
                    onLoading = { CircularProgressIndicator(modifier = Modifier.size(SizeDefaults.triple)) },
                    onError = { },
                    onContent = { isPkv ->
                        GemSwitch(
                            checked = isPkv,
                            onCheckedChange = { shouldBePkv ->
                                if (shouldBePkv) {
                                    pkvController.switchToPkv()
                                } else {
                                    pkvController.switchToGkv()
                                }
                            }
                        )
                    }
                )
            }

            HorizontalDivider(color = AppTheme.colors.neutral200)

            // Consent Version Section
            Text(
                text = "Consent Version",
                style = AppTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Select ERP Charge consent version for testing.\nUsed for both PKV and EU consent calls.\nProduction uses v1.0.",
                style = MaterialTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                lineHeight = 20.sp
            )

            // Radio button layout for consent versions
            Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                listOf(ConsentVersion.V1_0, ConsentVersion.V1_1).forEach { version ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = ripple(),
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                viewModel.setConsentVersion(version)
                            }
                            .padding(vertical = PaddingDefaults.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = consentVersion == version,
                            onClick = { viewModel.setConsentVersion(version) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppTheme.colors.primary600,
                                unselectedColor = AppTheme.colors.neutral400
                            )
                        )
                        Spacer(modifier = Modifier.width(PaddingDefaults.Small))
                        Column {
                            Text(
                                text = version.displayName,
                                style = AppTheme.typography.body1,
                                color = if (consentVersion == version) {
                                    AppTheme.colors.primary900
                                } else {
                                    AppTheme.colors.neutral900
                                },
                                fontWeight = if (consentVersion == version) FontWeight.Medium else FontWeight.Normal
                            )
                            if (version == ConsentVersion.V1_0) {
                                Text(
                                    text = "Production",
                                    style = MaterialTheme.typography.body2,
                                    color = AppTheme.colors.neutral600,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EuVersionSelector(viewModel: DebugSettingsViewModel) {
    val euVersion by viewModel.euVersion.collectAsStateWithLifecycle()

    VersionSelector(
        title = "Eu Version",
        description = "Select Eu version for testing",
        currentVersion = euVersion,
        versions = listOf(EuVersion.V_1_0, EuVersion.V_1_1),
        getDisplayName = { it.displayName },
        onVersionSelected = { viewModel.setEuVersion(it) }
    )
}

@Composable
fun CommunicationVersionSelector(viewModel: DebugSettingsViewModel) {
    val communicationVersion by viewModel.communicationVersion.collectAsStateWithLifecycle()

    VersionSelector(
        title = "Communication Version",
        description = "Select communication dispense version for testing.\nProduction uses v1.5.",
        currentVersion = communicationVersion,
        versions = CommunicationVersion.entries,
        getDisplayName = { it.displayName },
        onVersionSelected = { viewModel.setCommunicationVersion(it) },
        useRadioButtons = true
    )
}

@Composable
fun CommunicationDigaVersionSelector(viewModel: DebugSettingsViewModel) {
    val communicationDigaVersion by viewModel.communicationDigaVersion.collectAsStateWithLifecycle()

    VersionSelector(
        title = "Communication DiGA Version",
        description = "Select DiGA communication dispense version for testing.\nProduction uses v1.5.",
        currentVersion = communicationDigaVersion,
        versions = CommunicationDigaVersion.entries,
        getDisplayName = { it.displayName },
        onVersionSelected = { viewModel.setCommunicationDigaVersion(it) },
        useRadioButtons = true
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun DebugCardPreview() {
    PreviewAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.neutral000
        ) {
            Column(
                modifier = Modifier.padding(PaddingDefaults.Medium)
            ) {
                DebugCard(
                    title = "Card Settings",
                    onReset = {}
                ) {
                    Text(
                        "Sample content inside the debug card",
                        style = AppTheme.typography.body2l
                    )
                    SpacerSmall()
                    Text(
                        "More content here with proper spacing",
                        style = AppTheme.typography.body2l
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugActionButtonPreview() {
    PreviewAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.neutral000
        ) {
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
            ) {
                DebugActionButton(
                    text = "Normal Button",
                    onClick = {}
                )
                DebugActionButton(
                    text = "Button with Icon",
                    icon = painterResource(R.drawable.ic_prescription_refresh),
                    onClick = {}
                )
                DebugActionButton(
                    text = "Loading Button",
                    loading = true,
                    onClick = {}
                )
                DebugActionButton(
                    text = "Disabled Button",
                    enabled = false,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugNavigationItemPreview() {
    PreviewAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.neutral000
        ) {
            Column(
                modifier = Modifier.padding(PaddingDefaults.Medium)
            ) {
                DebugCard(title = "Navigation Items") {
                    DebugNavigationItem(
                        text = "Logger",
                        subtitle = "View application logs",
                        icon = painterResource(R.drawable.ic_finger),
                        onClick = {}
                    )
                    SpacerTiny()
                    DebugNavigationItem(
                        text = "Settings",
                        icon = painterResource(R.drawable.ic_pkv),
                        onClick = {}
                    )
                    SpacerTiny()
                    DebugNavigationItem(
                        text = "Without Icon",
                        subtitle = "Navigation without an icon",
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun VersionSelectorPreview() {
    PreviewAppTheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.neutral000
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(PaddingDefaults.Medium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
            ) {
                item {
                    // Switch style (2 versions)
                    VersionSelector(
                        title = "Consent Version",
                        description = "Toggle between consent versions for testing",
                        currentVersion = ConsentVersion.V1_0,
                        versions = listOf(
                            ConsentVersion.V1_0,
                            ConsentVersion.V1_1
                        ),
                        getDisplayName = { it.displayName },
                        onVersionSelected = {}
                    )
                }
                item {
                    // Radio button style (multiple versions)
                    VersionSelector(
                        title = "Communication Version",
                        description = "Select communication dispense version for testing.\nProduction uses v1.5.",
                        currentVersion = CommunicationVersion.V_1_5,
                        versions = CommunicationVersion.entries,
                        getDisplayName = { it.displayName },
                        onVersionSelected = {},
                        useRadioButtons = true
                    )
                }
            }
        }
    }
}
