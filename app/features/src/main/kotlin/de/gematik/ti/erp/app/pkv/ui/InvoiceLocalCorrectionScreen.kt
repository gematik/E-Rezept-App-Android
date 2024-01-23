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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.zxing.common.BitMatrix
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.navigation.PkvNavigationArguments
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.DataMatrix
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.createBitMatrix
import de.gematik.ti.erp.app.utils.extensions.forceBrightness

class InvoiceLocalCorrectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val pkvNavigationArguments = remember {
            val arguments = requireNotNull(navBackStackEntry.arguments)
            PkvNavigationArguments(
                taskId = requireNotNull(arguments.getString(PkvRoutes.TaskId)),
                profileId = requireNotNull(arguments.getString(PkvRoutes.ProfileId))
            )
        }
        val invoiceController = rememberInvoiceController(pkvNavigationArguments.profileId)
        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val invoice by produceState<InvoiceData.PKVInvoice?>(null) {
            invoiceController.detailState(pkvNavigationArguments.taskId).collect {
                value = it
            }
        }
        val matrix = remember(invoice) {
            invoice?.dmcPayload?.let {
                createBitMatrix(it)
            }
        }
        val activity = LocalActivity.current
        activity.forceBrightness()
        AnimatedElevationScaffold(
            modifier = Modifier
                .imePadding(),
            topBarTitle = "",
            navigationMode = null,
            scaffoldState = scaffoldState,
            listState = listState,
            actions = {
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text(stringResource(R.string.invoice_correct_done))
                }
            },
            onBack = { navController.popBackStack() }
        ) { innerPadding ->
            InvoiceLocalCorrectionScreenContent(
                innerPadding,
                listState,
                invoice,
                matrix
            )
        }
    }
}

@Composable
private fun InvoiceLocalCorrectionScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState,
    invoice: InvoiceData.PKVInvoice?,
    matrix: BitMatrix?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize().padding(innerPadding),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
        state = listState
    ) {
        item {
            Text(
                modifier = Modifier.padding(PaddingDefaults.Medium).fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.subtitle1,
                text = stringResource(R.string.invoice_correction_info)
            )
        }
        matrix?.let {
            item {
                DataMatrix(
                    modifier = Modifier
                        .padding(PaddingDefaults.Medium)
                        .fillMaxWidth(),
                    matrix = matrix
                )
            }
        }
        item {
            InvoiceLocalCorrectionSection(invoice)
        }
    }
}

@Composable
private fun InvoiceLocalCorrectionSection(invoice: InvoiceData.PKVInvoice?) {
    SpacerLarge()
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = AppTheme.typography.subtitle1,
        text = stringResource(R.string.invoice_correction)
    )
    SpacerSmall()
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = AppTheme.typography.body2l,
        text = invoice?.medicationRequest?.medication?.name() ?: ""
    )
}
