/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import de.gematik.ti.erp.app.utils.compose.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.redeem.ui.DataMatrixCode
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.NavigationClose
import de.gematik.ti.erp.app.utils.compose.Spacer32
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8

@Composable
fun DisplayPickupScreen(
    mainNavController: NavController,
    pickupCodeHR: String?,
    pickupCodeDMC: String?,
    viewModel: MessageViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.Unspecified,
                title = {
                    Text(stringResource(R.string.pickup_screen_title))
                },
                navigationIcon = {
                    NavigationClose(onClick = { mainNavController.popBackStack() })
                },
                elevation = 0.dp
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer8()
            Text(
                text = stringResource(id = R.string.pickup_screen_info),
                style = MaterialTheme.typography.subtitle2
            )
            Spacer32()
            pickupCodeHR?.let {
                androidx.compose.material.Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.neutral100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = pickupCodeHR, style = MaterialTheme.typography.h5)
                        Spacer4()
                        Text(
                            text = stringResource(id = R.string.pickup_screen_title),
                            style = MaterialTheme.typography.body1,
                            color = AppTheme.colors.neutral600
                        )
                    }
                }
                Spacer8()
            }
            pickupCodeDMC?.let {
                val code = remember { viewModel.createBitmapMatrix(it) }

                Spacer8()
                DataMatrixCode(
                    code,
                    modifier = Modifier
                        .aspectRatio(1.0f)
                )
                Spacer4()
                Text(text = it, color = AppTheme.colors.neutral600)
            }
        }
    }
}
