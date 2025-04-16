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

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProductsImprovementsSettingsController(
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val scope: CoroutineScope
) {

    private val analyticsFlow by lazy {
        isAnalyticsAllowedUseCase().map { SettingStatesData.AnalyticsState(it) }
    }

    val analyticsState
        @Composable
        get() = analyticsFlow.collectAsStateWithLifecycle(SettingStatesData.defaultAnalyticsState)

    @Requirement(
        "O.Purp_5#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Enable usage analytics."
    )
    fun changeAnalyticsAllowedState(boolean: Boolean) {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(boolean)
        }
    }
}

@Composable
fun rememberProductsImprovementsSettingsController(): ProductsImprovementsSettingsController {
    val isAnalyticsAllowedUseCase by rememberInstance<IsAnalyticsAllowedUseCase>()
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        ProductsImprovementsSettingsController(
            isAnalyticsAllowedUseCase = isAnalyticsAllowedUseCase,
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            scope = scope
        )
    }
}
