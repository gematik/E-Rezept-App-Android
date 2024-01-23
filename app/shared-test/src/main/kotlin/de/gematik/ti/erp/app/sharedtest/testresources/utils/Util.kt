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

package de.gematik.ti.erp.app.sharedtest.testresources.utils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.test.platform.app.InstrumentationRegistry
import de.gematik.ti.erp.app.InsuranceState
import de.gematik.ti.erp.app.MedicationCategory
import de.gematik.ti.erp.app.PharmacyId
import de.gematik.ti.erp.app.PrescriptionId
import de.gematik.ti.erp.app.SubstitutionAllowed
import de.gematik.ti.erp.app.SupplyForm
import de.gematik.ti.erp.app.sharedtest.testresources.config.TestConfig.WaitTimeout1MilliSec

fun ComposeTestRule.awaitDisplay(timeout: Long, vararg tags: String): String {
    val t0 = System.currentTimeMillis()
    do {
        tags.forEach { tag ->
            try {
                onNodeWithTag(tag).assertIsDisplayed()
                return tag
            } catch (_: AssertionError) {
            }
        }
        mainClock.advanceTimeBy(WaitTimeout1MilliSec)
        Thread.sleep(WaitTimeout1MilliSec)
    } while (System.currentTimeMillis() - t0 < timeout)
    throw AssertionError(
        "Node was not displayed after $timeout milliseconds. Root node was:\n${
        onRoot().printToString(Int.MAX_VALUE)
        }"
    )
}

fun ComposeTestRule.awaitDisplay(timeout: Long, node: () -> SemanticsNodeInteraction) {
    val t0 = System.currentTimeMillis()
    do {
        try {
            node().assertIsDisplayed()
            return
        } catch (_: AssertionError) {
        }
        mainClock.advanceTimeBy(WaitTimeout1MilliSec)
        Thread.sleep(WaitTimeout1MilliSec)
    } while (System.currentTimeMillis() - t0 < timeout)
    throw AssertionError(
        "Node was not displayed after $timeout milliseconds. Root node was:\n${
        onRoot().printToString(Int.MAX_VALUE)
        }"
    )
}

fun ComposeTestRule.await(timeout: Long, node: () -> Unit) {
    val t0 = System.currentTimeMillis()
    do {
        try {
            node()
            return
        } catch (_: AssertionError) {
        }
        mainClock.advanceTimeBy(WaitTimeout1MilliSec)
        Thread.sleep(WaitTimeout1MilliSec)
    } while (System.currentTimeMillis() - t0 < timeout)
    throw AssertionError(
        "Node was not displayed after $timeout milliseconds. Root node was:\n${
        onRoot().printToString(Int.MAX_VALUE)
        }"
    )
}

fun ComposeTestRule.sleep(timeout: Long) {
    val t0 = System.currentTimeMillis()
    do {
        mainClock.advanceTimeBy(WaitTimeout1MilliSec)
        Thread.sleep(WaitTimeout1MilliSec)
    } while (System.currentTimeMillis() - t0 < timeout)
}

fun SemanticsNodeInteraction.assertHasText(includeEditableText: Boolean = true) =
    assert(hasText(includeEditableText))

fun hasText(
    includeEditableText: Boolean = true
): SemanticsMatcher {
    val propertyName = if (includeEditableText) {
        "${SemanticsProperties.Text.name} + ${SemanticsProperties.EditableText.name}"
    } else {
        SemanticsProperties.Text.name
    }
    return SemanticsMatcher(
        propertyName
    ) { node ->
        val actual = mutableListOf<String>()
        if (includeEditableText) {
            node.config.getOrNull(SemanticsProperties.EditableText)
                ?.let { actual.add(it.text) }
        }
        node.config.getOrNull(SemanticsProperties.Text)
            ?.let { actual.addAll(it.map { anStr -> anStr.text }) }
        actual.all { it.isNotBlank() }
    }
}

fun SemanticsNodeInteractionCollection.assertNone(
    matcher: SemanticsMatcher
): SemanticsNodeInteractionCollection =
    filter(matcher)
        .assertCountEquals(0)

fun hasPrescriptionId(id: String): SemanticsMatcher =
    SemanticsMatcher.expectValue(PrescriptionId, id)

fun hasPharmacyId(id: String): SemanticsMatcher =
    SemanticsMatcher.expectValue(PharmacyId, id)

fun hasInsuranceState(state: String?): SemanticsMatcher =
    SemanticsMatcher.expectValue(InsuranceState, state)

fun hasSubstitutionAllowed(allowed: Boolean): SemanticsMatcher =
    SemanticsMatcher.expectValue(SubstitutionAllowed, allowed)

fun hasSupplyForm(form: String): SemanticsMatcher =
    SemanticsMatcher.expectValue(SupplyForm, form)

fun hasMedicationCategory(form: String): SemanticsMatcher =
    SemanticsMatcher.expectValue(MedicationCategory, form)

fun execShellCmd(cmd: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(cmd)
}
