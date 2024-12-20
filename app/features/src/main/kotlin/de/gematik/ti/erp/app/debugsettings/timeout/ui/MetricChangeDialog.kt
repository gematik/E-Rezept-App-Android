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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.debugsettings.timeout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum.HOURS
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum.MINUTES
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum.SECONDS
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.extensions.ErezeptKeyboardOptions
import kotlinx.coroutines.android.awaitFrame
import kotlin.time.Duration

@Composable
fun MetricChangeDialog(
    label: String,
    currentValue: Duration,
    onDismissRequest: () -> Unit,
    onValueChanged: (String, DurationEnum) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val (value, duration) = "$currentValue".extractTimeValues()
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var expanded by remember { mutableStateOf(false) }
    var selectedDurationEnum by remember { mutableStateOf(DurationEnum.valueOf(duration.name)) }
    val items = listOf(SECONDS, MINUTES, HOURS)

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        awaitFrame()
        keyboard?.show()
    }

    Surface {
        Column(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .padding(vertical = PaddingDefaults.Large)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErezeptOutlineText(
                    modifier = Modifier.weight(0.4f)
                        .focusRequester(focusRequester),
                    keyboardOptions = ErezeptKeyboardOptions.number,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onValueChanged(textFieldValue.text, selectedDurationEnum)
                        }
                    ),
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.body2
                        )
                    },
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                    }
                )
                Spacer(modifier = Modifier.weight(0.1f))
                Text(
                    modifier = Modifier.weight(0.25f),
                    text = "$selectedDurationEnum",
                    style = MaterialTheme.typography.body1
                )
                Box(
                    modifier = Modifier.weight(0.25f)
                ) {
                    IconButton(
                        onClick = {
                            expanded = !expanded
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "$selectedDurationEnum"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = !expanded
                        }
                    ) {
                        items.forEachIndexed { index, durationEnum ->
                            DropdownMenuItem(
                                onClick = {
                                    keyboard?.show()
                                    selectedDurationEnum = durationEnum
                                    expanded = false
                                },
                                content = { Text(text = durationEnum.name) }
                            )
                            if (index + 1 < items.size) {
                                Divider()
                            }
                        }
                    }
                }
            }
            SpacerMedium()
            Row {
                TextButton(
                    modifier = Modifier.weight(0.5f),
                    onClick = onDismissRequest
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.body1
                    )
                }
                TextButton(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        onValueChanged(textFieldValue.text, selectedDurationEnum)
                    }

                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

private fun String.extractTimeValues(): Pair<String, DurationEnum> {
    val regex = Regex("(\\d+)([hms])")
    val matchResults = regex.findAll(this)
    var value = ""
    var unit = SECONDS
    for (result in matchResults) {
        value = result.groupValues[1] // Extract the numeric value
        unit = DurationEnum.extractedUnit(result.groupValues[2]) // Extract the unit (h, m, or s)
    }
    return value to unit
}
