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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationClose
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.WindowDecorationColors
import de.gematik.ti.erp.app.utils.compose.testId

@Composable
fun CardWallIntro(
    cardHelper: () -> Unit,
    onNext: () -> Unit
) {
    val activity = LocalActivity.current

    // this threshold defines the minimum scroll distance after which the top app bar gets elevated
    val threshold = with(LocalDensity.current) {
        10.dp.roundToPx()
    }

    val scrollState = rememberScrollState()

    var topBarElevation = 0.dp

    topBarElevation = when {
        scrollState.value > threshold -> AppBarDefaults.TopAppBarElevation
        scrollState.value < threshold / 2 -> 0.dp // divided by 2 leaves enough space to avoid flickering
        else -> topBarElevation
    }

    val topColor = AppTheme.colors.primary100
    WindowDecorationColors(topColor, resetColorsOnDispose = true)

    Scaffold(
        modifier = Modifier.testTag("cardWall/intro"),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.cdw_add_card))
                },
                backgroundColor = topColor,
                elevation = topBarElevation,
                navigationIcon = {
                    NavigationClose {
                        activity.onBackPressed()
                    }
                }
            )
        },
        bottomBar = {
            CardWallBottomBar(
                {
                    onNext()
                },
                true
            )
        }
    ) {
        Box(modifier = Modifier.verticalScroll(scrollState)) {
            Column(modifier = Modifier.padding(it)) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = topColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painterResource(R.drawable.card_wall_man),
                        null,
                        alignment = Alignment.BottomStart
                    )
                }
                SpacerSmall()
                Column(modifier = Modifier.padding(AppTheme.framePadding)) {
                    Text(
                        stringResource(R.string.cdw_intro_title),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.testId("cdw_txt_intro_header_bottom")
                    )
                    SpacerSmall()
                    Text(
                        stringResource(R.string.cdw_intro_body),
                        style = MaterialTheme.typography.body1
                    )
                    SpacerSmall()
                    HintTextLearnMoreButton()
                    SpacerMedium()
                    Text(
                        stringResource(R.string.cdw_intro_what_you_need),
                        style = MaterialTheme.typography.subtitle1
                    )
                    SpacerMedium()
                    SimpleCheck(stringResource(R.string.cdw_intro_what_you_need_egk))
                    SpacerMedium()
                    SimpleCheck(stringResource(R.string.cdw_intro_what_you_need_pin))
                    SpacerMedium()
                    SimpleCheck(stringResource(R.string.cdw_intro_what_you_need_nfc))
                    SpacerMedium()
                    Text(
                        stringResource(R.string.cdw_intro_what_you_need_no_egk),
                        style = MaterialTheme.typography.caption
                    )
                    SpacerSmall()
                    Row(modifier = Modifier.align(Alignment.End)) {
                        HintTextActionButton(text = stringResource(R.string.learn_more_btn)) {
                            cardHelper()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardWallMissingCapabilities() {
    val activity = LocalActivity.current
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                mode = NavigationBarMode.Close,
                headline = stringResource(R.string.cdw_capability_title),
                onClick = { activity.onBackPressed() }
            )
        }
    ) {
        Column {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .padding(AppTheme.framePadding)
                        .semantics(true) {
                            focused = true
                        }
                ) {
                    Image(
                        painterResource(id = R.drawable.oh_no),
                        null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SpacerSmall()
                    Text(
                        stringResource(R.string.cdw_capability_headline),
                        style = MaterialTheme.typography.h6
                    )
                    SpacerSmall()
                    Text(
                        stringResource(R.string.cdw_capability_body),
                        style = MaterialTheme.typography.body1
                    )
                    SpacerSmall()
                    Text(
                        stringResource(R.string.cdw_capability_more),
                        style = AppTheme.typography.body2l
                    )
                }
            }
        }
    }
}
