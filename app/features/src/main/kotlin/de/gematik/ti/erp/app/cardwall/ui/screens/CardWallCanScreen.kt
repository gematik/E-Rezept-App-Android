/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.cardwall.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.CardWallCanScreen.getProfileIdentifier
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.extensions.accessibility
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.CanPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding

const val CAN_LENGTH = 6

@Requirement(
    "O.Data_6#9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = ".. the screen where CAN is entered from."
)
@Requirement(
    "O.Purp_2#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The CAN is needed for creating the secure channel to the eGK."
)
class CardWallCanScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val profileId = navBackStackEntry.getProfileIdentifier()
        profileId?.let { sharedViewModel.setProfileId(it) }

        val can by sharedViewModel.can.collectAsStateWithLifecycle()
        val scannedCan by sharedViewModel.scannedCan.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }

        LaunchedEffect(scannedCan) {
            scannedCan?.let { scanned ->
                sharedViewModel.setCardAccessNumber(scanned)
                sharedViewModel.setScannedCan(null)
            }
        }
        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.CAN.CANScreen),
            onBack = { onBack() },
            title = "",
            nextEnabled = can.length == CAN_LENGTH,
            onNext = {
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = "",
                        can = ""
                    )
                )
            },
            listState = lazyListState,
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(
                    onClick = {
                        navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
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
                onClickLearnMore = {
                    navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.path())
                },
                onClickScanNow = {
                    navController.navigate(CardWallRoutes.CardWallScannerScreen.path())
                },
                onNext = {
                    navController.navigate(
                        CardWallRoutes.CardWallPinScreen.path(
                            profileIdentifier = "",
                            can = ""
                        )
                    )
                },
                onCanChange = { sharedViewModel.setCardAccessNumber(it) }
            )
        }
    }
}

@Composable
fun CanScreenContent(
    lazyListState: LazyListState,
    innerPadding: PaddingValues,
    onClickLearnMore: () -> Unit,
    onClickScanNow: () -> Unit,
    onNext: () -> Unit,
    can: String,
    onCanChange: (String) -> Unit
) {
    val contentPadding by rememberContentPadding(innerPadding)

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isKeyboardOpen = imeInsets.getBottom(density) > 0

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            if (!isKeyboardOpen) {
                HealthCardCanImage()
            }
        }
        item {
            CanDescription(
                onClickScanNow = onClickScanNow
            )
        }
        item {
            CanInputFieldWithLink(
                modifier = Modifier,
                can = can,
                onCanChange = onCanChange,
                onClickLearnMore = onClickLearnMore,
                next = onNext
            )
        }
    }
}

@Composable
fun HealthCardCanImage() {
    Column(modifier = Modifier.wrapContentHeight()) {
        Image(
            painterResource(R.drawable.card_wall_card_can_redesign),
            null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .accessibility(
                    contentDescriptionRes = R.string.a11y_cdw_health_card_image_description,
                    role = Role.Image
                )
        )
        SpacerXXLarge()
    }
}

@Composable
fun CanDescription(
    onClickScanNow: () -> Unit
) {
    Column {
        Text(
            stringResource(R.string.cdw_can_headline),
            style = AppTheme.typography.h5,
            color = AppTheme.colors.neutral900,
            modifier = Modifier.accessibility(
                isHeading = true
            )
        )
        SpacerMedium()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier.clickable {
                    onClickScanNow()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier
                        .size(SizeDefaults.double)
                        .offset(x = (-SizeDefaults.one), y = SizeDefaults.quarter)
                )
                Text(
                    text = stringResource(R.string.cdw_scan_with_card),
                    color = AppTheme.colors.primary700,
                    style = AppTheme.typography.body2
                )
            }
        }
        SpacerMedium()
    }
}

@Requirement(
    "O.Data_6#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "CAN is used for eGK connection."
)
@Composable
fun CanInputFieldWithLink(
    modifier: Modifier,
    can: String,
    onCanChange: (String) -> Unit,
    onClickLearnMore: () -> Unit,
    next: () -> Unit
) {
    Column {
        CanInputField(
            modifier = modifier,
            can = can,
            onCanChange = onCanChange,
            next = next
        )
        SpacerMedium()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTag.CardWall.CAN.OrderEgkButton)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onClickLearnMore() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cdw_no_can_on_card),
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.primary700,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.width(SizeDefaults.half))

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(SizeDefaults.triple)
                )
            }
        }
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
    ErezeptOutlineText(
        modifier = modifier
            .testTag(TestTag.CardWall.CAN.CANField)
            .fillMaxWidth(),
        value = can,
        onValueChange = {
            if (it.matches(canRegex)) {
                onCanChange(it)
            }
        },
        label = stringResource(R.string.can_input_field_label),
        placeholder = stringResource(R.string.can_input_field_label),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(SizeDefaults.one),
        keyboardActions = KeyboardActions {
            if (can.length == CAN_LENGTH) {
                next()
            }
        }
    )
    SpacerSmall()
    Text(
        text = stringResource(R.string.cdw_puk_info),
        style = AppTheme.typography.caption1l,
        color = AppTheme.colors.neutral600
    )
}

@LightDarkPreview
@Composable
fun CardWallCanScreenPreview(@PreviewParameter(CanPreviewParameterProvider::class) can: String) {
    PreviewAppTheme {
        val lazyListState = rememberLazyListState()
        CanScreenContent(
            lazyListState = lazyListState,
            innerPadding = PaddingValues(PaddingDefaults.Medium),
            onClickLearnMore = {},
            onClickScanNow = {},
            onNext = {},
            can = can,
            onCanChange = {}
        )
    }
}
