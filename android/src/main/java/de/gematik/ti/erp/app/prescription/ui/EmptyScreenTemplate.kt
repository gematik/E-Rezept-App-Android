/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun EmptyScreenHome(
    modifier: Modifier = Modifier,
    header: String,
    description: String,
    image: @Composable () -> Unit,
    button: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        image()
        SpacerMedium()
        Text(
            text = header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
        SpacerSmall()
        Text(
            text = description,
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
        SpacerSmall()
        button()
    }
}

@Composable
fun EmptyScreenArchive(
    modifier: Modifier = Modifier,
    header: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerSmall()
        Text(
            text = description,
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ConnectButton(
    onClick: () -> Unit
) =
    TextButton(
        onClick = onClick
    ) {
        Icon(
            Icons.Rounded.Refresh,
            null,
            modifier = Modifier.size(16.dp),
            tint = AppTheme.colors.primary600
        )
        SpacerSmall()
        Text(text = "Verbinden", textAlign = TextAlign.Right)
    }

@Composable
fun RefreshButton(
    onClick: () -> Unit
) =
    TextButton(
        onClick = onClick
    ) {
        Icon(
            Icons.Rounded.Refresh,
            null,
            modifier = Modifier.size(16.dp),
            tint = AppTheme.colors.primary600
        )
        SpacerSmall()
        Text(text = "Aktualisieren", textAlign = TextAlign.Right)
    }
