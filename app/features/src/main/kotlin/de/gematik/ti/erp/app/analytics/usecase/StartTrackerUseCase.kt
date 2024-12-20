/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.analytics.usecase

import android.content.Context
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Requirement(
    "A_19096-01#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "On every app session the old session is forcefully forgotten before we start a new one."
)
class StartTrackerUseCase(val context: Context) {
    @Requirement(
        "A_19090-01#1",
        "A_19089-01#1",
        "A_19091-01#1",
        "A_19092-01#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Resets the analytics id on each new session and creates a new one."
    )
    suspend operator fun invoke() = withContext(Dispatchers.Main) {
        try {
            Contentsquare.start(context)
            Contentsquare.optOut(context) // reset the analytics id on each new session
            Contentsquare.optIn(context) // create a new one
        } catch (e: Exception) {
            Napier.e { "Error trying to start tracker ${e.stackTraceToString()}" }
            // ignore
        }
    }
}
