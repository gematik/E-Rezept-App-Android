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

package de.gematik.ti.erp.app.digas.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@Composable
fun InsuranceListItem(
    insuranceUiModel: de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel,
    onSelectInsurance: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onSelectInsurance(insuranceUiModel.pharmacy) })
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
            .testTag(TestTag.Settings.InsuranceCompanyList.ListOfInsuranceButtons),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(SizeDefaults.sixfoldAndQuarter)) {
            Image(
                painter = painterResource(insuranceUiModel.drawableResourceId),
                contentDescription = null,
                modifier = Modifier
                    .widthIn(max = SizeDefaults.sixfoldAndQuarter)
                    .heightIn(max = SizeDefaults.fivefold)
            )
        }

        SpacerMedium()

        Text(
            text = insuranceUiModel.name,
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral900
        )
    }
}
