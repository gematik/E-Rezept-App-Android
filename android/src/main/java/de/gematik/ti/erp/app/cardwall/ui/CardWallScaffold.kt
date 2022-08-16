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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight

@Composable
fun CardWallIntroScaffold(
    onNext: () -> Unit,
    onClickAlternateAuthentication: () -> Unit,
    onClickOrderNow: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val activity = LocalActivity.current

    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.CardWall.Login.LoginScreen),
        topBarTitle = "",
        elevated = scrollState.value > 0,
        navigationMode = null,
        actions = actions,
        bottomBar = {
            CardWallIntroBottomBar(
                onNext = onNext,
                onClickAlternateAuthentication = onClickAlternateAuthentication
            )
        },
        onBack = { activity.onBackPressed() }
    ) {
        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            AddCardContent(
                onClickOrderNow = onClickOrderNow
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
        modifier = Modifier,
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

@Composable
fun CardWallMissingCapabilities() {
    val activity = LocalActivity.current
    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag("cardWall/intro"),
        topBarTitle = "",
        elevated = scrollState.value > 0,
        actions = @Composable {
            TextButton(onClick = { activity.onBackPressed() }) {
                Text(stringResource(R.string.cdw_missing_capabilities_close))
            }
        },
        navigationMode = null,
        onBack = {}
    ) {
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
                .padding(PaddingDefaults.Medium)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.cdw_missing_capabilities_title),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_missing_capabilities_info),
                style = AppTheme.typography.body1
            )
            SpacerSmall()
            ClickableTaggedText(
                text = annotatedLinkStringLight(
                    uri = stringResource(R.string.cdw_missing_capabilities_link_to_faq),
                    text = stringResource(R.string.cdw_missing_capabilities_learn_more)
                ),
                onClick = {
                    uriHandler.openUri(it.item)
                },
                style = AppTheme.typography.body2.merge(
                    TextStyle(textAlign = TextAlign.End)
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun AddCardContent(
    onClickOrderNow: () -> Unit
) {
    Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
        Text(
            stringResource(R.string.cdw_intro_header),
            style = AppTheme.typography.h5,
            modifier = Modifier.testTag("cdw_txt_intro_header_bottom")
        )
        SpacerSmall()
        Text(
            stringResource(R.string.cdw_intro_info),
            style = AppTheme.typography.body1
        )
        SpacerLarge()
        Text(
            stringResource(R.string.cdw_intro_what_you_need),
            style = AppTheme.typography.subtitle1
        )
        SpacerMedium()

        val uriHandler = LocalUriHandler.current
        val link = stringResource(R.string.cdw_link_health_card_info)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { uriHandler.openUri(link) }
                .padding(vertical = PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green500)
            SpacerMedium()
            Text(
                stringResource(R.string.cdw_intro_nfc_card_needed),
                style = AppTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )

            SpacerMedium()
            Icon(
                Icons.Outlined.Info,
                null,
                tint = AppTheme.colors.primary600
            )
        }

        SimpleCheck(stringResource(R.string.cdw_intro_pin_needed))
        SpacerMedium()

        Text(
            text = stringResource(R.string.cdw_have_no_card_with_pin),
            style = AppTheme.typography.body2l
        )

        HintTextActionButton(
            text = stringResource(R.string.cdw_intro_order_now),
            align = Alignment.End,
            modifier = Modifier.align(Alignment.End)
        ) {
            onClickOrderNow()
        }
    }
}
