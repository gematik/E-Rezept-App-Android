/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Spacer32
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

@Composable
fun DPDifferences30112021() {
    DPSection(title = stringResource(R.string.data_terms_first_update_header)) {
        Text(
            stringResource(R.string.data_terms_first_update_text),
            modifier = Modifier.fillMaxWidth(),
            style = AppTheme.typography.body2l
        )
    }
    Spacer32()
    DPSection(title = stringResource(R.string.data_terms_second_update_header)) {
        val uriHandler = LocalUriHandler.current

        val policiesLink = annotatedLinkStringLight(
            uri = stringResource(R.string.google_policies_link),
            text = stringResource(R.string.google_policies_link)
        )
        val supportLink = annotatedLinkStringLight(
            uri = stringResource(R.string.google_support_link),
            text = stringResource(R.string.google_support_link)
        )

        val text = annotatedStringResource(
            R.string.data_terms_second_update_text,
            policiesLink,
            supportLink
        )
        ClickableText(
            text = text,
            style = AppTheme.typography.body2l,
            onClick = {
                text
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            },
            modifier = Modifier
                .padding(end = PaddingDefaults.Medium)
        )
    }
}

@Composable
fun DPSection(title: String, content: @Composable () -> Unit) {
    var sectionExpanded by remember { mutableStateOf(false) }
    val arrow = if (sectionExpanded) {
        Icons.Rounded.ArrowDropUp
    } else {
        Icons.Rounded.ArrowDropDown
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { sectionExpanded = !sectionExpanded })
                .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.body1
            )
            Icon(
                imageVector = arrow,
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                tint = AppTheme.colors.primary600
            )
        }
        Spacer8()
        AnimatedVisibility(
            visible = sectionExpanded,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Tiny,
                bottom = PaddingDefaults.Medium,
                end = 48.dp
            )
        ) {
            content()
        }
    }
}
