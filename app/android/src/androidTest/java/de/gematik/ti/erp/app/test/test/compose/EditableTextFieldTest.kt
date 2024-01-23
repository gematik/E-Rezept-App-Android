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

package de.gematik.ti.erp.app.test.test.compose

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.EditableTextField
import de.gematik.ti.erp.app.utils.compose.ErrorTextTag
import io.github.aakira.napier.Napier
import org.junit.Rule
import org.junit.Test

class EditableTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEditableTextField() {
        val text = "some random text"
        val newText = "some new text"

        composeTestRule.setContent {
            AppTheme {
                EditableTextField(
                    text = text,
                    textMinLength = 20,
                    onDoneClicked = {
                        Napier.d { "text is $it" }
                    }
                )
            }
        }

        // test existing text
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.onNodeWithText(text).assertHasClickAction()

        // test text change
        composeTestRule.onNodeWithText(text).performTextReplacement(newText)
        composeTestRule.onNodeWithText(newText).assertIsDisplayed()

        // test showing error text
        composeTestRule.onNodeWithText(newText).performTextClearance()
        composeTestRule.onNodeWithTag(ErrorTextTag).assertIsDisplayed()
    }
}
