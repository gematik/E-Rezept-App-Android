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

package de.gematik.ti.erp.app.appupdate.usecase

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import de.gematik.ti.erp.app.base.BaseActivity
import io.github.aakira.napier.Napier

class AppUpdateInfoUseCase {
    operator fun invoke(
        activity: BaseActivity,
        appUpdateManager: AppUpdateManager,
        onTaskSuccessful: () -> Unit,
        onTaskFailed: () -> Unit
    ) {
        try {
            if (appUpdateManager is FakeAppUpdateManager) {
                onTaskFailed()
            } else {
                val appUpdateInfoTask = appUpdateManager.appUpdateInfo

                appUpdateInfoTask.addOnFailureListener {
                    Napier.e { "app update failed ${it.stackTraceToString()}" }
                    onTaskFailed()
                }

                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        val task = appUpdateManager.startUpdateFlow(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                        )

                        task.addOnFailureListener {
                            Napier.e { "app update failed ${it.stackTraceToString()}" }
                            onTaskFailed()
                        }

                        task.addOnCompleteListener {
                            when {
                                task.isSuccessful && task.result == Activity.RESULT_OK -> onTaskSuccessful()
                                task.isSuccessful && task.result != Activity.RESULT_OK -> activity.finish()
                                else -> {
                                    Napier.e { "app update failed ${task.result}" }
                                    onTaskFailed()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Napier.e { "app update failed ${e.stackTraceToString()}" }
            onTaskFailed()
        }
    }
}
