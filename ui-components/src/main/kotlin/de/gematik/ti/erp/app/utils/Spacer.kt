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

package de.gematik.ti.erp.app.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun SpacerTiny() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Tiny))

@Composable
fun SpacerSmall() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Small))

@Composable
fun SpacerMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Medium))

@Composable
fun SpacerShortMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.ShortMedium))

@Composable
fun SpacerLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Large))

@Composable
fun SpacerXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XLarge))

@Composable
fun SpacerXXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLarge))

@Composable
fun SpacerXXLargeMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLargeMedium))

@Composable
fun SpacerXXXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXXLarge))
