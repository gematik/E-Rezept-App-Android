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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Alignment
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErezeptText.ErezeptTextAlignment.Center

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    title: String,
    body: String,
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(SizeDefaults.triple),
            contentColor = contentColorFor(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Large)
            ) {
                SpacerMedium()
                ErezeptText.Title(
                    text = title,
                    textAlignment = Center
                )
                SpacerMedium()
                ErezeptText.Body(body)
                SpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                        onClick = onDismissRequest
                    ) {
                        Text(okText)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    title: String,
    body: @Composable ColumnScope.() -> Unit,
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(SizeDefaults.triple),
            contentColor = contentColorFor(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
            ) {
                SpacerMedium()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ErezeptText.Title(title)
                }
                SpacerMedium()
                body()
                SpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                        onClick = onDismissRequest
                    ) {
                        Text(okText)
                    }
                }
            }
        }
    }
}
