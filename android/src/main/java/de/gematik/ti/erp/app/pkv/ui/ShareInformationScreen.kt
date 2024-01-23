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

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

import kotlinx.coroutines.flow.first

@Composable
fun ShareInformationScreen(
    taskId: String,
    onBack: () -> Unit,
    invoicesController: InvoicesController
) {
    val scaffoldState = rememberScaffoldState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var innerHeight by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        listState.scrollToItem(listState.layoutInfo.totalItemsCount, 0)
    }

    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .visualTestTag(TestTag.Profile.InvoicesDetailScreen),
        topBarTitle = "",
        navigationMode = NavigationBarMode.Close,
        scaffoldState = scaffoldState,
        bottomBar = {
            ShareInformationBottomBar {
                scope.launch {
                    invoicesController.detailState(taskId).first()?.let {
                        invoicesController.shareInvoicePDF(context, it)
                        onBack()
                    }
                }
            }
        },
        listState = listState,
        actions = {},
        onBack = onBack
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium
                )
                .onGloballyPositioned {
                    innerHeight = it.boundsInWindow().size.height.toInt()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item {
                Image(
                    painterResource(R.drawable.share_sheet),
                    null
                )
            }
            item {
                InformationHeader()
            }
            item {
                InformationLabel(Icons.Rounded.ImportExport, stringResource(R.string.share_information_app_share_info))
            }
            item {
                Text(stringResource(R.string.share_information_or).uppercase(), style = AppTheme.typography.subtitle2l)
            }
            item {
                InformationLabel(Icons.Rounded.SaveAlt, stringResource(R.string.share_information_app_save_info))
            }
            item {
                SpacerLarge()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(350.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            startY = 40f,
                            endY = 450f,
                            colors = listOf(
                                AppTheme.colors.neutral000,
                                Color.Transparent
                            )
                        )
                    )
            ) {}
        }
    }
}

@Composable
fun InformationLabel(icon: ImageVector, info: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = AppTheme.colors.neutral600,
            modifier = Modifier.padding(end = PaddingDefaults.Medium)
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = PaddingDefaults.Medium),
            text = info,
            style = AppTheme.typography.body2
        )
    }
}

@Composable
fun InformationHeader() {
    Text(
        text = stringResource(R.string.share_invoice_information_header),
        modifier = Modifier
            .padding(top = PaddingDefaults.XXLarge),
        style = AppTheme.typography.subtitle1
    )
    SpacerSmall()
}

@Composable
fun ShareInformationBottomBar(onClickShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecondaryButton(
            onClick = onClickShare,
            modifier = Modifier.padding(end = PaddingDefaults.Medium),
            contentPadding = PaddingValues(horizontal = PaddingDefaults.XXLarge, vertical = 13.dp)
        ) {
            Text(text = stringResource(R.string.invoice_share_okay))
        }
    }
}
