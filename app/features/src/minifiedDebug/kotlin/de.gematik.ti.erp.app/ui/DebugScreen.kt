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

package de.gematik.ti.erp.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
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
import de.gematik.ti.erp.app.debugsettings.navigation.DebugScreenNavigation
import de.gematik.ti.erp.app.debugsettings.timeout.DebugTimeoutScreen
import de.gematik.ti.erp.app.debugsettings.ui.DebugCard
import de.gematik.ti.erp.app.debugsettings.ui.DebugScreenPKV
import de.gematik.ti.erp.app.debugsettings.ui.EditablePathComponentSetButton
import de.gematik.ti.erp.app.debugsettings.ui.EnvironmentSelector
import de.gematik.ti.erp.app.debugsettings.ui.LoadingButton
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.ui.LabelButton
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.navigationModeState
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
                invoiceRepository = instance(),
                vauRepository = instance(),
                idpRepository = instance(),
                idpUseCase = instance(),
                profilesUseCase = instance(),
                featureToggleManager = instance(),
                pharmacyDirectRedeemUseCase = instance(),
                dispatchers = instance()
            )
        }
    }) {
        NavHost(
            navController,
            startDestination = DebugScreenNavigation.DebugMain.path()
        ) {
            composable(DebugScreenNavigation.DebugMain.route) {
                NavigationAnimation(mode = navMode) {
                    DebugScreenMain(
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
                        }
                    )
                }
            }
            composable(DebugScreenNavigation.DebugRedeemWithoutFD.route) {
                NavigationAnimation(mode = navMode) {
                    DebugScreenDirectRedeem(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable(DebugScreenNavigation.DebugPKV.route) {
                val viewModel by rememberViewModel<DebugSettingsViewModel>()
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
        }
    }
}

@Composable
fun DebugScreenDirectRedeem(onBack: () -> Unit) {
    val viewModel by rememberViewModel<DebugSettingsViewModel>()
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
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = shipmentUrl,
                        label = { Text("Shipment URL") },
                        onValueChange = {
                            shipmentUrl = it
                        }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deliveryUrl,
                        label = { Text("Delivery URL") },
                        onValueChange = {
                            deliveryUrl = it
                        }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = onPremiseUrl,
                        label = { Text("OnPremise URL") },
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
                    OutlinedTextField(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                        value = message,
                        label = { Text("Any Message") },
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
                    OutlinedTextField(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                        value = certificates,
                        label = { Text("Certificate as PEM") },
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
    onBack: () -> Unit,
    onClickDirectRedemption: () -> Unit,
    onClickPKV: () -> Unit,
    onClickBioMetricSettings: () -> Unit
) {
    val viewModel by rememberViewModel<DebugSettingsViewModel>()
    val listState = rememberLazyListState()
    val modal = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

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
                            text = "PKV"
                        ) {
                            onClickPKV()
                        }
                        LabelButton(
                            icon = painterResource(R.drawable.ic_prescription_refresh),
                            text = "Trigger Prescription Refresh"
                        ) {
                            viewModel.refreshPrescriptions()
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
                    VirtualHealthCard(viewModel = viewModel)
                }
                item {
                    FeatureToggles(viewModel = viewModel)
                }
                item {
                    RotatingLog(viewModel = viewModel)
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
private fun VirtualHealthCard(modifier: Modifier = Modifier, viewModel: DebugSettingsViewModel) {
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

        OutlinedTextField(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.CertificateField)
                .heightIn(max = 144.dp)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardCert,
            onValueChange = {
                viewModel.onSetVirtualHealthCardCertificate(it)
            },
            label = { Text("Certificate in Base64") }
        )

        val subjectInfo =
            remember(viewModel.debugSettingsData.virtualHealthCardCert) {
                viewModel.getVirtualHealthCardCertificateSubjectInfo()
            }
        Text(subjectInfo, style = AppTheme.typography.caption1l)

        OutlinedTextField(
            modifier = Modifier
                .testTag(TestTag.DebugMenu.PrivateKeyField)
                .heightIn(max = 144.dp)
                .fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardPrivateKey,
            onValueChange = {
                viewModel.onSetVirtualHealthCardPrivateKey(it)
            },
            label = { Text("Private Key in Base64") }
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

