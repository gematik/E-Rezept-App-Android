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

package de.gematik.ti.erp.app.messages.mappers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.utils.compose.annotatedLinkUnderlined

@Composable
fun OrderUseCaseData.OrderDetail.getDispenseMessageTitle(): AnnotatedString {
    val fullText = when {
        pharmacy.name.isEmpty() -> stringResource(
            R.string.orders_prescription_sent_to,
            stringResource(R.string.pharmacy_order_pharmacy)
        )

        else -> stringResource(R.string.orders_prescription_sent_to, pharmacy.name)
    }
    val annotatedText = annotatedLinkUnderlined(fullText, pharmacy.name, "PharmacyNameClickable")

    return annotatedText
}

@Composable
fun OrderUseCaseData.OrderDetail.getInfoText(): String {
    return if (taskDetailedBundles.size > 1) {
        stringResource(R.string.all_prescriptions_of_order)
    } else {
        taskDetailedBundles.firstOrNull()?.prescription?.name ?: ""
    }
}

@Composable
fun OrderUseCaseData.OrderDetail.getSentOnTime(): String {
    val formatter = rememberErpTimeFormatter()

    val date = remember(this) { formatter.date(sentOn) }
    val time = remember(this) { formatter.time(sentOn) }

    return stringResource(R.string.orders_timestamp, date, time)
}
