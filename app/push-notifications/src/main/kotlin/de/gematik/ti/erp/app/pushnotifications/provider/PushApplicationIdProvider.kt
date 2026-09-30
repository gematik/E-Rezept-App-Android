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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package de.gematik.ti.erp.app.pushnotifications.provider

import android.content.Context
import com.google.firebase.FirebaseApp
import de.gematik.ti.erp.app.BuildKonfig.BUILD_FLAVOR
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pushnotifications.BuildConfig

fun interface PushApplicationIdProvider {
    fun getPushApplicationId(): String
}

/** Builds the Fachdienst pusher `app_id` from the running Firebase application. */
class FirebasePushApplicationIdProvider(
    private val firebaseAppProvider: () -> FirebaseApp = FirebaseApp::getInstance
) : PushApplicationIdProvider {

    /**
     * Note: The 64-character limit for `app_id` is defined by the Matrix/Element push specification:
     * "app_id: This is a reverse-DNS style identifier for the application. Max length, 64 chars."
     * See: https://docs.element.io/latest/element-support/element-androidios-client-settings/understanding-push-notifications/
     */
    @Requirement(
        "A_27168",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Maximum 64 characters limit " +
            "(https://docs.element.io/latest/element-support/element-androidios-client-settings/understanding-push-notifications/) " +
            ""
    )
    override fun getPushApplicationId(): String {
        val firebaseApp = firebaseAppProvider()
        val context = firebaseApp.applicationContext
        val prefs = context.getSharedPreferences(
            "debug_push_notifications_prefs_sp",
            Context.MODE_PRIVATE
        )
        val failTest = prefs.getBoolean("fail_push_gateway_test", false)

        val packageName = context.packageName

        val isKonnektathon = packageName.contains("konnektathon", ignoreCase = true)
        val isTu = BUILD_FLAVOR.contains(
            "tu",
            ignoreCase = true
        ) || packageName.endsWith(".tu") ||
            packageName.endsWith(".tu.test") ||
            packageName.endsWith(".tu.debug")

        val baseAppId = if (failTest) {
            val projectId = firebaseApp.options.projectId.orEmpty()
            val shortEnv = when {
                isKonnektathon -> "konny"
                isTu -> "tu"
                else -> "pu"
            }
            listOfNotNull(projectId.takeIf(String::isNotBlank), shortEnv)
                .joinToString(separator = ".")
        } else {
            when {
                isKonnektathon -> BuildConfig.RECEIVING_APP_ID_KONNEKTATHON
                isTu -> BuildConfig.RECEIVING_APP_ID_TU
                else -> BuildConfig.RECEIVING_APP_ID_PU
            }
        }

        val platformId = BuildConfig.PLATFORM_IDENTIFIER
        val pushAppId = "$baseAppId.$platformId"

        require(pushAppId.length <= 64) {
            "Push application ID '$pushAppId' is longer than 64 chars (Matrix spec). Requirement A_27168 is violated."
        }

        return pushAppId
    }
}
