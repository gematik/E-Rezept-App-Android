/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.orderhealthcard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardScreen
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardContactOption
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardGraphController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class OrderHealthCardSelectOptionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: OrderHealthCardGraphController
) : OrderHealthCardScreen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        OrderHealthCardSelectOptionScreenScaffold(
            listState = listState,
            onBack = { navController.popBackStack() },
            onClose = { navController.popBackStack(OrderHealthCardRoutes.subGraphName(), inclusive = true) },
            onSelectOption = {
                graphController.setContactOption(it)
                navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectMethodScreen.path())
            }
        )
    }
}

@Composable
private fun OrderHealthCardSelectOptionScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSelectOption: (OrderHealthCardContactOption) -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Settings.OrderEgk.SelectOrderOptionScreen),
        topBarTitle = "",
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        actions = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.health_card_order_cancel))
            }
        }
    ) {
        OrderHealthCardSelectOptionScreenContent(
            listState = listState,
            onSelectOption = onSelectOption
        )
    }
}

@Composable
private fun OrderHealthCardSelectOptionScreenContent(
    listState: LazyListState,
    onSelectOption: (OrderHealthCardContactOption) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTag.Settings.OrderEgk.SelectOrderOptionContent)
            .padding(horizontal = PaddingDefaults.Medium),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerXXLarge()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(painter = painterResource(R.drawable.egk_on_blue_circle), contentDescription = null)
            }
        }
        item {
            SpacerXXLarge()
            SpacerXXLarge()
            Text(
                stringResource(id = R.string.select_order_option_header),
                style = AppTheme.typography.h5,
                textAlign = TextAlign.Center
            )
        }
        item {
            SpacerSmall()
            Text(
                stringResource(id = R.string.select_order_option_info),
                style = AppTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
            SpacerXXLarge()
        }
        item {
            Option(
                testTag = TestTag.Settings.ContactInsuranceCompany.OrderPinButton,
                name = stringResource(R.string.cdw_health_insurance_contact_pin_only),
                onSelect = { onSelectOption(OrderHealthCardContactOption.PinOnly) }
            )
            SpacerMedium()
        }
        item {
            Option(
                testTag = TestTag.Settings.ContactInsuranceCompany.OrderEgkAndPinButton,
                name = stringResource(R.string.cdw_health_insurance_contact_healthcard_pin),
                onSelect = { onSelectOption(OrderHealthCardContactOption.WithHealthCardAndPin) }
            )
        }
    }
}

@Composable
private fun Option(
    name: String,
    testTag: String,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(SizeDefaults.double)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(shape)
            .border(1.dp, color = AppTheme.colors.neutral300, shape = shape)
            .clickable(onClick = onSelect)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(name, style = AppTheme.typography.body1, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@LightDarkPreview
@Composable
fun OrderHealthCardSelectOptionScreenPreview() {
    val listState = rememberLazyListState()

    PreviewAppTheme {
        OrderHealthCardSelectOptionScreenScaffold(
            listState,
            onBack = {},
            onClose = {},
            onSelectOption = {}
        )
    }
}
