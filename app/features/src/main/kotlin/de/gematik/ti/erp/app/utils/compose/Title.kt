/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
internal fun Title(
    text: String
) {
    Text(
        text = text,
        style = AppTheme.typography.h6
    )
}

@LightDarkPreview
@Composable
internal fun TitlePreview() {
    AppTheme {
        Title(text = "Title text")
    }
}
