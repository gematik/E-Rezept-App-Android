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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.statusBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.settings.usecase.DATA_PROTECTION_LAST_UPDATED
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer48
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DataTermsUpdateScreen(
    dataProtectionVersionAcceptedOn: Instant,
    onClickDataTerms: () -> Unit,
    onAcceptTermsOfUseUpdate: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        onAcceptTermsOfUseUpdate()
                    },
                    shape = RoundedCornerShape(PaddingDefaults.Small),
                    modifier = Modifier.padding(end = PaddingDefaults.Small)
                ) {
                    Text(stringResource(id = R.string.data_terms_accept_update))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
            ) {
                Text(
                    stringResource(R.string.data_terms_update_header),
                    style = AppTheme.typography.h5,
                    textAlign = TextAlign.Center
                )

                Spacer24()
                Text(
                    stringResource(R.string.data_terms_update_info),
                    style = AppTheme.typography.body1
                )

                Spacer8()
                TextButton(
                    onClick = { onClickDataTerms() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        stringResource(R.string.data_terms_update_open_data_terms),
                        style = AppTheme.typography.caption1
                    )
                }

                val dtFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
                val date = remember(dataProtectionVersionAcceptedOn) {
                    OffsetDateTime.ofInstant(dataProtectionVersionAcceptedOn, ZoneId.systemDefault())
                        .toLocalDate()
                        .format(dtFormatter)
                }

                val updateInfo = annotatedStringResource(
                    R.string.data_terms_update_updates,
                    date
                ).toString()
                Spacer48()
                Text(
                    updateInfo,
                    style = AppTheme.typography.subtitle1
                )
                Spacer16()
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                if (dataProtectionVersionAcceptedOn < DATA_PROTECTION_LAST_UPDATED) {
                    DPDifferences30112021()
                }
            }
        }
    }
}
