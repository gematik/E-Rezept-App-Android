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

package de.gematik.ti.erp.app.components

import android.content.Context
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.gematik.ti.erp.app.MockMainActivity
import de.gematik.ti.erp.app.config.TestScenario
import de.gematik.ti.erp.app.config.injectionConfig

/**
 * Every scenario is injected as an intent to start the mock app for that test
 */

fun AndroidComposeTestRule<ActivityScenarioRule<MockMainActivity>, MockMainActivity>.injectConfig(
    applicationContext: Context,
    config: TestScenario
): ActivityScenario<MockMainActivity> = activityRule.scenario.onActivity {
    it.injectionConfig(applicationContext, config)
}
