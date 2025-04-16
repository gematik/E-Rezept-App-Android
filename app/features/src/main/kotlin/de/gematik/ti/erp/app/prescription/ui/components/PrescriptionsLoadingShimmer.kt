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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.CircularShapeShimmer
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LimitedTextShimmer
import de.gematik.ti.erp.app.utils.compose.RowTextShimmer
import de.gematik.ti.erp.app.utils.compose.SquareShapeShimmer
import de.gematik.ti.erp.app.utils.compose.StatusChipShimmer
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

// TODO: To be used when the user does pull to refresh
@Composable
internal fun PrescriptionScreenShimmer() {
    Column {
        SpacerSmall()
        CircularShapeShimmer(
            modifier = Modifier.padding(start = PaddingDefaults.Medium)
        )
        SpacerSmall()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
        PrescriptionCardShimmer()
    }
}

@Composable
private fun PrescriptionCardShimmer() {
    Card(
        modifier = Modifier
            .padding(bottom = PaddingDefaults.Medium)
            .padding(horizontal = PaddingDefaults.Medium),
        shape = RoundedCornerShape(SizeDefaults.double),
        border = BorderStroke(
            SizeDefaults.eighth,
            color = AppTheme.colors.neutral300
        ),
        elevation = SizeDefaults.zero,
        backgroundColor = AppTheme.colors.neutral050
    ) {
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                LimitedTextShimmer()
                SpacerTiny()
                RowTextShimmer(
                    modifier = Modifier.padding(end = PaddingDefaults.Medium)
                )
                SpacerSmall()

                StatusChipShimmer()
            }

            SquareShapeShimmer(
                modifier = Modifier.align(Alignment.CenterVertically),
                size = SizeDefaults.double
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionScreenShimmerPreview() {
    PreviewAppTheme {
        PrescriptionScreenShimmer()
    }
}
