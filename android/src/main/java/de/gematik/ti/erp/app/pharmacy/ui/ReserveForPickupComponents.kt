/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption.Local
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer48
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import kotlinx.coroutines.flow.collect
import java.util.Locale

@Composable
fun ReserveForPickupInPharmacy(
    navController: NavController,
    viewModel: PharmacySearchViewModel,
    taskIds: List<String>,
    pharmacyName: String,
    telematikId: String
) {
    val prescriptions by produceState(initialValue = listOf<UIPrescriptionOrder>()) {
        taskIds.takeIf { it.isNotEmpty() }?.let { ids ->
            viewModel.fetchSelectedOrders(ids).collect { value = it }
        }
    }
    val header = stringResource(id = R.string.reserve_header)
    val alpha = if (viewModel.uiState.loading) ContentAlpha.medium else ContentAlpha.high
    HeaderWithScaffold(
        navController = navController,
        viewModel = viewModel,
        telematikId = telematikId,
        redeemOption = Local,
        header = header,
        uiState = viewModel.uiState
    ) {
        item {
            DescriptionHeader(pharmacyName = pharmacyName)
            Spacer48()
        }
        item {
            PrescriptionHeader(alpha)
            Spacer16()
        }
        items(items = prescriptions) { prescription ->
            PrescriptionOrder(
                prescription,
                toggleContentDescription = "contentDescription",
                alpha,
                viewModel::toggleOrder,
            )
        }
        item {
            Spacer48()
        }
    }
}

@Composable
fun HeaderWithScaffold(
    navController: NavController,
    viewModel: PharmacySearchViewModel,
    telematikId: String,
    header: String,
    redeemOption: RemoteRedeemOption,
    uiState: PharmacySearchViewModel.RedeemUIState,
    items: LazyListScope.() -> Unit
) {
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate(PharmacyNavigationScreens.UploadStatus.path(redeemOption.ordinal))
        }
    }
    val message = stringResource(id = R.string.redeem_online_error_uploading)
    val actionLabel = stringResource(id = R.string.redeem_online_error_retry_label)

    val scaffoldState = rememberScaffoldState()

    if (uiState.error) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            val result = scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.triggerOrderInPharmacy(telematikId, redeemOption)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            ErrorSnackBar(snackBarHostState = it)
        },
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                title = header,
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            RedeemFab(uiState.fabState, Icons.Default.FileUpload) {
                viewModel.triggerOrderInPharmacy(
                    telematikId, redeemOption
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxHeight()) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                content = items,
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.navigationBars,
                    applyBottom = true
                )
            )
            LoadingIndicator(visible = uiState.loading, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ErrorSnackBar(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = snackBarHostState,
        snackbar = { data ->
            Snackbar(
                backgroundColor = AppTheme.colors.neutral900,
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(
                        onClick = { snackBarHostState.currentSnackbarData?.performAction() }
                    ) {
                        Text(
                            text = data.actionLabel ?: "",
                            color = AppTheme.colors.primary600
                        )
                    }
                }
            ) {
                Text(
                    text = snackBarHostState.currentSnackbarData?.message ?: "",
                    color = AppTheme.colors.neutral300
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun DescriptionHeader(pharmacyName: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        text = annotatedStringResource(
            id = R.string.pharm_reserve_subheader,
            buildAnnotatedString {
                pushStyle(SpanStyle(color = AppTheme.colors.neutral900))
                append(pharmacyName)
            }
        ),
        textAlign = TextAlign.Center,
        style = AppTheme.typography.subtitle1l
    )
}

@Composable
fun PrescriptionHeader(contentAlpha: Float = 1f) {
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
        Text(
            text = stringResource(id = R.string.pharm_reserve_prescriptions),
            style = MaterialTheme.typography.h6,
        )
    }
}

@Composable
fun LoadingIndicator(visible: Boolean, modifier: Modifier = Modifier) {
    if (visible) {
        CircularProgressIndicator(modifier = modifier)
    }
}

@Composable
fun PrescriptionOrder(
    order: UIPrescriptionOrder,
    toggleContentDescription: String,
    contentAlpha: Float = 1f,
    onAddOrder: (UIPrescriptionOrder) -> Boolean
) {
    var selected by remember { mutableStateOf(order.selected) }
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val alpha = remember { Animatable(0.0f) }
            LaunchedEffect(selected) {
                if (selected) {
                    alpha.animateTo(1.0f)
                } else {
                    alpha.animateTo(0.0f)
                }
            }

            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .weight(1f)
            ) {
                Text(text = order.title ?: "", style = MaterialTheme.typography.subtitle1)
                Spacer4()
                if (order.substitutionsAllowed) {
                    Text(
                        text = stringResource(id = R.string.pres_detail_aut_idem_info),
                        style = AppTheme.typography.body2l
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
                    .toggleable(
                        value = selected,
                        onValueChange = {
                            selected = onAddOrder(order)
                        },
                        role = Role.Checkbox,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(
                            bounded = false,
                            radius = 16.dp
                        )
                    )
                    .semantics {
                        contentDescription = toggleContentDescription
                    }
            ) {
                Icon(
                    Icons.Rounded.RadioButtonUnchecked, null,
                    tint = AppTheme.colors.neutral400.copy(alpha = LocalContentAlpha.current)
                )
                Icon(
                    Icons.Rounded.CheckCircle, null,
                    tint = AppTheme.colors.primary600.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier.alpha(alpha.value)
                )
            }
        }
    }
}

@Composable
fun RedeemFab(
    fabState: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    if (dialogVisible) {

        RedeemAlertDialog(
            onRedeem = {
                onClick()
                dialogVisible = false
            },
            onDismissRequest = {
                dialogVisible = false
            }
        )
    }

    val backgroundColor = if (fabState) {
        MaterialTheme.colors.secondary
    } else {
        AppTheme.colors.neutral300
    }

    val contentColor = if (fabState) {
        contentColorFor(backgroundColor)
    } else {
        AppTheme.colors.neutral500
    }

    FloatingActionButton(
        onClick = {
            if (fabState) {
                dialogVisible = true
            }
        },
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = "")
            Spacer4()
            Text(text = stringResource(id = R.string.pharm_redeem).uppercase(Locale.getDefault()))
        }
    }
}

@Composable
private fun RedeemAlertDialog(
    onDismissRequest: () -> Unit,
    onRedeem: () -> Unit
) =
    CommonAlertDialog(
        header = stringResource(R.string.redeem_online_detail_header),
        info = stringResource(R.string.redeem_online_detail_message),
        cancelText = stringResource(R.string.redeem_online_no),
        actionText = stringResource(R.string.redeem_online_yes),
        onCancel = onDismissRequest,
        onClickAction = onRedeem
    )
