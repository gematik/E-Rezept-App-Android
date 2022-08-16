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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.VerbatimTtsAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight

const val EXPECTED_CAN_LENGTH = 6

@Composable
fun CardAccessNumber(
    onClickLearnMore: () -> Unit,
    can: String,
    screenTitle: String,
    onCanChange: (String) -> Unit,
    onNext: () -> Unit,
    nextText: String? = null,
    onCancel: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/cardAccessNumber"),
        backMode = NavigationBarMode.Back,
        title = screenTitle,
        nextEnabled = can.length == EXPECTED_CAN_LENGTH,
        onNext = { onNext() },
        listState = lazyListState,
        nextText = nextText ?: stringResource(R.string.cdw_next),
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }

    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().padding(it),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                HealthCardCanImage()
            }
            item {
                CanDescription(onClickLearnMore)
                SpacerXXLarge()
                CanInputField(
                    modifier = Modifier
                        .scrollOnFocus(),
                    can = can,
                    onCanChange = onCanChange,
                    next = onNext
                )
                println("asdfadsfgsdgsdf")
            }
        }
    }
}

@Composable
fun HealthCardCanImage() {
    Column(modifier = Modifier.wrapContentHeight()) {
        Image(
            painterResource(R.drawable.card_wall_card_can),
            null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerXXLarge()
    }
}

@Composable
fun CanDescription(onClickLearnMore: () -> Unit) {
    Column {
        Text(
            stringResource(R.string.cdw_can_headline),
            style = AppTheme.typography.h5
        )

        SpacerSmall()

        Text(
            stringResource(R.string.cdw_can_description),
            style = AppTheme.typography.body1
        )

        SpacerSmall()

        ClickableTaggedText(
            text = annotatedLinkStringLight(
                uri = "",
                text = stringResource(R.string.cdw_no_can_on_card)
            ),
            onClick = { onClickLearnMore() },
            style = AppTheme.typography.body2,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun CanInputField(
    modifier: Modifier,
    can: String,
    onCanChange: (String) -> Unit,
    next: () -> Unit
) {
    Column(modifier) {
        val textValue = TextFieldValue(
            annotatedString = buildAnnotatedString {
                pushTtsAnnotation(VerbatimTtsAnnotation(can))
                append(can)
                pop()
            },
            selection = TextRange(can.length)
        )

        var isFocussed by remember { mutableStateOf(false) }
        val canRegex = """^\d{0,6}$""".toRegex()

        BasicTextField(
            value = textValue,
            onValueChange = {
                if (it.text.matches(canRegex)) {
                    onCanChange(it.text)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (can.length == EXPECTED_CAN_LENGTH) {
                        next()
                    }
                }
            ),
            singleLine = true,
            modifier = Modifier
                .testTag(TestTag.CardWall.CAN.CANField)
                .fillMaxWidth()
                .padding(start = PaddingDefaults.Large, bottom = PaddingDefaults.Small, end = PaddingDefaults.Large)
                .onFocusChanged {
                    isFocussed = it.isFocused
                }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    val shape = RoundedCornerShape(8.dp)
                    val backgroundColor = AppTheme.colors.neutral200
                    val borderModifier = Modifier.border(
                        BorderStroke(1.dp, color = AppTheme.colors.primary700),
                        shape
                    )

                    repeat(EXPECTED_CAN_LENGTH) {
                        Box(
                            modifier = Modifier
                                .size(40.dp, 48.dp)
                                .shadow(1.dp, shape)
                                .then(if (can.length == it && isFocussed) borderModifier else Modifier)
                                .background(
                                    color = backgroundColor,
                                    shape
                                )
                                .graphicsLayer {
                                    clip = false
                                }
                        ) {
                            Text(
                                text = can.getOrNull(it)?.toString() ?: " ",
                                style = AppTheme.typography.h6,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clearAndSetSemantics { }
                            )
                        }
                    }
                }
            }
        }
    }
}
