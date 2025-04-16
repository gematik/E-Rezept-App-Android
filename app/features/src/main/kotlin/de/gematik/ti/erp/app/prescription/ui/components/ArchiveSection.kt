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

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.SpacerLarge

fun LazyListScope.archiveSection(
    isArchiveEmpty: Boolean,
    onClickArchive: () -> Unit
) {
    if (!isArchiveEmpty) {
        item {
            SpacerLarge()
            TextButton(
                onClick = onClickArchive,
                modifier = Modifier.testTag(TestTag.Prescriptions.ArchiveButton)
            ) {
                Text(stringResource(R.string.archived_prescriptions_button))
            }
            SpacerLarge()
        }
    }
}
