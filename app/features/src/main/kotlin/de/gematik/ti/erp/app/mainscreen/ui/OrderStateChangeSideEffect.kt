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

package de.gematik.ti.erp.app.mainscreen.ui

import android.app.Activity
import android.content.Context
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import com.google.android.play.core.review.ReviewManagerFactory
import de.gematik.ti.erp.app.mainscreen.presentation.AppController
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionNaming")
fun OrderStateChangeOnSuccessSideEffect(
    context: Context,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    orderedEvent: AppController.OrderedEvent?,
    resetOrdered: () -> Unit
) {
    if (orderedEvent == AppController.OrderedEvent.Success) {
        scope.requestReview(context) {
            it.launch {
                val result = snackbar.showSnackbar(
                    message = "Review is requested here in release mode",
                    actionLabel = "OK"
                )
                when (result) {
                    SnackbarResult.Dismissed -> snackbar.currentSnackbarData?.dismiss()
                    SnackbarResult.ActionPerformed -> snackbar.currentSnackbarData?.dismiss()
                }
            }
        }
        resetOrdered()
    }
}

private fun CoroutineScope.requestReview(
    context: Context,
    inDebugMode: (CoroutineScope) -> Unit
) {
    try {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(context as Activity, reviewInfo)
            }
        }
    } catch (e: Throwable) {
        Napier.e { "error on request review ${e.stackTraceToString()}" }
    }
    if (BuildConfigExtension.isDebug) {
        inDebugMode(this)
    }
}
