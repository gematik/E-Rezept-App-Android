/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyImagePlaceholder
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningHours
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.LocalPharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.OnlinePharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService.PickUpPharmacyService
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat

private const val OneKilometerInMeter = 1000

internal fun formattedDistance(distanceInMeters: Double): String {
    val f = DecimalFormat()
    return if (distanceInMeters < OneKilometerInMeter) {
        f.maximumFractionDigits = 0
        f.format(distanceInMeters).toString() + " m"
    } else {
        f.maximumFractionDigits = 1
        f.format(distanceInMeters / OneKilometerInMeter).toString() + " km"
    }
}

@Composable
internal fun PharmacyResultCard(
    modifier: Modifier,
    pharmacy: Pharmacy,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val distanceTxt = pharmacy.distance?.let { distance ->
            formattedDistance(distance)
        }

        PharmacyImagePlaceholder(Modifier)
        SpacerMedium()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pharmacy.name,
                color = AppTheme.colors.neutral999,
                style = AppTheme.typography.subtitle1
            )

            Text(
                text = pharmacy.singleLineAddress(),
                color = AppTheme.colors.neutral600,
                style = AppTheme.typography.body2l,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            val pharmacyLocalServices =
                pharmacy.provides.find { it is LocalPharmacyService } as? LocalPharmacyService
            val now =
                remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

            if (pharmacyLocalServices?.isOpenAt(now) == true) {
                val text = if (pharmacyLocalServices.isAllDayOpen(now.dayOfWeek)) {
                    stringResource(R.string.search_pharmacy_continuous_open)
                } else {
                    stringResource(
                        R.string.search_pharmacy_open_until,
                        requireNotNull(pharmacyLocalServices.openUntil(now)).toString()
                    )
                }
                Text(
                    text,
                    style = AppTheme.typography.subtitle2l,
                    color = AppTheme.colors.green600
                )
            } else {
                val text =
                    pharmacyLocalServices?.opensAt(now)?.let {
                        stringResource(
                            R.string.search_pharmacy_opens_at,
                            it.toString()
                        )
                    }
                if (text != null) {
                    Text(
                        text,
                        style = AppTheme.typography.subtitle2l,
                        color = AppTheme.colors.yellow600
                    )
                }
            }
        }

        SpacerMedium()

        if (distanceTxt != null) {
            Text(
                distanceTxt,
                style = AppTheme.typography.body2l,
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.End
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            null,
            tint = AppTheme.colors.neutral400,
            modifier = Modifier
                .size(SizeDefaults.triple)
                .align(Alignment.CenterVertically)
        )
    }
}

@LightDarkPreview
@Composable
internal fun PharmacyResultCardPreview() {
    AppTheme {
        PharmacyResultCard(
            modifier = Modifier,
            pharmacy = Pharmacy(
                id = "pharmacy-id",
                name = "2Königen-Aptheke",
                address = "Ostwall 97, 47798 Krefeld",
                coordinates = null,
                distance = null,
                contact = PharmacyUseCaseData.PharmacyContact(
                    phone = "12345678",
                    mail = "pharmacy@mail.com",
                    url = "https://pharmacy.com",
                    pickUpUrl = "https://pharmacy.pickup.com/code123",
                    deliveryUrl = "https://pharmacy.delivery.com/code123",
                    onlineServiceUrl = "https://pharmacy.online.com/code123"
                ),
                provides = listOf(
                    DeliveryPharmacyService(
                        name = "delivery-service",
                        openingHours = OpeningHours(openingTime = mapOf())
                    ),
                    OnlinePharmacyService(name = "online-service"),
                    PickUpPharmacyService(name = "pickup-service"),
                    LocalPharmacyService(
                        name = "local-service",
                        openingHours = OpeningHours(openingTime = mapOf())
                    )
                ),
                openingHours = OpeningHours(openingTime = mapOf()),
                telematikId = "telematikId"
            ),
            onClick = {}
        )
    }
}
