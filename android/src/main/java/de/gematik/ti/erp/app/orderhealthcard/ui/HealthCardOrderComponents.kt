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

package de.gematik.ti.erp.app.orderhealthcard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.rounded.ArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.orderhealthcard.ui.model.HealthCardOrderViewModelData
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import kotlinx.coroutines.flow.collect

@Composable
fun HealthCardContactOrderScreen(
    onBack: () -> Unit,
    healthCardOrderViewModel: HealthCardOrderViewModel = hiltViewModel()
) {
    val state by produceState(healthCardOrderViewModel.defaultState) {
        healthCardOrderViewModel.screenState().collect {
            value = it
        }
    }

    val navController = rememberNavController()

    val title = stringResource(R.string.cdw_health_insurance_page_title)

    val navigationMode by navController.navigationModeState(HealthCardOrderNavigationScreens.HealthCardOrder.route)
    NavHost(
        navController,
        startDestination = HealthCardOrderNavigationScreens.HealthCardOrder.path()
    ) {
        composable(HealthCardOrderNavigationScreens.HealthCardOrder.route) {
            val scrollState = rememberScrollState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    topBarTitle = title,
                    elevated = scrollState.value > 0,
                    navigationMode = NavigationBarMode.Close,
                    onBack = onBack
                ) {
                    Box(
                        Modifier
                            .verticalScroll(scrollState)
                            .padding(
                                rememberInsetsPaddingValues(
                                    insets = LocalWindowInsets.current.systemBars,
                                    applyBottom = true
                                )
                            )
                    ) {
                        HealthCardOrder(
                            state = state,
                            onClickInsuranceSelector = { navController.navigate(HealthCardOrderNavigationScreens.HealthCardOrderInsuranceCompanies.path()) },
                            onSelectOption = { healthCardOrderViewModel.onSelectContactOption(it) }
                        )
                    }
                }
            }
        }
        composable(HealthCardOrderNavigationScreens.HealthCardOrderInsuranceCompanies.route) {
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    topBarTitle = title,
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    navigationMode = NavigationBarMode.Back,
                    onBack = { navController.popBackStack() }
                ) {
                    HealthInsuranceSelector(
                        state = listState,
                        insuranceCompanies = state.companies,
                        selected = state.selectedCompany,
                        onSelectionChange = { healthCardOrderViewModel.onSelectInsuranceCompany(it) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HealthInsuranceSelectorPreview() {
    val insuranceCompanies = (0..9).map {
        HealthCardOrderUseCaseData.HealthInsuranceCompany(
            name = "CardWallData.HealthInsuranceCompany $it",
            healthCardAndPinPhone = null,
            healthCardAndPinMail = null,
            healthCardAndPinUrl = null,
            pinUrl = null
        )
    }
    AppTheme {
        var selected by remember { mutableStateOf<HealthCardOrderUseCaseData.HealthInsuranceCompany?>(null) }
        HealthInsuranceSelector(
            insuranceCompanies = insuranceCompanies,
            selected = selected,
            onSelectionChange = { selected = it }
        )
    }
}

@Composable
private fun HealthInsuranceSelector(
    state: LazyListState = rememberLazyListState(),
    insuranceCompanies: List<HealthCardOrderUseCaseData.HealthInsuranceCompany>,
    selected: HealthCardOrderUseCaseData.HealthInsuranceCompany?,
    onSelectionChange: (HealthCardOrderUseCaseData.HealthInsuranceCompany) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.navigationBars,
            applyBottom = true
        )
    ) {
        items(insuranceCompanies) { company ->
            HealthInsuranceCompanySelectable(
                name = company.name,
                selected = company == selected,
                onSelectionChange = { onSelectionChange(company) }
            )
        }
    }
}

@Composable
private fun HealthInsuranceCompanySelectable(
    name: String,
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .toggleable(
                value = selected,
                role = Role.RadioButton,
                onValueChange = onSelectionChange
            )
            .heightIn(min = 56.dp)
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1f))
        if (selected) {
            SpacerMedium()
            Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.primary600)
        }
    }
}

@Composable
private fun HealthCardOrder(
    state: HealthCardOrderViewModelData.State,
    onClickInsuranceSelector: () -> Unit,
    onSelectOption: (HealthCardOrderViewModelData.ContactInsuranceOption) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.cdw_health_insurance_title),
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center
            )
            SpacerLarge()
            Text(
                stringResource(R.string.cdw_health_insurance_body_what_you_need),
                style = MaterialTheme.typography.body1
            )
            SpacerSmall()
            Text(stringResource(R.string.cdw_health_insurance_body_how_to_get), style = MaterialTheme.typography.body1)
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_health_insurance_caption_recognize_healthcard),
                style = AppTheme.typography.body2l
            )
            SpacerSmall()
            HintTextLearnMoreButton(
                modifier = Modifier.align(Alignment.End),
                uri = stringResource(R.string.cdw_health_insurance_learn_more),
                align = Alignment.End
            )
        }

        SpacerXXLarge()

        Text(
            stringResource(R.string.cdw_health_insurance_select_company),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
        SpacerMedium()

        // insurance company selection button
        TextButton(
            onClick = onClickInsuranceSelector,
            contentPadding = PaddingValues(vertical = PaddingDefaults.Medium, horizontal = PaddingDefaults.Small),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Small)
        ) {
            Text(state.selectedCompany?.name ?: stringResource(R.string.cdw_health_insurance_no_company_selected))
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.ArrowRight, null)
        }

        if (state.selectedCompany != null) {
            SpacerXXLarge()
            if (state.selectedCompany.noContactInformation()) {
                NoContactsHint()
            } else {
                Text(
                    stringResource(R.string.cdw_health_insurance_what_to_do),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                )

                SpacerMedium()
                ContactInsuranceOptions(
                    company = state.selectedCompany,
                    selected = state.selectedOption,
                    onSelectionChange = onSelectOption
                )

                if (state.selectedOption != HealthCardOrderViewModelData.ContactInsuranceOption.None) {
                    SpacerXXLarge()
                    Text(
                        stringResource(R.string.cdw_health_insurance_contact_insurance_company),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                    )
                    SpacerMedium()
                    ContactInsurance(
                        company = state.selectedCompany, option = state.selectedOption
                    )
                }
            }
        }

        SpacerMedium()
    }
}

@Composable
private fun NoContactsHint() =
    HintCard(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        properties = HintCardDefaults.flatPropertiesAlert(),
        image = {
            HintSmallImage(
                painterResource(R.drawable.pharmacist_circle_red),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.cdw_health_insurance_no_cantacts_title)) },
        body = { Text(stringResource(R.string.cdw_health_insurance_no_cantacts_body)) }
    )

@Composable
private fun ContactInsuranceOptions(
    company: HealthCardOrderUseCaseData.HealthInsuranceCompany,
    selected: HealthCardOrderViewModelData.ContactInsuranceOption,
    onSelectionChange: (HealthCardOrderViewModelData.ContactInsuranceOption) -> Unit
) {
    Column {
        Option(
            name = stringResource(R.string.cdw_health_insurance_contact_healthcard_pin),
            enabled = company.hasContactInfoForHealthCardAndPin(),
            selected = selected == HealthCardOrderViewModelData.ContactInsuranceOption.WithHealthCardAndPin,
            onSelect = { onSelectionChange(HealthCardOrderViewModelData.ContactInsuranceOption.WithHealthCardAndPin) }
        )
        Option(
            name = stringResource(R.string.cdw_health_insurance_contact_pin_only),
            enabled = company.hasContactInfoForPin(),
            selected = selected == HealthCardOrderViewModelData.ContactInsuranceOption.PinOnly,
            onSelect = { onSelectionChange(HealthCardOrderViewModelData.ContactInsuranceOption.PinOnly) }
        )
    }
}

@Composable
private fun Option(
    name: String,
    enabled: Boolean,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(Modifier.padding(PaddingDefaults.Medium), verticalAlignment = Alignment.CenterVertically) {
        Text(
            name,
            style = MaterialTheme.typography.body1,
            color = if (enabled) Color.Unspecified else AppTheme.colors.neutral400
        )
        Spacer(Modifier.weight(1f))
        RadioButton(
            selected = selected, enabled = enabled, onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.colors.primary600,
                unselectedColor = AppTheme.colors.neutral400,
                disabledColor = AppTheme.colors.neutral300
            )
        )
    }
}

@Composable
private fun ContactInsurance(
    company: HealthCardOrderUseCaseData.HealthInsuranceCompany,
    option: HealthCardOrderViewModelData.ContactInsuranceOption
) {
    if (option == HealthCardOrderViewModelData.ContactInsuranceOption.WithHealthCardAndPin) {
        ContactMethodRow(
            phone = company.healthCardAndPinPhone,
            url = company.healthCardAndPinUrl,
            mail = company.healthCardAndPinMail
        )
    }
    if (option == HealthCardOrderViewModelData.ContactInsuranceOption.PinOnly) {
        ContactMethodRow(
            phone = null,
            url = company.pinUrl,
            mail = null
        )
    }
}

@Composable
private fun ContactMethodRow(
    phone: String?,
    url: String?,
    mail: String?,
) {
    val uriHandler = LocalUriHandler.current

    val mailSubject = stringResource(R.string.cdw_health_insurance_mail_subject)

    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
    ) {
        phone?.let {
            ContactMethod(
                modifier = Modifier.weight(1f),
                name = "Telefon",
                icon = Icons.Filled.PhoneInTalk,
                onClick = {
                    uriHandler.openUri("tel:$it")
                }
            )
        }
        url?.let {
            ContactMethod(
                modifier = Modifier.weight(1f),
                name = "Website",
                icon = Icons.Filled.OpenInBrowser,
                onClick = {
                    uriHandler.openUri(it)
                }
            )
        }
        mail?.let {
            ContactMethod(
                modifier = Modifier.weight(1f),
                name = "Mail",
                icon = Icons.Filled.MailOutline,
                onClick = {
                    uriHandler.openUri("mailto:$it?subject=$mailSubject")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ContactMethod(
    modifier: Modifier,
    name: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        contentColor = AppTheme.colors.primary600,
        role = Role.Button,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp
    ) {
        Column(Modifier.padding(PaddingDefaults.Medium), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null)
            SpacerSmall()
            Text(name, style = MaterialTheme.typography.subtitle2)
        }
    }
}
