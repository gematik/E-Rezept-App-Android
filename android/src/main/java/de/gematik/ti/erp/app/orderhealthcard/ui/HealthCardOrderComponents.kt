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

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.orderhealthcard.ui.model.HealthCardOrderViewModelData
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import de.gematik.ti.erp.app.settings.ui.openMailClient
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
import org.kodein.di.compose.rememberViewModel

@Composable
fun HealthCardContactOrderScreen(
    onBack: () -> Unit
) {
    val healthCardOrderViewModel by rememberViewModel<HealthCardOrderViewModel>()
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
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    modifier = Modifier.systemBarsPadding(),
                    topBarTitle = title,
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    navigationMode = NavigationBarMode.Close,
                    onBack = onBack,
                    actions = {}
                ) {
                    HealthCardOrder(
                        listState = listState,
                        state = state,
                        onClickInsuranceSelector = { navController.navigate(HealthCardOrderNavigationScreens.HealthCardOrderInsuranceCompanies.path()) },
                        onSelectOption = { healthCardOrderViewModel.onSelectContactOption(it) }
                    )
                }
            }
        }
        composable(HealthCardOrderNavigationScreens.HealthCardOrderInsuranceCompanies.route) {
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    modifier = Modifier.systemBarsPadding(),
                    navigationMode = NavigationBarMode.Back,
                    topBarTitle = title,
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    onBack = { navController.popBackStack() },
                    content = {
                        HealthInsuranceSelector(
                            state = listState,
                            insuranceCompanies = state.companies,
                            selected = state.selectedCompany,
                            onSelectionChange = {
                                healthCardOrderViewModel.onSelectInsuranceCompany(it)
                                navController.popBackStack()
                            }
                        )
                    },
                    actions = {}
                )
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
            pinUrl = null,
            subjectCardAndPinMail = null,
            bodyCardAndPinMail = null,
            subjectPinMail = null,
            bodyPinMail = null
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
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
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
        Text(name, style = AppTheme.typography.body1, modifier = Modifier.weight(1f))
        if (selected) {
            SpacerMedium()
            Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.primary600)
        }
    }
}

@Composable
private fun HealthCardOrder(
    listState: LazyListState = rememberLazyListState(),
    state: HealthCardOrderViewModelData.State,
    onClickInsuranceSelector: () -> Unit,
    onSelectOption: (HealthCardOrderViewModelData.ContactInsuranceOption) -> Unit
) {
    var onlyScrollOnce by remember { mutableStateOf(true) }

    LaunchedEffect(state.selectedCompany != null) {
        if (state.selectedCompany != null && onlyScrollOnce) {
            onlyScrollOnce = false
            listState.animateScrollToItem(2)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            Column(Modifier.padding(PaddingDefaults.Medium)) {
                Text(
                    stringResource(R.string.cdw_health_insurance_title),
                    style = AppTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W700
                )
                SpacerLarge()
                Text(
                    stringResource(R.string.cdw_health_insurance_body_what_you_need),
                    style = AppTheme.typography.body1
                )
                SpacerSmall()
                Text(stringResource(R.string.cdw_health_insurance_body_how_to_get), style = AppTheme.typography.body1)
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
        }

        item {
            SpacerXXLarge()

            Text(
                stringResource(R.string.cdw_health_insurance_select_company),
                style = AppTheme.typography.h6,
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
        }
        item {
            if (state.selectedCompany != null) {
                SpacerXXLarge()
                if (state.selectedCompany.noContactInformation()) {
                    NoContactsHint()
                } else {
                    Text(
                        stringResource(R.string.cdw_health_insurance_what_to_do),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                    )

                    SpacerMedium()
                    ContactInsuranceOptions(
                        company = state.selectedCompany,
                        selected = state.selectedOption,
                        onSelectionChange = onSelectOption
                    )

                    SpacerXXLarge()
                    Text(
                        stringResource(R.string.cdw_health_insurance_contact_insurance_company),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                    )
                    SpacerMedium()
                    ContactInsurance(
                        company = state.selectedCompany,
                        option = state.selectedOption
                    )
                }
            }
            SpacerMedium()
        }
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
            style = AppTheme.typography.body1,
            color = if (enabled) Color.Unspecified else AppTheme.colors.neutral400
        )
        Spacer(Modifier.weight(1f))
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = onSelect,
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
            mail = company.healthCardAndPinMail,
            company = company,
            option = option
        )
    }
    if (option == HealthCardOrderViewModelData.ContactInsuranceOption.PinOnly) {
        ContactMethodRow(
            phone = null,
            url = company.pinUrl,
            mail = if (company.hasMailContentForPin()) {
                company.healthCardAndPinMail
            } else { null },
            company = company,
            option = option
        )
    }
}

@Composable
private fun ContactMethodRow(
    phone: String?,
    url: String?,
    mail: String?,
    company: HealthCardOrderUseCaseData.HealthInsuranceCompany,
    option: HealthCardOrderViewModelData.ContactInsuranceOption
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
            val context = LocalContext.current
            ContactMethod(
                modifier = Modifier.weight(1f),
                name = "Mail",
                icon = Icons.Filled.MailOutline,
                onClick = {
                    when {
                        option == HealthCardOrderViewModelData.ContactInsuranceOption.WithHealthCardAndPin &&
                            company.hasMailContentForCardAndPin() -> openMailClient(context = context, address = it, subject = company.subjectCardAndPinMail!!, body = company.bodyCardAndPinMail!!)

                        option == HealthCardOrderViewModelData.ContactInsuranceOption.PinOnly &&
                            company.hasMailContentForPin() -> openMailClient(context = context, address = it, subject = company.subjectPinMail!!, body = company.bodyPinMail!!)

                        else -> uriHandler.openUri("mailto:$it?subject=${Uri.encode(mailSubject)}")
                    }
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
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentColor = AppTheme.colors.primary600,
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp
    ) {
        Column(Modifier.padding(PaddingDefaults.Medium), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null)
            SpacerSmall()
            Text(name, style = AppTheme.typography.subtitle2)
        }
    }
}
