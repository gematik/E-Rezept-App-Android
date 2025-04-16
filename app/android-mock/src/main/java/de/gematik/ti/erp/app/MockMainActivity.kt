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

package de.gematik.ti.erp.app

import android.content.Intent
import android.os.Build
import de.gematik.ti.erp.app.appupdate.usecase.CheckVersionUseCase
import de.gematik.ti.erp.app.config.TestConfigKey
import de.gematik.ti.erp.app.config.TestScenario
import de.gematik.ti.erp.app.config.UiTestsIntent
import de.gematik.ti.erp.app.demomode.di.demoModeModule
import de.gematik.ti.erp.app.demomode.di.demoModeOverrides
import de.gematik.ti.erp.app.di.overrides.testScenarioOverrides
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.mocks.settings.OnboardingDoneMockSettingsDataSource
import org.kodein.di.Copy
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.android.retainedSubDI
import org.kodein.di.bindProvider
import org.kodein.di.instance

class MockMainActivity : MainActivity() {

    private var diBuilder: DI.MainBuilder? = null

    override val di by retainedSubDI(closestDI(), copy = Copy.All) {

        fullDescriptionOnError = true
        fullContainerTreeOnError = true
        diBuilder = this

        // add testScenarioOverrides to the DI
        intent?.scenarios(diBuilder)

        import(demoModeModule)
        if (isDemoMode()) demoModeOverrides()
        if (BuildConfig.DEBUG && BuildKonfig.INTERNAL) debugOverrides()
        bindProvider { CheckVersionUseCase(instance()) }

        // domain verification is only available on SDK 31 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bindProvider<DomainVerifier> { Sdk31DomainVerifier(instance()) }
        } else {
            bindProvider<DomainVerifier> { OlderSdkDomainVerifier() }
        }
    }
}

private fun Intent.scenarios(diMainBuilder: DI.MainBuilder?) {
    when (this.action.equals(UiTestsIntent)) {
        // can be changed when minSDK = 33
        true -> this.getParcelableExtra<TestScenario>(TestConfigKey)?.let { testScenario ->
            diMainBuilder?.testScenarioOverrides(testScenario)
        }

        else -> OnboardingDoneMockSettingsDataSource()
    }
}
