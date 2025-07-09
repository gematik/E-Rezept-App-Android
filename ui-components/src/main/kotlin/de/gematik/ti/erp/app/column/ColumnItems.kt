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

package de.gematik.ti.erp.app.column

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

/**
 * [items] refers to the list of items to be displayed
 * [itemContent] is for placing the content of the item
 * [lastItemExtraContent] is for placing the extra content below the last item
 */
@Composable
fun <T> ColumnItems(
    items: List<T>,
    itemContent: @Composable ColumnScope.(Int, T) -> Unit,
    lastItemExtraContent: @Composable ColumnScope.(Int, T) -> Unit
) {
    Column {
        items.forEachIndexed { index, item ->
            if (index == items.size - 1) {
                itemContent(index, item)
                lastItemExtraContent(index, item)
            } else {
                itemContent(index, item)
            }
        }
    }
}
