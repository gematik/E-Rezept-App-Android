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

package de.gematik.ti.erp.app.orderhealthcard.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardScreen
import de.gematik.ti.erp.app.orderhealthcard.presentation.HealthInsuranceCompany
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardContactOption
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardGraphController
import de.gematik.ti.erp.app.orderhealthcard.presentation.rememberHealthInsuranceListController
import de.gematik.ti.erp.app.orderhealthcard.ui.preview.HealthInsuranceSearchData
import de.gematik.ti.erp.app.orderhealthcard.ui.preview.OrderHealthCardPreviewParameter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class OrderHealthCardSelectInsuranceCompanyScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: OrderHealthCardGraphController
) : OrderHealthCardScreen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val healthInsuranceListController = rememberHealthInsuranceListController()
        val healthInsuranceList by healthInsuranceListController.healthInsuranceList

        var searchName by remember { mutableStateOf("") }
        val filteredList = remember(searchName, healthInsuranceList) {
            healthInsuranceList.filter { it.name.contains(searchName, true) }
        }

        OrderHealthCardSelectInsuranceCompanyScreenScaffold(
            listState = listState,
            onBack = { navController.popBackStack() },
            healthInsuranceList = filteredList,
            searchName = searchName,
            onSearchChange = { searchName = it },
            onSelectCompany = {
                graphController.setInsuranceCompany(it)
                when {
                    (it.hasContactInfoForHealthCardAndPin() && it.hasContactInfoForPin()) ->
                        navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectOptionScreen.path())
                    it.hasContactInfoForHealthCardAndPin() -> {
                        graphController.setContactOption(
                            OrderHealthCardContactOption.WithHealthCardAndPin
                        )
                        navController.navigate(
                            OrderHealthCardRoutes.OrderHealthCardSelectMethodScreen.path()
                        )
                    }
                    it.hasContactInfoForPin() -> {
                        graphController.setContactOption(
                            OrderHealthCardContactOption.PinOnly
                        )
                        navController.navigate(
                            OrderHealthCardRoutes.OrderHealthCardSelectMethodScreen.path()
                        )
                    }
                    else -> {
                        navController.navigate(
                            OrderHealthCardRoutes.OrderHealthCardSelectMethodScreen.path()
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun OrderHealthCardSelectInsuranceCompanyScreenScaffold(
    listState: LazyListState,
    healthInsuranceList: List<HealthInsuranceCompany>,
    searchName: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onSelectCompany: (HealthInsuranceCompany) -> Unit
) {
    AnimatedElevationScaffold(
        listState = listState,
        modifier = Modifier.testTag(TestTag.Settings.OrderEgk.OrderEgkScreen),
        topBarTitle = stringResource(R.string.health_insurance_search_page_title),
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        actions = {}
    ) {
        OrderHealthCardSelectInsuranceCompanyScreenContent(
            listState = listState,
            healthInsuranceList = healthInsuranceList,
            searchName = searchName,
            onSearchChange = onSearchChange,
            onSelectCompany = onSelectCompany
        )
    }
}

@Composable
private fun OrderHealthCardSelectInsuranceCompanyScreenContent(
    listState: LazyListState,
    healthInsuranceList: List<HealthInsuranceCompany>,
    searchName: String,
    onSearchChange: (String) -> Unit,
    onSelectCompany: (HealthInsuranceCompany) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTag.Settings.InsuranceCompanyList.InsuranceSelectionContent),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            InsuranceCompanySearchField(
                modifier = Modifier,
                searchValue = searchName,
                onSearchChange = onSearchChange
            )
            SpacerMedium()
        }
        items(
            healthInsuranceList.size
        ) { idx ->
            HealthInsuranceCompanySelectable(healthInsuranceList[idx], onSelectCompany)
        }
    }
}

@Composable
private fun InsuranceCompanySearchField(
    modifier: Modifier,
    searchValue: String,
    onSearchChange: (String) -> Unit
) {
    TextField(
        value = searchValue,
        placeholder = { Text(stringResource(R.string.health_card_search_field_place_holder)) },
        onValueChange = onSearchChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions {
            onSearchChange(searchValue)
        },
        visualTransformation = VisualTransformation.None,
        leadingIcon = {
            IconButton(
                onClick = { onSearchChange(searchValue) }
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(SizeDefaults.double),
        textStyle = AppTheme.typography.body1,
        colors = TextFieldDefaults.textFieldColors(
            textColor = AppTheme.colors.neutral900,
            leadingIconColor = AppTheme.colors.neutral600,
            trailingIconColor = AppTheme.colors.neutral600,
            backgroundColor = AppTheme.colors.neutral100,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun HealthInsuranceCompanySelectable(
    company: HealthInsuranceCompany,
    onSelectionCompany: (HealthInsuranceCompany) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onSelectionCompany(company) })
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
            .testTag(TestTag.Settings.InsuranceCompanyList.ListOfInsuranceButtons),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            company.name,
            style = AppTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@LightDarkPreview
@Composable
fun OrderHealthCardSelectInsuranceCompanyScreenPreview(
    @PreviewParameter(OrderHealthCardPreviewParameter::class) previewData: HealthInsuranceSearchData
) {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        var searchName by remember { mutableStateOf(previewData.searchText) }
        val healthInsuranceList = listOf(
            previewData.healthInsurance,
            previewData.healthInsurance.copy(name = "Insurance Company A"),
            previewData.healthInsurance.copy(name = "Insurance Company B"),
            previewData.healthInsurance.copy(name = "Insurance Company C"),
            previewData.healthInsurance.copy(name = "Insurance Company D"),
            previewData.healthInsurance.copy(name = "Insurance Company E"),
            previewData.healthInsurance.copy(name = "Insurance Company F"),
            previewData.healthInsurance.copy(name = "Insurance Company G")
        ).filter { it.name.contains(searchName, true) }

        OrderHealthCardSelectInsuranceCompanyScreenScaffold(
            listState = listState,
            onBack = {},
            onSelectCompany = {},
            healthInsuranceList = healthInsuranceList,
            searchName = searchName,
            onSearchChange = { searchName = it }
        )
    }
}
