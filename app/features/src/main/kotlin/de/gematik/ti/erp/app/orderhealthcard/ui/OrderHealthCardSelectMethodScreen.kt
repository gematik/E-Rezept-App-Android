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

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardScreen
import de.gematik.ti.erp.app.orderhealthcard.presentation.HealthInsuranceCompany
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardContactOption
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardGraphController
import de.gematik.ti.erp.app.orderhealthcard.ui.preview.OrderHealthCardPreviewData.healthInsuranceCompany
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
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import de.gematik.ti.erp.app.utils.openMailClient

class OrderHealthCardSelectMethodScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: OrderHealthCardGraphController
) : OrderHealthCardScreen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val selectedCompany by graphController.selectedInsuranceCompany.collectAsStateWithLifecycle()
        val selectedOption by graphController.selectedContactOption.collectAsStateWithLifecycle()
        OrderHealthCardSelectMethodScreenScaffold(
            listState,
            selectedCompany,
            selectedOption,
            onBack = { navController.popBackStack() },
            onClose = {
                navController.popBackStack(
                    OrderHealthCardRoutes.subGraphName(),
                    inclusive = true
                )
            }
        )
    }
}

@Composable
private fun OrderHealthCardSelectMethodScreenScaffold(
    listState: LazyListState,
    selectedCompany: HealthInsuranceCompany?,
    selectedOption: OrderHealthCardContactOption,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Settings.OrderEgk.HealthCardOrderContactScreen),
        topBarTitle = "",
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        actions = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.health_card_order_close))
            }
        }
    ) {
        OrderHealthCardSelectMethodScreenContent(
            listState,
            selectedCompany,
            selectedOption
        )
    }
}

@Composable
private fun OrderHealthCardSelectMethodScreenContent(
    listState: LazyListState = rememberLazyListState(),
    selectedCompany: HealthInsuranceCompany?,
    selectedOption: OrderHealthCardContactOption
) {
    selectedCompany?.let {
        if (selectedCompany.noContactInformation()) {
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
                    val info = if (selectedCompany.singleContactInformation()) {
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

                when (selectedOption) {
                    OrderHealthCardContactOption.WithHealthCardAndPin ->
                        item {
                            ContactMethodRow(
                                phone = it.healthCardAndPinPhone,
                                url = it.healthCardAndPinUrl,
                                mail = it.healthCardAndPinMail,
                                company = it,
                                option = selectedOption
                            )
                        }

                    else ->
                        item {
                            ContactMethodRow(
                                phone = it.healthCardAndPinPhone,
                                url = it.pinUrl,
                                mail = if (!it.subjectPinMail.isNullOrEmpty()) it.healthCardAndPinMail else null,
                                company = it,
                                option = selectedOption
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
    company: HealthInsuranceCompany,
    option: OrderHealthCardContactOption
) {
    val uriHandler = LocalUriHandler.current

    val mailSubject = stringResource(R.string.cdw_health_insurance_mail_subject)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SizeDefaults.one)
        ) {
            phone?.let {
                ContactMethod(
                    modifier = Modifier
                        .testTag(TestTag.Settings.ContactInsuranceCompany.TelephoneButton),
                    name = stringResource(R.string.healthcard_order_phone),
                    icon = Icons.Filled.PhoneInTalk,
                    onClick = {
                        uriHandler.openUriWhenValid("tel:$phone")
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
                        uriHandler.openUriWhenValid(url)
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
                            option == OrderHealthCardContactOption.WithHealthCardAndPin &&
                                company.hasMailContentForCardAndPin() -> openMailClient(
                                context = context,
                                address = mail,
                                subject = company.subjectCardAndPinMail!!,
                                body = company.bodyCardAndPinMail!!
                            )

                            option == OrderHealthCardContactOption.PinOnly &&
                                company.hasMailContentForPin() -> openMailClient(
                                context = context,
                                address = mail,
                                subject = company.subjectPinMail!!,
                                body = company.bodyPinMail!!
                            )

                            else -> uriHandler.openUriWhenValid("mailto:$mail?subject=${Uri.encode(mailSubject)}")
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
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(SizeDefaults.one),
        contentColor = AppTheme.colors.primary600,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
        backgroundColor = AppTheme.colors.neutral050,
        elevation = SizeDefaults.zero
    ) {
        Column(Modifier.padding(PaddingDefaults.Medium), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null)
            SpacerSmall()
            Text(
                name,
                modifier = modifier.widthIn(SizeDefaults.ninefold),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.subtitle2
            )
        }
    }
}

@LightDarkPreview
@Composable
fun OrderHealthCardSelectMethodScreenPreview() {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        OrderHealthCardSelectMethodScreenScaffold(
            listState,
            healthInsuranceCompany,
            OrderHealthCardContactOption.NotChosen,
            onBack = {},
            onClose = {}
        )
    }
}
