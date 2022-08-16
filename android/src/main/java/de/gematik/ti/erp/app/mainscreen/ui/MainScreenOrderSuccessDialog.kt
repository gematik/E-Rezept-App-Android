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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.VideoContent
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData.OrderOption.CourierDelivery
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData.OrderOption.MailDelivery
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData.OrderOption.ReserveInPharmacy
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

const val OrderSuccessVideoAspectRatio = 1.69f

@Composable
fun OrderSuccessDialog(
    mainScreenVM: MainScreenViewModel
) {
    val action = mainScreenVM.onActionEvent.collectAsState(null).value as? ActionEvent.ReturnFromPharmacyOrder
    var showDialog by remember(action) { mutableStateOf(action != null) }

    if (action != null && showDialog) {
        Dialog(
            onDismissRequest = {
                showDialog = false
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                Modifier
                    .semantics(false) { }
                    .fillMaxSize()
                    .background(SolidColor(Color.Black), alpha = 0.5f)
                    .systemBarsPadding()
                    .clickable(
                        onClick = { showDialog = false },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                var delayedVisibility by remember { mutableStateOf(false) }
                LaunchedEffect(showDialog) {
                    delayedVisibility = showDialog
                }
                AnimatedVisibility(
                    delayedVisibility,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(PaddingDefaults.Medium),
                        color = MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(28.dp),
                        elevation = 8.dp
                    ) {
                        Column {
                            VideoContent(
                                Modifier.fillMaxWidth(),
                                source = when (action.successfullyOrdered) {
                                    ReserveInPharmacy -> R.raw.animation_local
                                    CourierDelivery -> R.raw.animation_courier
                                    MailDelivery -> R.raw.animation_mail
                                },
                                aspectRatioOverwrite = OrderSuccessVideoAspectRatio
                            )
                            SpacerMedium()
                            Column(
                                Modifier.padding(horizontal = PaddingDefaults.Medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.main_order_success_title),
                                    textAlign = TextAlign.Center,
                                    style = AppTheme.typography.h6
                                )
                                SpacerSmall()
                                Text(
                                    stringResource(R.string.main_order_success_subtitle),
                                    textAlign = TextAlign.Center,
                                    style = AppTheme.typography.body1
                                )
                                TextButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.padding(PaddingDefaults.Large).align(Alignment.End)
                                ) {
                                    Text(stringResource(R.string.main_order_success_close))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
