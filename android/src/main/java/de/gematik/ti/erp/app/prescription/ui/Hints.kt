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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedHintCard
import de.gematik.ti.erp.app.utils.compose.HintActionButton
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource

@Composable
fun PrescriptionScreenDemoModeActivatedCard(
    modifier: Modifier = Modifier,
    onClose: suspend () -> Unit
) {
    AnimatedHintCard(
        modifier = modifier,
        onTransitionEnd = {
            if (!it) {
                onClose()
            }
        },
        image = {
            HintSmallImage(
                painterResource(R.drawable.clapping_hands_hint_yellow),
                null,
                it
            )
        },
        title = { Text(stringResource(R.string.prescription_overview_hint_welcome_to_demo_headline)) },
        body = { Text(stringResource(R.string.prescription_overview_hint_welcome_to_demo_text)) },
        action = null
    )
}

@Preview
@Composable
private fun PrescriptionScreenDemoModeActivatedPreview() {
    AppTheme {
        PrescriptionScreenDemoModeActivatedCard(Modifier, {})
    }
}

@Composable
fun PrescriptionScreenTryDemoModeCard(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    onClose: suspend () -> Unit
) {
    AnimatedHintCard(
        modifier = modifier,
        onTransitionEnd = {
            if (!it) {
                onClose()
            }
        },
        image = { HintSmallImage(painterResource(R.drawable.health_card_hint_blue), null, it) },
        title = { Text(stringResource(R.string.prescription_overview_hint_link_to_demo_mode_headline)) },
        body = { Text(stringResource(R.string.prescription_overview_hint_link_to_demo_mode_text)) },
        action = {
            HintTextActionButton(
                stringResource(R.string.prescription_overview_hint_link_to_demo_mode_call_to_action_text),
                onClick = onClickAction
            )
        },
    )
}

@Preview
@Composable
private fun PrescriptionScreenTryDemoModePreview() {
    AppTheme {
        PrescriptionScreenTryDemoModeCard(Modifier, {}, {})
    }
}

@Composable
fun PrescriptionScreenDefineSecurityCard(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit
) {
    AnimatedHintCard(
        modifier = modifier,
        onTransitionEnd = {},
        image = {
            HintSmallImage(
                painterResource(R.drawable.pharmacist_with_phone_hint_blue),
                null,
                it
            )
        },
        title = { Text(stringResource(R.string.prescription_overview_hint_define_security_headline)) },
        body = { Text(stringResource(R.string.prescription_overview_hint_define_security_text)) },
        action = {
            HintTextActionButton(
                stringResource(R.string.prescription_overview_hint_define_security_call_to_action_text),
                onClick = onClickAction
            )
        },
        close = null
    )
}

@Preview
@Composable
private fun PrescriptionScreenDefineSecurityPreview() {
    AppTheme {
        PrescriptionScreenDefineSecurityCard(Modifier, {})
    }
}

@Composable
fun PrescriptionScreenNewPrescriptionsCard(
    modifier: Modifier = Modifier,
    countOfNewPrescriptions: Int,
    onClickAction: () -> Unit
) {
    AnimatedHintCard(
        modifier = modifier,
        onTransitionEnd = {},
        image = {
            Box(
                modifier = Modifier
                    .padding(it)
                    .align(Alignment.Top)
            ) {
                Image(
                    painterResource(R.drawable.medical_hand_out_circle_blue),
                    null,
                    modifier = Modifier
                        .size(80.dp)
                )
                Box(
                    modifier = Modifier
                        .background(
                            // color = AppTheme.colors.red600,
                            shape = CircleShape,
                            brush = Brush.linearGradient(
                                0.0f to AppTheme.colors.red700,
                                0.6f to AppTheme.colors.red600,
                                1.0f to AppTheme.colors.red500,
                                start = Offset(0.0f, 100.0f),
                                end = Offset(100.0f, 0.0f)
                            )
                        )
                        .size(32.dp)
                        .align(
                            Alignment.BottomEnd
                        )
                ) {
                    Text(
                        countOfNewPrescriptions.toString(),
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.neutral000,
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        },
        title = {
            Text(
                annotatedPluralsResource(
                    R.plurals.prescription_overview_hint_new_prescriptions_headline,
                    countOfNewPrescriptions,
                    AnnotatedString(countOfNewPrescriptions.toString())
                )
            )
        },
        body = { Text(stringResource(R.string.prescription_overview_hint_new_prescriptions_text)) },
        action = {
            HintActionButton(
                stringResource(R.string.prescription_overview_hint_new_prescriptions_call_to_action_text),
                onClick = onClickAction
            )
        },
        close = null
    )
}

@Preview
@Composable
private fun PrescriptionScreenNewPrescriptionsCardPreview() {
    AppTheme {
        PrescriptionScreenNewPrescriptionsCard(Modifier, 1, {})
    }
}
