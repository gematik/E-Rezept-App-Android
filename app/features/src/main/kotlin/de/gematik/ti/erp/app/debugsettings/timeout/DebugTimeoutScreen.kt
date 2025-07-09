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

@file:Suppress("MagicNumber", "LongMethod")

package de.gematik.ti.erp.app.debugsettings.timeout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.debugsettings.timeout.intent.restartApp
import de.gematik.ti.erp.app.debugsettings.timeout.ui.MetricChangeDialog
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.NoError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsScreenViewModel
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.SpacerXXLargeMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

object DebugTimeoutScreen {
    @Composable
    fun Content(
        onBack: () -> Unit
    ) {
        val viewmodel by rememberInstance<TimeoutsScreenViewModel>()
        val dialog = LocalDialog.current
        val snackbar = LocalSnackbarScaffold.current
        val activity = LocalActivity.current

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val inactivityMetric by viewmodel.inactivityMetricDuration.collectAsStateWithLifecycle()
        val pauseMetric by viewmodel.pauseMetricDuration.collectAsStateWithLifecycle()
        val error by viewmodel.error.collectAsStateWithLifecycle(NoError)

        AnimatedElevationScaffold(
            topBarTitle = "Timeout settings",
            actions = {},
            listState = listState,
            onBack = onBack
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                item {
                    SpacerMedium()
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(0.4f),
                            text = "Inactivity timer:",
                            style = MaterialTheme.typography.body1
                        )
                        Card(
                            modifier = Modifier.weight(0.6f),
                            border = BorderStroke(0.5.dp, AppTheme.colors.primary700)
                        ) {
                            TextButton(
                                onClick = {
                                    dialog.show { dialog ->
                                        MetricChangeDialog(
                                            label = "Inactivity Timer",
                                            currentValue = inactivityMetric,
                                            onDismissRequest = {
                                                dialog.dismiss()
                                            },
                                            onValueChanged = { value, duration ->
                                                viewmodel.setInactivityMetric(value, duration)
                                                dialog.dismiss()
                                                scope.launch {
                                                    snackbar.showSnackbar("Inactivity timer reset. Restart required")
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    "$inactivityMetric",
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                }
                item {
                    SpacerMedium()
                    Text(
                        text = "The inactivity timer is called when the app is running but nothing is clicked",
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                item {
                    SpacerLarge()
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(0.4f),
                            text = "Pause Timer:",
                            style = MaterialTheme.typography.body1
                        )
                        Card(
                            modifier = Modifier.weight(0.6f),
                            border = BorderStroke(0.5.dp, AppTheme.colors.primary700)
                        ) {
                            TextButton(
                                onClick = {
                                    dialog.show { dialog ->
                                        MetricChangeDialog(
                                            label = "Pause Timer",
                                            currentValue = pauseMetric,
                                            onDismissRequest = {
                                                dialog.dismiss()
                                            },
                                            onValueChanged = { value, duration ->
                                                viewmodel.setPauseMetric(value, duration)
                                                dialog.dismiss()
                                                scope.launch {
                                                    snackbar.showSnackbar("Pause timer reset. Restart required")
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    "$pauseMetric",
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                }
                item {
                    SpacerMedium()
                    Text(
                        text = "The pause timer is called when the app is minimized",
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                item {
                    SpacerXXLargeMedium()
                    TextButton(
                        modifier = Modifier.padding(horizontal = PaddingDefaults.XXLargeMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
                        onClick = {
                            viewmodel.resetToDefaultMetrics()
                            scope.launch {
                                snackbar.showSnackbar("All timers reset. Restart required")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_reset),
                                contentDescription = null
                            )
                            Text(
                                text = "Reset to Default",
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }
                item {
                    SpacerXXLarge()
                    PrimaryButton(
                        modifier = Modifier
                            .padding(horizontal = PaddingDefaults.XXLargeMedium)
                            .fillMaxWidth(),
                        content = {
                            Text(
                                modifier = Modifier,
                                text = "Restart App",
                                style = MaterialTheme.typography.h6
                            )
                        },
                        onClick = {
                            restartApp<MainActivity>(activity)
                        }
                    )
                }
            }
        }

        LaunchedEffect(error) {
            // show errors for timeout
            when (error) {
                TimeoutsError.InactivityError -> snackbar.showSnackbar(
                    message = "Error setting inactivity timer"
                    // backgroundTint = R.color.red_500
                )

                TimeoutsError.PauseError -> snackbar.showSnackbar(
                    message = "Error setting pause timer"
                    // backgroundTint = R.color.red_500
                )

                TimeoutsError.Error -> snackbar.showSnackbar(
                    message = "Error setting inactivity and pause timers"
                    // backgroundTint = R.color.red_500

                )

                NoError -> {
                    // show nothing
                }
            }
        }
    }
}
