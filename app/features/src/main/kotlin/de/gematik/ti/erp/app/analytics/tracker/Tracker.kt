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

package de.gematik.ti.erp.app.analytics.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import de.gematik.ti.erp.app.analytics.mapper.ContentSquareMapper
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import kotlinx.coroutines.flow.first
import org.kodein.di.compose.rememberInstance

class Tracker(
    private val contentSquareTracker: ContentSquareTracker,
    private val demoTracker: DemoTracker
) {

    @NonRestartableComposable
    @Composable
    fun computeScreenTrackingProperty(
        routeEnum: String,
        block: (String) -> Unit
    ) {
        val mapper: ContentSquareMapper by rememberInstance()
        val isAnalyticsAllowed: IsAnalyticsAllowedUseCase by rememberInstance()

        val screenName = mapper.map(NavigationRouteNames.valueOf(routeEnum))

        LaunchedEffect(screenName) {
            if (isAnalyticsAllowed.invoke().first()) {
                screenName?.let { block(it) }
            }
        }
    }

    @NonRestartableComposable
    @Composable
    fun track(
        screenAnalyticsMetric: MutableState<String?>,
        isTrackingPermitted: MutableState<Boolean>
    ) {
        LaunchedEffect(
            screenAnalyticsMetric,
            isTrackingPermitted.value,
            screenAnalyticsMetric.value?.isNotEmpty()
        ) {
            screenAnalyticsMetric.value?.let {
                contentSquareTracker.track(it)
                if (BuildConfigExtension.isNonReleaseMode) {
                    demoTracker.track(it)
                }
            }
        }
    }
}
