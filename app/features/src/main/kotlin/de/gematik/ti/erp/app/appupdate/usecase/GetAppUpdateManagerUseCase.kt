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

package de.gematik.ti.erp.app.appupdate.usecase

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import de.gematik.ti.erp.app.appupdate.repository.AppUpdateManagerSelectionRepository
import de.gematik.ti.erp.app.base.BaseActivity

class GetAppUpdateManagerUseCase(
    private val appUpdateManagerSelectionRepository: AppUpdateManagerSelectionRepository
) {
    operator fun invoke(activity: BaseActivity): AppUpdateManager {
        val isOriginal = appUpdateManagerSelectionRepository.getFlag()

        val appUpdateManager = when {
            isOriginal -> AppUpdateManagerFactory.create(activity)
            else -> FakeAppUpdateManager(activity) // not used in production
        }
        return appUpdateManager
    }
}
