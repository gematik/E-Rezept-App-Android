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

package de.gematik.ti.erp.app.translation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun LanguagePickerList(
    listState: LazyListState,
    languages: List<Pair<String, String>>,
    selectedLanguageCode: String?,
    onLanguageSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        items(languages) { (code, name) ->
            val isSelected = code == selectedLanguageCode
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(code) }
                    .padding(horizontal = SizeDefaults.double, vertical = SizeDefaults.oneHalf),
                style = if (isSelected) {
                    MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colors.secondary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    MaterialTheme.typography.body1
                }
            )
        }
    }
}
