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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.shimmer.LimitedTextShimmer
import de.gematik.ti.erp.app.shimmer.TinyTextShimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    data: UiState<DigaMainScreenUiModel>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)

    ) {
        UiStateMachine(
            state = data,
            onLoading = {
                Column(
                    modifier = Modifier
                        .padding(horizontal = PaddingDefaults.Tiny)
                        .fillMaxWidth()
                        .shimmer(),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
                ) {
                    Image(painter = painterResource(R.drawable.digas_logo_placeholder), contentDescription = null)
                    SpacerSmall()
                    LimitedTextShimmer()
                    TinyTextShimmer()
                }
            },
            onEmpty = {
                ContentHeader()
            },
            onError = {
                ContentHeader()
            },
            onContent = { state ->
                ContentHeader(
                    logo = state.logoUrl,
                    name = state.name,
                    insuredPerson = state.insuredPerson
                )
            }
        )
    }
}

@Composable
private fun ContentHeader(
    logo: String? = null,
    name: String? = null,
    insuredPerson: String? = null
) {
    Column {
        val contentDescriptionLogo = if (logo != null) {
            stringResource(R.string.diga_Logo)
        } else {
            stringResource(R.string.diga_Logo_placeholder)
        }
        Image(
            painter = painterResource(R.drawable.digas_logo_placeholder),
            contentDescription = contentDescriptionLogo
        )
        SpacerSmall()
        Text(
            modifier = Modifier.semanticsHeading(),
            text = name ?: stringResource(R.string.no_value_app_name),
            style = AppTheme.typography.h6
        )
        Text(
            insuredPerson ?: stringResource(R.string.pres_details_no_value),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral600
        )
    }
}

@LightDarkPreview
@Composable
internal fun HeaderSectionPreview() {
    PreviewTheme {
        HeaderSection(
            data = UiState.Empty()
        )
    }
}

@LightDarkPreview
@Composable
internal fun HeaderSectionLoadingPreview() {
    PreviewTheme {
        HeaderSection(
            data = UiState.Loading()
        )
    }
}
