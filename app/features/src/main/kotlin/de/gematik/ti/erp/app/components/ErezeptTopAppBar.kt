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

package de.gematik.ti.erp.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.NavigationBack
import de.gematik.ti.erp.app.utils.compose.NavigationClose

object ErezeptTopAppBar {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Close(
        title: String?,
        onClickNavIcon: () -> Unit
    ) {
        TopAppBar(
            title = {
                title?.let {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            style = AppTheme.typography.h6,
                            overflow = TextOverflow.Ellipsis,
                            text = it
                        )
                    }
                }
            },
            navigationIcon = {
                NavigationClose { onClickNavIcon() }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Back(
        title: String,
        onClickNavIcon: () -> Unit
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        style = AppTheme.typography.h6,
                        overflow = TextOverflow.Ellipsis,
                        text = title
                    )
                }
            },
            navigationIcon = {
                NavigationBack { onClickNavIcon() }
            }
        )
    }
}
