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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

const val CAN_LENGTH = 6

@Requirement(
    "O.Purp_2#2",
    "O.Data_6#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "CAN is used for eGK connection."
)
class CardWallCanScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val can by graphController.can.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.CAN.CANScreen),
            onBack = {
                navController.popBackStack()
            },
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = can.length == CAN_LENGTH,
            onNext = {
                navController.navigate(CardWallRoutes.CardWallPinScreen.path())
            },
            listState = lazyListState,
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(
                    onClick = {
                        graphController.reset()
                        navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            CanScreenContent(
                lazyListState = lazyListState,
                innerPadding = innerPadding,
                can = can,
                onClickLearnMore = { navController.navigate(MainNavigationScreens.OrderHealthCard.path()) },
                onNext = {
                    navController.navigate(CardWallRoutes.CardWallPinScreen.path())
                },
                onCanChange = { graphController.setCardAccessNumber(it) }
            )
        }
    }
}

@Composable
fun CanScreenContent(
    lazyListState: LazyListState,
    innerPadding: PaddingValues,
    onClickLearnMore: () -> Unit,
    onNext: () -> Unit,
    can: String,
    onCanChange: (String) -> Unit
) {
    val contentPadding by remember(innerPadding) {
        derivedStateOf {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            HealthCardCanImage()
        }
        item {
            CanDescription(onClickLearnMore)
            SpacerXXLarge()
        }
        item {
            CanInputField(
                modifier = Modifier.scrollOnFocus(to = 2, lazyListState),
                can = can,
                onCanChange = onCanChange,
                next = onNext
            )
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
                .testTag(TestTag.CardWall.CAN.OrderEgkButton)
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
    val canRegex = """^\d{0,$CAN_LENGTH}$""".toRegex()
    OutlinedTextField(
        modifier = modifier
            .testTag(TestTag.CardWall.CAN.CANField)
            .fillMaxWidth(),
        value = can,
        onValueChange = {
            if (it.matches(canRegex)) {
                onCanChange(it)
            }
        },
        label = { Text(stringResource(R.string.can_input_field_label)) },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(SizeDefaults.one),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedLabelColor = AppTheme.colors.neutral400,
            placeholderColor = AppTheme.colors.neutral400,
            trailingIconColor = AppTheme.colors.neutral400
        ),
        keyboardActions = KeyboardActions {
            if (can.length == CAN_LENGTH) {
                next()
            }
        }
    )
    SpacerTiny()
    Text(
        text = annotatedStringResource(
            R.string.cdw_can_length_info,
            CAN_LENGTH.toString()
        ),
        style = AppTheme.typography.caption1l
    )
}
