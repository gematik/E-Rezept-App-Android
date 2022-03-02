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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

enum class EmptyScreenState(val index: Int) {
    LoggedIn(0),
    LoggedOut(1),
    NoHealthCard(2),
    NotEmpty(3);

    companion object {
        fun ofValue(index: Int): EmptyScreenState? = values().find { it.index == index }
    }
}

@Composable
fun EmptyScreenHome(
    modifier: Modifier = Modifier,
    header: String,
    description: String,
    image: @Composable () -> Unit,
    button: @Composable () -> Unit,
    index: Int,
    displayedScreen: (EmptyScreenState) -> Unit
) {
    displayedScreen(EmptyScreenState.ofValue(index)!!)
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        image()
        SpacerMedium()
        Text(
            text = header,
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
        SpacerSmall()
        Text(
            text = description,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
        SpacerSmall()
        button()
    }
}

@Composable
fun HomeHealthCardConnected(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    displayedScreen: (EmptyScreenState) -> Unit
) =
    EmptyScreenHome(
        modifier = modifier,
        index = 0,
        displayedScreen = displayedScreen,
        header = stringResource(R.string.home_egk_redeemed_header),
        description = stringResource(R.string.home_egk_redeemed_description),
        image = {
            Image(
                painterResource(R.drawable.woman_red_shirt_circle_blue),
                contentDescription = null,
                modifier = Modifier.size(160.dp),
            )
        },
        button = {
            TextButton(
                onClick = onClickAction
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colors.primary600
                )
                SpacerSmall()
                Text(text = stringResource(R.string.home_egk_redeemed_buttontext), textAlign = TextAlign.Right)
            }
        }
    )

@Preview
@Composable
fun HomeEGKRedeemedPreview() {
    AppTheme {
        HomeHealthCardConnected(onClickAction = {}, displayedScreen = { })
    }
}

@Composable
fun HomeHealthCardDisconnected(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    displayedScreen: (EmptyScreenState) -> Unit
) =
    EmptyScreenHome(
        modifier = modifier,
        index = 1,
        displayedScreen = displayedScreen,
        header = stringResource(R.string.home_egk_notredeemable_header),
        description = stringResource(R.string.home_egk_notredeemable_description),
        image = {
            Image(
                painterResource(R.drawable.alarm_clock),
                contentDescription = null
            )
        },
        button = {
            TextButton(
                onClick = onClickAction
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colors.primary600
                )
                SpacerSmall()
                Text(text = stringResource(R.string.home_egk_notredeemable_buttontext), textAlign = TextAlign.Right)
            }
        }
    )

@Preview
@Composable
fun HomeEGKNotRedeemablePreview() {
    AppTheme {
        HomeHealthCardDisconnected(onClickAction = {}, displayedScreen = { })
    }
}

@Preview
@Composable
fun HomeNoEGKInitialPreview() {
    AppTheme {
        HomeNoHealthCard(onClickAction = {}, displayedScreen = { })
    }
}

@Composable
fun HomeNoHealthCard(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    displayedScreen: (EmptyScreenState) -> Unit
) = EmptyScreenHome(
    modifier = modifier,
    index = 2,
    displayedScreen = displayedScreen,
    header = stringResource(R.string.home_noegk_initial_header),
    description = stringResource(R.string.home_noegk_initial_description),
    image = {
        Image(
            painterResource(R.drawable.prescription),
            contentDescription = null
        )
    },
    button = {
        TextButton(onClick = onClickAction) {
            Icon(
                imageVector = Icons.Rounded.QrCode,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.primary600
            )
            SpacerSmall()
            Text(text = stringResource(R.string.home_noegk_initial_buttontext), textAlign = TextAlign.Right)
        }
    }
)

@Composable
fun EmptyScreenArchive(
    modifier: Modifier = Modifier,
    header: String,
    description: String,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerSmall()
        Text(
            text = description,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ArchiveNoEGKInitial(modifier: Modifier = Modifier) = EmptyScreenArchive(
    modifier = modifier,
    header = stringResource(R.string.archive_noegk_initial_header),
    description = stringResource(R.string.archive_noegk_initial_description)
)

@Composable
fun ArchiveNoEGKRedeemed(modifier: Modifier = Modifier) = EmptyScreenArchive(
    modifier = modifier,
    header = stringResource(R.string.archive_noegk_redeemed_header),
    description = stringResource(R.string.archive_noegk_redeemed_description)
)

@Preview
@Composable
fun ArchiveNoEGKInitialPreview() {
    AppTheme {
        ArchiveNoEGKInitial()
    }
}

@Preview
@Composable
fun ArchiveNoEGKRedeemedPreview() {
    AppTheme {
        ArchiveNoEGKRedeemed()
    }
}

@Composable
fun HomeNoHealthCardSignInHint(onClickAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        backgroundColor = AppTheme.colors.neutral050
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        ) {
            Text(
                text = stringResource(R.string.home_noegk_signin_hint_description),
                modifier = Modifier.padding(vertical = PaddingDefaults.Medium)
                    .fillMaxWidth()
                    .weight(1f),
                style = MaterialTheme.typography.body2,
            )
            TextButton(
                onClick = onClickAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.primary600),
                modifier = Modifier.align(Alignment.CenterVertically),
                contentPadding = PaddingValues(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
            ) {
                Text(
                    text = stringResource(R.string.home_noegk_signin_hint_buttontext),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}

@Preview
@Composable
fun SignInHintPreview() {
    AppTheme {
        HomeNoHealthCardSignInHint({})
    }
}
