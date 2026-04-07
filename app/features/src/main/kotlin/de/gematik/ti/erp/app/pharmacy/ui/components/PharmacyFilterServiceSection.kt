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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

enum class PharmacyFilterServiceOption(
    val code: String,
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    AllergyTest(
        "allergietest",
        R.string.search_pharmacies_service_allergy_test,
        R.string.search_pharmacies_service_allergy_test_desc
    ), // Allergietest erwerben
    OrganTransplant(
        "100",
        R.string.search_pharmacies_service_organ_transplant,
        R.string.search_pharmacies_service_organ_transplant_desc
    ), // Beratung bei Organtransplantation
    Polymedication(
        "80",
        R.string.search_pharmacies_service_polymedication,
        R.string.search_pharmacies_service_polymedication_desc
    ), // Beratung bei Polymedikation
    CancerTherapy(
        "90",
        R.string.search_pharmacies_service_cancer_therapy,
        R.string.search_pharmacies_service_cancer_therapy_desc
    ), // Betreuung oraler Krebstherapie
    BloodPressure(
        "60",
        R.string.search_pharmacies_service_blood_pressure,
        R.string.search_pharmacies_service_blood_pressure_desc
    ), // Bluthochdruck kontrollieren
    Vaccination(
        "impfung",
        R.string.search_pharmacies_service_vaccination,
        R.string.search_pharmacies_service_vaccination_desc
    ), // Impfen lassen
    Inhalation(
        "70",
        R.string.search_pharmacies_service_inhalation,
        R.string.search_pharmacies_service_inhalation_desc
    ), // Inhalationsschulung
    BodyValues(
        "koerperwerte",
        R.string.search_pharmacies_service_body_values,
        R.string.search_pharmacies_service_body_values_desc
    ), // Körperwerte messen
    TravelMedicine(
        "reisemedizin-beratung",
        R.string.search_pharmacies_service_travel_medicine,
        R.string.search_pharmacies_service_travel_medicine_desc
    ), // Reisemedizinberatung
    Sterile(
        "50",
        R.string.search_pharmacies_service_sterile,
        R.string.search_pharmacies_service_sterile_desc
    ); // Sterilherstellung
}

private val serviceItems = PharmacyFilterServiceOption.entries

enum class PharmacyOnSiteFeatureOption(
    val code: String,
    @StringRes val label: Int
) {
    PickupStation("abholautomat", R.string.search_pharmacies_filter_pickup_station), // Abholautomat
    AccessibleEntry("barrierefrei", R.string.search_pharmacies_filter_accessible_entry), // Barrierefreier Zugang
    PublicTransport("oepnv", R.string.search_pharmacies_filter_public_transport_nearby), // ÖPNV in der Nähe
    Parking("parkmoeglichkeit", R.string.search_pharmacies_filter_parking_option); // Parkmöglichkeit
}

@Composable
internal fun PharmacyFilterServiceSection(
    showDescriptions: Boolean,
    selectedServiceCodes: Set<String>,
    onToggleDescriptions: () -> Unit,
    onToggleServiceOption: (PharmacyFilterServiceOption) -> Unit
) {
    ServiceSectionHeader(
        showDescriptions = showDescriptions,
        onToggleDescriptions = onToggleDescriptions
    )

    serviceItems.forEach { item ->
        ServiceListItem(
            title = item.title,
            description = item.description,
            selected = item.code in selectedServiceCodes,
            showDescription = showDescriptions,
            onClick = { onToggleServiceOption(item) }
        )
    }

    ServiceHints()
}

@Composable
private fun ServiceSectionHeader(
    modifier: Modifier = Modifier,
    showDescriptions: Boolean,
    onToggleDescriptions: () -> Unit
) {
    val toggleIcon = if (showDescriptions) Icons.Default.VisibilityOff else Icons.Default.RemoveRedEye
    val toggleText = stringResource(
        if (showDescriptions) R.string.search_pharmacies_not_explain else R.string.search_pharmacies_explain
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.search_pharmacies_section_services),
            style = AppTheme.typography.h6,
            color = AppTheme.colors.neutral900,
            modifier = Modifier.semanticsHeading()
        )
        TextButton(
            onClick = onToggleDescriptions
        ) {
            Icon(
                imageVector = toggleIcon,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.doubleHalf)
            )
            Spacer(modifier = Modifier.size(SizeDefaults.half))
            Text(
                text = toggleText,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary700
            )
        }
    }
}

@Composable
private fun ServiceHints(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = PaddingDefaults.Tiny),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.quarter)
    ) {
        Text(
            text = stringResource(R.string.search_pharmacies_service_hint_statutory),
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral900,
            textAlign = TextAlign.End
        )
        Text(
            text = stringResource(R.string.search_pharmacies_service_hint_all),
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral900,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ServiceListItem(
    modifier: Modifier = Modifier,
    title: Int,
    description: Int,
    selected: Boolean,
    showDescription: Boolean,
    onClick: () -> Unit
) {
    val leadingIcon = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val leadingIconTint = if (selected) AppTheme.colors.primary700 else AppTheme.colors.neutral600
    val titleText = stringResource(title)
    val accessibleTitle = titleText.withAccessibleAsteriskSuffix(
        singleAsteriskDescription = stringResource(R.string.search_pharmacies_service_single_asterisk_a11y),
        doubleAsteriskDescription = stringResource(R.string.search_pharmacies_service_double_asterisk_a11y)
    )

    ListItem(
        colors = GemListItemDefaults.gemListItemColors(
            containerColor = AppTheme.colors.neutral000,
            headlineColor = AppTheme.colors.neutral900,
            supportingColor = AppTheme.colors.neutral600,
            leadingIconColor = leadingIconTint
        ),
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                onValueChange = { onClick() },
                role = Role.Checkbox
            ),
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = leadingIconTint,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        },
        headlineContent = {
            Text(
                text = titleText,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.neutral900,
                modifier = Modifier.semantics { contentDescription = accessibleTitle }
            )
        },
        supportingContent = {
            AnimatedVisibility(
                visible = showDescription,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    SpacerTiny()
                    Text(
                        text = stringResource(description),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.neutral600
                    )
                }
            }
        }
    )
}

private fun String.withAccessibleAsteriskSuffix(
    singleAsteriskDescription: String,
    doubleAsteriskDescription: String
): String = when {
    endsWith("**") -> removeSuffix("**").trimEnd() + " $doubleAsteriskDescription"
    endsWith("*") -> removeSuffix("*").trimEnd() + " $singleAsteriskDescription"
    else -> this
}

@LightDarkPreview
@Composable
private fun PharmacyFilterServiceSectionPreview() {
    PreviewAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium)
        ) {
            PharmacyFilterServiceSection(
                showDescriptions = false,
                selectedServiceCodes = setOf("allergietest", "70"),
                onToggleDescriptions = {},
                onToggleServiceOption = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PharmacyFilterServiceSectionWithDescriptionsPreview() {
    PreviewAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium)
        ) {
            PharmacyFilterServiceSection(
                showDescriptions = true,
                selectedServiceCodes = setOf("impfung"),
                onToggleDescriptions = {},
                onToggleServiceOption = {}
            )
        }
    }
}
