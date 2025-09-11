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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Banner
import de.gematik.ti.erp.app.utils.compose.BannerClickableIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource

@Composable
fun SelfPayerPrescriptionWarning(
    modifier: Modifier = Modifier,
    selfPayerPrescriptionNames: List<String?>,
    showSingleCodes: Boolean,
    nrOfDMCodes: Int
) {
    if (selfPayerPrescriptionNames.isNotEmpty()) {
        val text = if (showSingleCodes && selfPayerPrescriptionNames.size == 1 ||
            nrOfDMCodes == 1
        ) {
            stringResource(
                R.string.local_redeem_self_payer_prescription_warning
            )
        } else {
            annotatedPluralsResource(
                R.plurals.pharmacy_order_self_payer_prescriptions,
                selfPayerPrescriptionNames.size,
                AnnotatedString(
                    selfPayerPrescriptionNames.joinToString(" & ")
                )
            )
        }
        Banner(
            modifier = Modifier
                .padding(PaddingDefaults.Medium)
                .then(modifier),
            startIcon = BannerClickableIcon(
                BannerIcon.Custom(
                    vector = Icons.Rounded.WarningAmber,
                    color = AppTheme.colors.yellow900
                ),
                onClick = {}
            ),
            contentColor = AppTheme.colors.yellow900,
            containerColor = AppTheme.colors.yellow100,
            borderColor = AppTheme.colors.yellow900,
            text = text.toString()
        )
    }
}

@Composable
fun SelfPayerPrescriptionWarning(
    selfPayerPrescriptionNames: List<String>
) {
    Banner(
        modifier = Modifier,
        startIcon = BannerClickableIcon(
            BannerIcon.Custom(
                vector = Icons.Rounded.WarningAmber,
                color = AppTheme.colors.yellow900
            ),
            onClick = {}
        ),
        contentColor = AppTheme.colors.yellow900,
        containerColor = AppTheme.colors.yellow100,
        borderColor = AppTheme.colors.yellow900,
        text = annotatedPluralsResource(
            R.plurals.pharmacy_order_self_payer_prescriptions,
            selfPayerPrescriptionNames.size,
            AnnotatedString(selfPayerPrescriptionNames.joinToString(" & "))
        ).text
    )
}

@LightDarkPreview
@Composable
fun SelfPayerPrescriptionWarningPreview() {
    SelfPayerPrescriptionWarning(
        selfPayerPrescriptionNames = listOf("Medication"),
        nrOfDMCodes = 2,
        showSingleCodes = false
    )
}

@LightDarkPreview
@Composable
fun SelfPayerPrescriptionWarningSingleCodesPreview() {
    SelfPayerPrescriptionWarning(
        selfPayerPrescriptionNames = listOf("Medication"),
        nrOfDMCodes = 1,
        showSingleCodes = true
    )
}

@LightDarkPreview
@Composable
fun SelfPayerPrescriptionWarningListPreview() {
    SelfPayerPrescriptionWarning(selfPayerPrescriptionNames = listOf("Medication"))
}
