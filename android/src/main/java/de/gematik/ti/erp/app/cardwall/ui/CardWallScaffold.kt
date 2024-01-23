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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge

@Requirement(
    "O.Auth_3#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Selection of Authentication with health card or insurance App"
)
@Composable
fun CardWallIntroScaffold(
    onNext: () -> Unit,
    onClickAlternateAuthentication: () -> Unit,
    onClickOrderNow: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    nfcEnabled: Boolean
) {
    val activity = LocalActivity.current

    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.CardWall.Intro.IntroScreen)
            .systemBarsPadding(),
        topBarTitle = "",
        elevated = scrollState.value > 0,
        navigationMode = null,
        actions = actions,
        bottomBar = {},
        onBack = { activity.onBackPressed() }
    ) {
        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            AddCardContent(
                onClickOrderNow = onClickOrderNow,
                onClickHealthCardAuth = onNext,
                onClickInsuranceAuth = onClickAlternateAuthentication,
                nfcAvailable = nfcEnabled
            )
        }
    }
}

@Composable
fun CardWallInfoScaffold(
    topColor: Color,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        modifier = Modifier.systemBarsPadding(),
        topBarTitle = stringResource(R.string.cdw_info_title),
        topBarColor = topColor,
        elevated = scrollState.value > 0,
        navigationMode = NavigationBarMode.Close,
        actions = { },
        bottomBar = {
            AlternativeInfoBottomBar(
                onNext = onNext
            )
        },
        onBack = { onCancel() }
    ) {
        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it),
            content = { content() }
        )
    }
}

val CardPaddingModifier = Modifier
    .padding(
        bottom = PaddingDefaults.Medium
    )
    .fillMaxWidth()

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddCardContent(
    onClickOrderNow: () -> Unit,
    onClickHealthCardAuth: () -> Unit,
    onClickInsuranceAuth: () -> Unit,
    nfcAvailable: Boolean
) {
    Column(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HealthCardPhoneImage()
        Text(
            stringResource(R.string.cdw_intro_header),
            style = AppTheme.typography.h5,
            color = AppTheme.colors.neutral900,
            modifier = Modifier.testTag("cdw_txt_intro_header_bottom")
        )
        SpacerSmall()
        Text(
            stringResource(R.string.cdw_intro_info),
            style = AppTheme.typography.subtitle2,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.neutral600,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        SpacerXLarge()
        Card(
            modifier = CardPaddingModifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
            elevation = 0.dp,
            backgroundColor = AppTheme.colors.neutral050,
            onClick = onClickInsuranceAuth
        ) {
            Row(
                modifier = Modifier.padding(
                    PaddingDefaults.Medium
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        stringResource(R.string.cdw_intro_auth_insurance_app),
                        style = AppTheme.typography.subtitle1l,
                        color = AppTheme.colors.neutral900
                    )
                    SpacerSmall()
                }
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral400,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
        Card(
            modifier = CardPaddingModifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
            elevation = 0.dp,
            backgroundColor = AppTheme.colors.neutral050,
            onClick = if (nfcAvailable) {
                onClickHealthCardAuth
            } else {
                { }
            }
        ) {
            Row(
                Modifier.padding(PaddingDefaults.Medium)
            ) {
                Column(
                    modifier = if (nfcAvailable) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.weight(1f).clickable { }
                    }
                ) {
                    Text(
                        stringResource(R.string.cdw_intro_auth_health_card),
                        style = AppTheme.typography.subtitle1l,
                        color = if (nfcAvailable) {
                            AppTheme.colors.neutral900
                        } else {
                            AppTheme.colors.neutral400
                        }
                    )
                    SpacerTiny()
                    Text(
                        stringResource(R.string.cdw_intro_auth_health_card_pin),
                        style = AppTheme.typography.body2l,
                        color = if (nfcAvailable) {
                            AppTheme.colors.neutral600
                        } else {
                            AppTheme.colors.neutral400
                        }
                    )
                    SpacerSmall()
                }
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    null,
                    tint = if (nfcAvailable) AppTheme.colors.neutral400 else AppTheme.colors.neutral300,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
        SpacerSmall()
        Text(
            text = stringResource(R.string.cdw_have_no_card_with_pin),
            style = AppTheme.typography.body2l
        )
        HintTextActionButton(
            text = stringResource(R.string.cdw_intro_order_now),
            align = Alignment.End,
            modifier = Modifier.align(Alignment.End).testTag(TestTag.CardWall.Intro.OrderEgkButton)
        ) {
            onClickOrderNow()
        }
    }
}

@Composable
fun HealthCardPhoneImage() {
    Column(modifier = Modifier.wrapContentHeight()) {
        Image(
            painterResource(R.drawable.card_wall_card_hand),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
