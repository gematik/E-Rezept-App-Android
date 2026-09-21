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

package de.gematik.ti.erp.app.migration.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.android.material.progressindicator.CircularProgressIndicator
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.database.migration.MigrationProgress
import de.gematik.ti.erp.app.onboarding.ui.ErezeptLogo
import de.gematik.ti.erp.app.onboarding.ui.FlaggedGematikLogo
import de.gematik.ti.erp.app.onboarding.ui.OnboardingImages
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun DataMigrationLoadingContent(
    innerPadding: PaddingValues,
    progress: MigrationProgress?,
    scrollState: ScrollState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .scrollable(scrollState, Orientation.Vertical)
    ) {
        FlaggedGematikLogo()
        OnboardingImages(modifier = Modifier.fillMaxSize())
        MigrationProgressSection(
            progress = progress
        )
    }
}

@Composable
private fun MigrationProgressSection(
    progress: MigrationProgress?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics(
                mergeDescendants = true,
                properties = {}
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ErezeptLogo(
            modifier = Modifier
                .padding(top = PaddingDefaults.Large)
        )
        Text(
            text = stringResource(R.string.data_migration_header),
            style = AppTheme.typography.h4,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Small
                )
        )
        Text(
            text = stringResource(R.string.data_migration_body),
            style = AppTheme.typography.subtitle1l,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(
                    bottom = PaddingDefaults.XLarge
                )
        )
        CircularProgressIndicator(
            progress = progress?.let {
                    it ->
                it.groupsDone.toFloat() / it.groupsTotal.toFloat()
            } ?: 0f,
            modifier = Modifier
                .padding(bottom = PaddingDefaults.XLarge)
        )
    }
}
