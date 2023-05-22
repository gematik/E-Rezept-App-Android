/*
 * Copyright (c) 2023 gematik GmbH
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import de.gematik.ti.erp.app.settings.ui.openMailClient
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.navigationModeState

@Composable
fun HealthCardContactOrderScreen(
    onBack: () -> Unit
) {
    val healthCardOrderState = rememberHealthCardOrderState()
    val state by healthCardOrderState.state

    val navController = rememberNavController()
    var previousNavEntry by remember { mutableStateOf("contactInsuranceCompany") }
    TrackNavigationChanges(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })
    val title = stringResource(R.string.health_insurance_search_page_title)

    val navigationMode by navController.navigationModeState(HealthCardOrderNavigationScreens.HealthCardOrder.route)
    NavHost(
        navController,
        startDestination = HealthCardOrderNavigationScreens.HealthCardOrder.path()
    ) {
        composable(HealthCardOrderNavigationScreens.HealthCardOrder.route) {
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    modifier = Modifier.testTag(TestTag.Settings.OrderEgk.OrderEgkScreen),
                    topBarTitle = title,
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    navigationMode = NavigationBarMode.Back,
                    onBack = onBack,
                    actions = {}
                ) {
                    HealthCardOrder(
                        listState = listState,
                        healthCardOrderState = state,
                        onSelectCompany = {
                            healthCardOrderState.onSelectInsuranceCompany(it)
                            when (true) {
                                (it.hasContactInfoForHealthCardAndPin() && it.hasContactInfoForPin()) ->
                                    navController.navigate(HealthCardOrderNavigationScreens.SelectOrderOption.path())
                                it.hasContactInfoForHealthCardAndPin() -> {
                                    healthCardOrderState.onSelectContactOption(
                                        HealthCardOrderStateData.ContactInsuranceOption.WithHealthCardAndPin
                                    )
                                    navController.navigate(
                                        HealthCardOrderNavigationScreens.HealthCardOrderContact.path()
                                    )
                                }
                                it.hasContactInfoForPin() -> {
                                    healthCardOrderState.onSelectContactOption(
                                        HealthCardOrderStateData.ContactInsuranceOption.PinOnly
                                    )
                                    navController.navigate(
                                        HealthCardOrderNavigationScreens.HealthCardOrderContact.path()
                                    )
                                }
                                else -> {
                                    navController.navigate(
                                        HealthCardOrderNavigationScreens.HealthCardOrderContact.path()
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        composable(HealthCardOrderNavigationScreens.SelectOrderOption.route) {
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    modifier = Modifier.testTag(TestTag.Settings.OrderEgk.SelectOrderOptionScreen),
                    topBarTitle = "",
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    navigationMode = NavigationBarMode.Back,
                    onBack = { navController.popBackStack() },
                    actions = {
                        TextButton(onClick = onBack) {
                            Text(stringResource(R.string.health_card_order_cancel))
                        }
                    }
                ) {
                    SelectOrderOption(
                        listState = listState,
                        onSelectOption = {
                            healthCardOrderState.onSelectContactOption(it)
                            navController.navigate(HealthCardOrderNavigationScreens.HealthCardOrderContact.path())
                        }
                    )
                }
            }
        }
        composable(HealthCardOrderNavigationScreens.HealthCardOrderContact.route) {
            val listState = rememberLazyListState()

            NavigationAnimation(mode = navigationMode) {
                AnimatedElevationScaffold(
                    modifier = Modifier.testTag(TestTag.Settings.OrderEgk.HealthCardOrderContactScreen),
                    topBarTitle = "",
                    elevated = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0,
                    navigationMode = NavigationBarMode.Back,
                    onBack = { navController.popBackStack() },
                    actions = {
                        TextButton(onClick = onBack) {
                            Text(stringResource(R.string.health_card_order_close))
                        }
                    }
                ) {
                    ContactInsurance(
                        listState,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthInsuranceCompanySelectable(
    company: HealthCardOrderUseCaseData.HealthInsuranceCompany,
    onSelectionCompany: (HealthCardOrderUseCaseData.HealthInsuranceCompany) -> Unit
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

@Composable
private fun HealthCardOrder(
    listState: LazyListState = rememberLazyListState(),
    healthCardOrderState: HealthCardOrderStateData.HealthCardOrderState,
    onSelectCompany: (HealthCardOrderUseCaseData.HealthInsuranceCompany) -> Unit
) {
    var searchName by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTag.Settings.InsuranceCompanyList.InsuranceSelectionContent),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            InsuranceCompanySearchField(modifier = Modifier, searchValue = searchName, onSearchChange = {
                searchName = it.trim()
            })
            SpacerMedium()
        }
        items(
            healthCardOrderState.companies.filter {
                it.name.contains(searchName, true)
            }
        ) {
            HealthInsuranceCompanySelectable(it, onSelectCompany)
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
        shape = RoundedCornerShape(16.dp),
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
private fun SelectOrderOption(
    listState: LazyListState = rememberLazyListState(),
    onSelectOption: (HealthCardOrderStateData.ContactInsuranceOption) -> Unit
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
                onSelect = { onSelectOption(HealthCardOrderStateData.ContactInsuranceOption.PinOnly) }
            )
            SpacerMedium()
        }
        item {
            Option(
                testTag = TestTag.Settings.ContactInsuranceCompany.OrderEgkAndPinButton,
                name = stringResource(R.string.cdw_health_insurance_contact_healthcard_pin),
                onSelect = { onSelectOption(HealthCardOrderStateData.ContactInsuranceOption.WithHealthCardAndPin) }
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
    val shape = RoundedCornerShape(16.dp)
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

@Composable
private fun ContactInsurance(
    listState: LazyListState = rememberLazyListState(),
    healthCardOrderState: HealthCardOrderStateData.HealthCardOrderState
) {
    healthCardOrderState.selectedCompany?.let {
        if (healthCardOrderState.selectedCompany.noContactInformation()) {
            NoContactInformation()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.Settings.OrderEgk.HealthCardOrderContactScreenContent)
                    .padding(horizontal = PaddingDefaults.Medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = listState,
                contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
            ) {
                item {
                    val header = stringResource(R.string.order_health_card_contact_header)
                    val info = if (healthCardOrderState.selectedCompany.singleContactInformation()) {
                        stringResource(R.string.order_health_card_contact_info_single)
                    } else {
                        stringResource(R.string.order_health_card_contact_info)
                    }
                    SpacerMedium()
                    Text(
                        text = header,
                        style = AppTheme.typography.h5,
                        textAlign = TextAlign.Center
                    )
                    SpacerSmall()
                    Text(
                        text = info,
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral600,
                        textAlign = TextAlign.Center
                    )
                    SpacerXXLarge()
                }

                when (healthCardOrderState.selectedOption) {
                    HealthCardOrderStateData.ContactInsuranceOption.WithHealthCardAndPin ->
                        item {
                            ContactMethodRow(
                                phone = it.healthCardAndPinPhone,
                                url = it.healthCardAndPinUrl,
                                mail = it.healthCardAndPinMail,
                                company = it,
                                option = healthCardOrderState.selectedOption
                            )
                        }

                    else ->
                        item {
                            ContactMethodRow(
                                phone = null,
                                url = it.pinUrl,
                                mail = it.healthCardAndPinMail,
                                company = it,
                                option = healthCardOrderState.selectedOption
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun NoContactInformation() {
    Column(
        modifier = Modifier.fillMaxSize().padding(PaddingDefaults.Medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_girl),
            contentDescription = null,
            alignment = Alignment.Center
        )

        Text(
            stringResource(R.string.cdw_health_insurance_no_cantacts_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )

        SpacerSmall()
        Text(
            stringResource(R.string.cdw_health_insurance_no_cantacts_body),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContactMethodRow(
    phone: String?,
    url: String?,
    mail: String?,
    company: HealthCardOrderUseCaseData.HealthInsuranceCompany,
    option: HealthCardOrderStateData.ContactInsuranceOption
) {
    val uriHandler = LocalUriHandler.current

    val mailSubject = stringResource(R.string.cdw_health_insurance_mail_subject)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            phone?.let {
                ContactMethod(
                    modifier = Modifier
                        .testTag(TestTag.Settings.ContactInsuranceCompany.TelephoneButton),
                    name = stringResource(R.string.healthcard_order_phone),
                    icon = Icons.Filled.PhoneInTalk,
                    onClick = {
                        uriHandler.openUri("tel:$phone")
                    }
                )
            }
            url?.let {
                ContactMethod(
                    modifier = Modifier
                        .testTag(TestTag.Settings.ContactInsuranceCompany.WebsiteButton),
                    name = stringResource(R.string.healthcard_order_website),
                    icon = Icons.Filled.OpenInBrowser,
                    onClick = {
                        uriHandler.openUri(url)
                    }
                )
            }
            mail?.let {
                val context = LocalContext.current
                ContactMethod(
                    modifier = Modifier
                        .testTag(TestTag.Settings.ContactInsuranceCompany.MailToButton),
                    name = stringResource(R.string.healthcard_order_mail),
                    icon = Icons.Filled.MailOutline,
                    onClick = {
                        when {
                            option == HealthCardOrderStateData.ContactInsuranceOption.WithHealthCardAndPin &&
                                company.hasMailContentForCardAndPin() -> openMailClient(
                                context = context,
                                address = mail,
                                subject = company.subjectCardAndPinMail!!,
                                body = company.bodyCardAndPinMail!!
                            )

                            option == HealthCardOrderStateData.ContactInsuranceOption.PinOnly &&
                                company.hasMailContentForPin() -> openMailClient(
                                context = context,
                                address = mail,
                                subject = company.subjectPinMail!!,
                                body = company.bodyPinMail!!
                            )

                            else -> uriHandler.openUri("mailto:$mail?subject=${Uri.encode(mailSubject)}")
                        }
                    }
                )
            }
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
        shape = RoundedCornerShape(8.dp),
        contentColor = AppTheme.colors.primary600,
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        backgroundColor = AppTheme.colors.neutral050,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(PaddingDefaults.Medium), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null)
            SpacerSmall()
            Text(
                name,
                modifier = modifier.widthIn(72.dp),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.subtitle2
            )
        }
    }
}
