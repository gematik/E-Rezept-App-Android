/*
 * Copyright 2024, gematik GmbH
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

@file: Suppress("UnusedPrivateMember", "MagicNumber")

package de.gematik.ti.erp.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.debugsettings.logger.ui.screens.LoggerScreen
import de.gematik.ti.erp.app.debugsettings.navigation.DebugScreenNavigation
import de.gematik.ti.erp.app.debugsettings.pkv.ui.DebugScreenPKV
import de.gematik.ti.erp.app.debugsettings.qrcode.QrCodeScannerScreen
import de.gematik.ti.erp.app.debugsettings.timeout.DebugTimeoutScreen
import de.gematik.ti.erp.app.debugsettings.ui.components.ClearTextTrafficSection
import de.gematik.ti.erp.app.debugsettings.ui.components.ClientIdsSection
import de.gematik.ti.erp.app.debugsettings.ui.components.EnvironmentSelector
import de.gematik.ti.erp.app.debugsettings.ui.components.LoadingButton
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.extensions.erezeptColors
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Base64
import org.kodein.di.bindProvider
import org.kodein.di.compose.rememberViewModel
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun DebugCard(
    modifier: Modifier = Modifier,
    title: String,
    onReset: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) =
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        backgroundColor = AppTheme.colors.neutral100,
        elevation = 10.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            content = {
                Column(
                    Modifier.padding(PaddingDefaults.Medium),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    SpacerMedium()
                    content()
                }
                onReset?.run {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(PaddingDefaults.Small),
                        onClick = onReset
                    ) {
                        Icon(Icons.Rounded.Refresh, null)
                    }
                }
            }
        )
    }

@Composable
fun EditablePathComponentSetButton(
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
fun EditablePathComponentWithControl(
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
                visibleDebugTree = instance(),
                endpointHelper = instance(),
                cardWallUseCase = instance(),
                prescriptionUseCase = instance(),
                saveInvoiceUseCase = instance(),
                vauRepository = instance(),
                idpRepository = instance(),
                idpUseCase = instance(),
                profilesUseCase = instance(),
                featureToggleManager = instance(),
                pharmacyDirectRedeemUseCase = instance(),
                getAppUpdateManagerFlagUseCase = instance(),
                changeAppUpdateManagerFlagUseCase = instance(),
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
                        onBack = {
                            settingsNavController.popBackStack()
                        },
                        onClickDirectRedemption = {
                            navController.navigate(DebugScreenNavigation.DebugRedeemWithoutFD.path())
                        },
                        onClickPKV = {
                            navController.navigate(DebugScreenNavigation.DebugPKV.path())
                        },
                        onClickBioMetricSettings = {
                            navController.navigate(DebugScreenNavigation.DebugTimeout.path())
                        },
                        onScanQrCode = {
                            navController.navigate(DebugScreenNavigation.QrCodeScannerScreen.path())
                        },
                        onClickLogger = {
                            navController.navigate(DebugScreenNavigation.LoggerScreen.path())
                        }
                    )
                }
            }
            composable(DebugScreenNavigation.DebugRedeemWithoutFD.route) {
                NavigationAnimation(mode = navMode) {
                    DebugScreenDirectRedeem(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
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
                        onBack = {
                            navController.popBackStack()
                        }
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
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(DebugScreenNavigation.LoggerScreen.route) {
                LoggerScreen.Content(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// TODO: Change to use the correct use-cases
@Composable
fun DebugScreenDirectRedeem(
    viewModel: DebugSettingsViewModel,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        topBarTitle = "Debug Redeem",
        onBack = onBack
    ) { innerPadding ->
        var shipmentUrl by remember { mutableStateOf("") }
        var deliveryUrl by remember { mutableStateOf("") }
        var onPremiseUrl by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var certificates by remember { mutableStateOf("") }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                DebugCard(
                    title = "Endpoints"
                ) {
                    ErezeptOutlineText(
                        modifier = Modifier.fillMaxWidth(),
                        value = shipmentUrl,
                        label = "Shipment URL",
                        placeholder = "Shipment URL",
                        onValueChange = {
                            shipmentUrl = it
                        }
                    )
                    ErezeptOutlineText(
                        modifier = Modifier.fillMaxWidth(),
                        value = deliveryUrl,
                        label = "Delivery URL",
                        placeholder = "Delivery URL",
                        onValueChange = {
                            deliveryUrl = it
                        }
                    )
                    ErezeptOutlineText(
                        modifier = Modifier.fillMaxWidth(),
                        value = onPremiseUrl,
                        label = "OnPremise URL",
                        placeholder = "OnPremise URL",
                        onValueChange = {
                            onPremiseUrl = it
                        }
                    )
                }
            }
            item {
                RedeemButton(
                    viewModel = viewModel,
                    url = shipmentUrl,
                    message = message,
                    certificates = certificates,
                    text = "Send as Shipment"
                )
                RedeemButton(
                    viewModel = viewModel,
                    url = deliveryUrl,
                    message = message,
                    certificates = certificates,
                    text = "Send as Delivery"
                )
                RedeemButton(
                    viewModel = viewModel,
                    url = onPremiseUrl,
                    message = message,
                    certificates = certificates,
                    text = "Send as OnPremise"
                )
            }
            item {
                DebugCard(
                    title = "Message"
                ) {
                    ErezeptOutlineText(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                        value = message,
                        label = "Any Message",
                        placeholder = "Any Message",
                        onValueChange = {
                            message = it
                        }
                    )
                }
            }
            item {
                DebugCard(
                    title = "Certificates"
                ) {
                    ErezeptOutlineText(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                        value = certificates,
                        label = "Certificate as PEM",
                        placeholder = "Certificate as PEM",
                        onValueChange = {
                            certificates = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RedeemButton(
    viewModel: DebugSettingsViewModel,
    url: String,
    message: String,
    certificates: String,
    text: String
) =
    LoadingButton(
        onClick = { viewModel.redeemDirect(url = url, message = message, certificatesPEM = certificates) },
        enabled = url.isNotEmpty() && certificates.isNotEmpty(),
        text = text
    )

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DebugScreenMain(
    viewModel: DebugSettingsViewModel,
    onBack: () -> Unit,
    onClickDirectRedemption: () -> Unit,
    onClickPKV: () -> Unit,
    onClickBioMetricSettings: () -> Unit,
    onScanQrCode: () -> Unit,
    onClickLogger: () -> Unit
) {
    val listState = rememberLazyListState()
    val modal = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val appUpdateManager by viewModel.appUpdateManagerState

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
            listState = listState,
            topBarTitle = "Debug Settings",
            onBack = onBack
        ) { innerPadding ->

            LaunchedEffect(Unit) {
                viewModel.state()
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
                item {
                    DebugCard(
                        title = "General"
                    ) {
                        LabelButton(
                            icon = painterResource(R.drawable.ic_finger),
                            text = "Biometric settings"
                        ) {
                            onClickBioMetricSettings()
                        }
                        LabelButton(
                            icon = painterResource(R.drawable.ic_qr_code),
                            text = "Direct Redemption"
                        ) {
                            onClickDirectRedemption()
                        }
                        LabelButton(
                            icon = painterResource(R.drawable.ic_pkv),
                            text = "PKV / GKV Switch"
                        ) {
                            onClickPKV()
                        }
                        LabelButton(
                            icon = painterResource(R.drawable.ic_prescription_refresh),
                            text = "Trigger Prescription Refresh"
                        ) {
                            viewModel.refreshPrescriptions()
                        }
                        LabelButton(
                            icon = Icons.Rounded.Adb,
                            text = "Logger"
                        ) {
                            onClickLogger()
                        }
                    }
                }
                item {
                    DebugCard(
                        title = "App Update"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Original app update",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                modifier = Modifier.testTag(TestTag.DebugMenu.FakeAppUpdate),
                                colors = SwitchDefaults.erezeptColors(),
                                checked = appUpdateManager,
                                onCheckedChange = { viewModel.changeAppUpdateManager(it) }
                            )
                        }
                    }
                }
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
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Switch(
                                modifier = Modifier.testTag(TestTag.DebugMenu.FakeNFCCapabilities),
                                colors = SwitchDefaults.erezeptColors(),
                                checked = viewModel.debugSettingsData.fakeNFCCapabilities,
                                onCheckedChange = { viewModel.allowNfc(it) }
                            )
                        }
                    }
                }
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
                        Button(
                            onClick = { scope.launch { viewModel.breakSSOToken() } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Break SSO Token")
                        }
                    }
                }
                item {
                    DebugCard(title = "Environment") {
                        OutlinedDebugButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Select Environment",
                            onClick = { scope.launch { modal.show() } }
                        )
                    }
                }
                item {
                    VirtualHealthCard(
                        viewModel = viewModel
                    ) {
                        onScanQrCode()
                    }
                }
                item {
                    FeatureToggles(viewModel = viewModel)
                }
                item {
                    RotatingLog(viewModel = viewModel)
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
                    ClientIdsSection()
                }
            }
        }
    }
}

@Composable
private fun RotatingLog(modifier: Modifier = Modifier, viewModel: DebugSettingsViewModel) {
    DebugCard(modifier, title = "Log") {
        val context = LocalContext.current
        val mailAddress = stringResource(R.string.settings_contact_mail_address)
        LabelButton(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(R.drawable.ic_log_mail),
            text = "Send mail",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:")
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
                intent.putExtra(Intent.EXTRA_SUBJECT, "#Log-#Android-${LocalDateTime.now()}")

                val bout = ByteArrayOutputStream()
                ZipOutputStream(bout).use {
                    val e = ZipEntry("log.txt")
                    it.putNextEntry(e)

                    val data = viewModel.rotatingLog.value.joinToString("\n").toByteArray()
                    it.write(data, 0, data.size)
                    it.closeEntry()
                }

                intent.putExtra(Intent.EXTRA_TEXT, Base64.toBase64String(bout.toByteArray()))

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        )
    }
}

@Composable
private fun VirtualHealthCard(
    modifier: Modifier = Modifier,
    viewModel: DebugSettingsViewModel,
    onScanQrCode: () -> Unit
) {
    var virtualHealthCardLoading by remember { mutableStateOf(false) }
    var virtualHealthCardError by remember { mutableStateOf<String?>(null) }
    virtualHealthCardError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                virtualHealthCardError = null
            },
            buttons = {
                Button(onClick = { virtualHealthCardError = null }) {
                    Text("OK")
                }
            },
            text = {
                Text(error)
            }
        )
    }

    DebugCard(modifier, title = "Virtual Health Card", onReset = viewModel::onResetVirtualHealthCard) {
        val scope = rememberCoroutineScope()
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, AppTheme.colors.primary600),
            shape = RoundedCornerShape(8.dp),
            onClick = onScanQrCode
        ) {
            Text(
                text = "Scan Virtual Health Card",
                color = AppTheme.colors.primary600
            )
        }

        ErezeptOutlineText(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.CertificateField)
                .heightIn(max = 144.dp)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardCert,
            onValueChange = {
                viewModel.onSetVirtualHealthCardCertificate(it)
            },
            label = "Certificate in Base64",
            placeholder = "Certificate in Base64"
        )

        val subjectInfo =
            remember(viewModel.debugSettingsData.virtualHealthCardCert) {
                viewModel.getVirtualHealthCardCertificateSubjectInfo()
            }
        Text(subjectInfo, style = AppTheme.typography.caption1l)

        ErezeptOutlineText(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.PrivateKeyField)
                .heightIn(max = 144.dp)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardPrivateKey,
            onValueChange = {
                viewModel.onSetVirtualHealthCardPrivateKey(it)
            },
            label = "Private Key in Base64",
            placeholder = "Private Key in Base64"
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTag.DebugMenu.SetVirtualHealthCardButton),
            onClick = {
                virtualHealthCardLoading = true
                scope.launch {
                    try {
                        viewModel.onTriggerVirtualHealthCard(
                            certificateBase64 = viewModel.debugSettingsData.virtualHealthCardCert,
                            privateKeyBase64 = viewModel.debugSettingsData.virtualHealthCardPrivateKey
                        )
                    } catch (e: Exception) {
                        virtualHealthCardError = e.message
                    } finally {
                        virtualHealthCardLoading = false
                    }
                }
            },
            enabled = !virtualHealthCardLoading
        ) {
            if (virtualHealthCardLoading) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = AppTheme.colors.neutral600
                )
                SpacerSmall()
            }
            Text("Set Virtual Health Card for Active Profile", textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FeatureToggles(modifier: Modifier = Modifier, viewModel: DebugSettingsViewModel) {
    val featuresState by produceState(initialValue = mutableMapOf<String, Boolean>()) {
        viewModel.featuresState().collect {
            value = it
        }
    }
    DebugCard(modifier, title = "Feature Toggles") {
        for (feature in viewModel.features()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.featureName,
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.body1
                )
                Switch(
                    checked = featuresState[feature.featureName] ?: false,
                    onCheckedChange = { viewModel.toggleFeature(feature) }
                )
            }
        }
    }
}
