/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pkv.ui.screens

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.FileProviderAuthority
import de.gematik.ti.erp.app.pkv.navigation.PkvNavigationArguments.Companion.getPkvNavigationArguments
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import org.kodein.di.compose.rememberInstance

class InvoiceShareScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val arguments = remember { navBackStackEntry.arguments.getPkvNavigationArguments() }
        val invoiceController = rememberInvoiceController(arguments.profileId)
        val scaffoldState = rememberScaffoldState()

        val context = LocalContext.current
        var innerHeight by remember { mutableIntStateOf(0) }
        val listState = rememberLazyListState()
        val fileProvider by rememberInstance<FileProviderAuthority>()

        LaunchedEffect(listState) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount, 0)
        }

        InvoiceShareScreenScaffold(
            scaffoldState = scaffoldState,
            listState = listState,
            onBottomBarClick = {
                arguments.taskId?.let {
                    invoiceController.shareInvoice(
                        context = context,
                        taskId = it,
                        fileProvider = fileProvider,
                        onCompletion = navController::popBackStack
                    )
                }
            },
            onBack = navController::popBackStack
        ) { innerPadding ->
            InvoiceShareScreenContent(
                innerPadding,
                listState,
                setInnerHeight = { innerHeight = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(SizeDefaults.fortyFourfold)
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
            )
        }
    }
}

@Composable
fun InvoiceShareScreenScaffold(
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    onBottomBarClick: () -> Unit,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .testTag(TestTag.Profile.InvoiceShareScreen),
        topBarTitle = "",
        navigationMode = NavigationBarMode.Close,
        scaffoldState = scaffoldState,
        bottomBar = {
            ShareInformationBottomBar(onClickShare = onBottomBarClick)
        },
        listState = listState,
        actions = {},
        onBack = onBack,
        content = content
    )
}

@Composable
private fun InvoiceShareScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState,
    setInnerHeight: (Int) -> Unit
) {
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
                setInnerHeight(it.boundsInWindow().size.height.toInt())
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.share_sheet),
                contentDescription = null
            )
        }
        item {
            InformationHeader()
        }
        item {
            InformationLabel(
                Icons.Rounded.ImportExport,
                stringResource(R.string.share_information_app_share_info)
            )
        }
        item {
            Text(
                stringResource(R.string.share_information_or).uppercase(),
                style = AppTheme.typography.subtitle2l
            )
        }
        item {
            InformationLabel(
                Icons.Rounded.SaveAlt,
                stringResource(R.string.share_information_app_save_info)
            )
        }
        item {
            SpacerLarge()
        }
    }
}

@Composable
private fun InformationLabel(icon: ImageVector, info: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
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
private fun InformationHeader() {
    Text(
        text = stringResource(R.string.share_invoice_information_header),
        modifier = Modifier
            .padding(top = PaddingDefaults.XXLarge),
        style = AppTheme.typography.subtitle1
    )
    SpacerSmall()
}

@Composable
private fun ShareInformationBottomBar(onClickShare: () -> Unit) {
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
            contentPadding = PaddingValues(horizontal = PaddingDefaults.XXLarge, vertical = PaddingDefaults.ShortMedium)
        ) {
            Text(text = stringResource(R.string.invoice_share_okay))
        }
    }
}

@LightDarkPreview
@Composable
fun InvoiceShareScreenScaffoldPreview() {
    PreviewAppTheme {
        InvoiceShareScreenScaffold(
            scaffoldState = rememberScaffoldState(),
            listState = rememberLazyListState(),
            onBottomBarClick = {},
            onBack = {}
        ) {
            InvoiceShareScreenContent(
                innerPadding = PaddingValues(),
                listState = rememberLazyListState(),
                setInnerHeight = {}
            )
        }
    }
}
