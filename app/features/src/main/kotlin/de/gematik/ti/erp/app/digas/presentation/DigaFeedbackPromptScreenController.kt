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

package de.gematik.ti.erp.app.digas.presentation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.analytics.model.TrackedEvent
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntentAsNewTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class DigaFeedbackPromptScreenController(
    private val application: Application,
    private val tracker: Tracker,
    private val digaSurveyAddress: String
) : AndroidViewModel(application) {

    fun onFeedbackAccepted() {
        openDigaSurvey()
        trackEvent()
    }

    private fun trackEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            tracker.trackMetric(TrackedEvent.DigaFeedbackAccepted)
        }
    }

    private fun openDigaSurvey() {
        viewModelScope.launch {
            application.applicationContext
                .handleIntent(provideWebIntentAsNewTask(digaSurveyAddress))
        }
    }
}

@Composable
fun rememberDigaFeedbackPromptScreenController(): DigaFeedbackPromptScreenController {
    val tracker by rememberInstance<Tracker>()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val digaSurveyAddress = stringResource(R.string.diga_settings_feedback_address)

    return remember {
        DigaFeedbackPromptScreenController(
            application = application,
            tracker = tracker,
            digaSurveyAddress = digaSurveyAddress
        )
    }
}
