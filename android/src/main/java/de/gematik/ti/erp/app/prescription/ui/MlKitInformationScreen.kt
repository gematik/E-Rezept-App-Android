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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent

@Composable
fun MlKitInformationScreen(
    navController: NavController
) {
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        modifier = Modifier
            .systemBarsPadding(),
        topBarTitle = stringResource(R.string.ml_information_title),
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = {
            navController.popBackStack()
        }
    ) {
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingDefaults.Medium),
            state = listState,
            contentPadding = it
        ) {
            item {
                Text(
                    text = stringResource(R.string.ml_info_header),
                    style = AppTheme.typography.h5
                )
                SpacerXXLarge()
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_1_header),
                    info = stringResource(R.string.ml_pararaph_1_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_2_header),
                    info = stringResource(R.string.ml_pararaph_2_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_3_header),
                    info = stringResource(R.string.ml_pararaph_3_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_4_header),
                    info = stringResource(R.string.ml_pararaph_4_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_5_header),
                    info = stringResource(R.string.ml_pararaph_5_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_6_header),
                    info = stringResource(R.string.ml_pararaph_6_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_7_header),
                    info = stringResource(R.string.ml_pararaph_7_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_8_header),
                    info = stringResource(R.string.ml_pararaph_8_info)
                )
            }
            item {
                Paragraph(
                    header = stringResource(R.string.ml_pararaph_9_header),
                    info = stringResource(R.string.ml_pararaph_9_info)
                )
            }
            item {
                val phoneNumber = stringResource(R.string.settings_contact_hotline_number)

                Text(
                    text = stringResource(R.string.ml_pararaph_10_header),
                    style = AppTheme.typography.body1
                )
                SpacerMedium()
                Row(
                    modifier = Modifier.clickable {
                        context.handleIntent(providePhoneIntent(phoneNumber))
                    }
                ) {
                    Icon(Icons.Rounded.Phone, null, tint = AppTheme.colors.primary600)
                    SpacerMedium()
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.settings_contact_hotline),
                        style = AppTheme.typography.body1
                    )
                }
                SpacerMedium()
                Text(
                    text = stringResource(R.string.ml_pararaph_10_info),
                    style = AppTheme.typography.body2l
                )
            }
        }
    }
}

@Composable
fun Paragraph(header: String, info: String) {
    Text(
        text = header,
        style = AppTheme.typography.body1
    )
    SpacerSmall()
    Text(
        text = info,
        style = AppTheme.typography.body2l
    )
    SpacerXXLarge()
}
