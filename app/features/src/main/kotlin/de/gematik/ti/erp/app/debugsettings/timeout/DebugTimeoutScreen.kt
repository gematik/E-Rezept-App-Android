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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.timeout.intent.restartApp
import de.gematik.ti.erp.app.debugsettings.timeout.ui.MetricChangeDialog
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.NoError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsScreenViewModel
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
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
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            actions = {},
            listState = listState,
            onBack = onBack
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = PaddingDefaults.Medium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
            ) {
                item { SpacerMedium() }

                // Timeout Settings Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SizeDefaults.double),
                        elevation = SizeDefaults.half,
                        backgroundColor = AppTheme.colors.neutral050,
                        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral200)
                    ) {
                        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                            Text(
                                text = "Timeout Configuration",
                                style = AppTheme.typography.subtitle1,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.neutral900
                            )
                            SpacerSmall()
                            Text(
                                text = "Configure app timeout behavior for inactivity and background pause.",
                                style = MaterialTheme.typography.body2,
                                color = AppTheme.colors.neutral600,
                                lineHeight = 20.sp
                            )

                            SpacerMedium()
                            HorizontalDivider(color = AppTheme.colors.neutral200)
                            SpacerMedium()

                            // Inactivity Timer
                            TimeoutSetting(
                                label = "Inactivity Timer",
                                value = "$inactivityMetric",
                                description = "Triggered when the app is running but no user interaction occurs.",
                                onClick = {
                                    dialog.show { dialog ->
                                        MetricChangeDialog(
                                            label = "Inactivity Timer",
                                            currentValue = inactivityMetric,
                                            onDismissRequest = { dialog.dismiss() },
                                            onValueChanged = { value, duration ->
                                                viewmodel.setInactivityMetric(value, duration)
                                                dialog.dismiss()
                                                scope.launch {
                                                    snackbar.showSnackbar("Inactivity timer updated. Restart required")
                                                }
                                            }
                                        )
                                    }
                                }
                            )

                            SpacerMedium()
                            HorizontalDivider(color = AppTheme.colors.neutral200)
                            SpacerMedium()

                            // Pause Timer
                            TimeoutSetting(
                                label = "Pause Timer",
                                value = "$pauseMetric",
                                description = "Triggered when the app is minimized or moved to background.",
                                onClick = {
                                    dialog.show { dialog ->
                                        MetricChangeDialog(
                                            label = "Pause Timer",
                                            currentValue = pauseMetric,
                                            onDismissRequest = { dialog.dismiss() },
                                            onValueChanged = { value, duration ->
                                                viewmodel.setPauseMetric(value, duration)
                                                dialog.dismiss()
                                                scope.launch {
                                                    snackbar.showSnackbar("Pause timer updated. Restart required")
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Reset Button
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewmodel.resetToDefaultMetrics()
                                scope.launch {
                                    snackbar.showSnackbar("All timers reset to defaults. Restart required")
                                }
                            },
                        shape = RoundedCornerShape(SizeDefaults.oneHalf),
                        color = Color.Transparent,
                        border = BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
                    ) {
                        Row(
                            modifier = Modifier.padding(PaddingDefaults.Medium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = AppTheme.colors.primary600
                            )
                            SpacerSmall()
                            Text(
                                text = "Reset to Defaults",
                                style = AppTheme.typography.body1,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.primary600
                            )
                        }
                    }
                }

                // Restart App Button
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { restartApp<MainActivity>(activity) },
                        shape = RoundedCornerShape(SizeDefaults.oneHalf),
                        color = AppTheme.colors.primary600
                    ) {
                        Row(
                            modifier = Modifier.padding(PaddingDefaults.Medium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = null,
                                tint = Color.White
                            )
                            SpacerSmall()
                            Text(
                                text = "Restart App",
                                style = AppTheme.typography.body1,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                item { SpacerLarge() }
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

@Composable
private fun TimeoutSetting(
    label: String,
    value: String,
    description: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = AppTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.neutral900
                )
                SpacerTiny()
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = AppTheme.colors.neutral600,
                    lineHeight = 18.sp
                )
            }

            Surface(
                modifier = Modifier.clickable(onClick = onClick),
                shape = RoundedCornerShape(SizeDefaults.one),
                color = AppTheme.colors.primary100,
                border = BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.Small
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    Text(
                        text = value,
                        style = AppTheme.typography.body1,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primary900
                    )
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = AppTheme.colors.primary600,
                        modifier = Modifier.padding(SizeDefaults.quarter)
                    )
                }
            }
        }
    }
}
