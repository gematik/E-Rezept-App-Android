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

package de.gematik.ti.erp.app.pharmacy.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme

data class PharmacyPortalText(
    @StringRes private val portalTextId: Int = R.string.pharmacy_detail_data_info_domain,
    @StringRes private val portalUriId: Int = R.string.pharmacy_detail_pharmacy_portal_uri,
    @StringRes private val infoTextId: Int = R.string.pharmacy_detail_data_info
) {
    private val portalText: String
        @Composable
        get() = stringResource(portalTextId)

    private val infoText: String
        @Composable
        get() = stringResource(infoTextId)

    private val portalUri: String
        @Composable
        get() = stringResource(portalUriId)

    private val start
        @Composable
        get() = infoText.indexOf(portalText)

    private val end
        @Composable
        get() = start + portalText.length

    @Composable
    fun urlText() = with(AnnotatedString.Builder()) {
        append(infoText)
        addStringAnnotation(
            tag = "URL",
            annotation = portalUri,
            start = start,
            end = end
        )
        addStyle(
            SpanStyle(color = AppTheme.colors.primary600),
            start,
            end
        )
        toAnnotatedString()
    }
}
