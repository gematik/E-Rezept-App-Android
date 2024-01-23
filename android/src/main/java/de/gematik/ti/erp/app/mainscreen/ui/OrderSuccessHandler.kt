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

package de.gematik.ti.erp.app.mainscreen.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.utils.compose.AcceptDialog

@Composable
fun OrderSuccessHandler(
    mainScreenController: MainScreenController
) {
    val context = LocalContext.current

    when (mainScreenController.orderedEvent) {
        MainScreenController.OrderedEvent.Success -> {
            LaunchedEffect(Unit) {
                requestReview(context)
                mainScreenController.resetOrderedEvent()
            }
        }

        MainScreenController.OrderedEvent.Error -> {
            AcceptDialog(
                header = stringResource(R.string.pharmacy_order_not_possible_title),
                info = stringResource(R.string.pharmacy_order_not_possible_desc),
                acceptText = stringResource(R.string.ok),
                onClickAccept = {
                    mainScreenController.resetOrderedEvent()
                }
            )
        }

        null -> {
            // noop
        }
    }
}

private fun requestReview(context: Context) {
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            manager.launchReviewFlow(context as Activity, reviewInfo)
        }
    }
}
