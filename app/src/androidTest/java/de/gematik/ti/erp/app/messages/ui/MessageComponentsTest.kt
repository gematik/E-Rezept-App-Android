/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import de.gematik.ti.erp.app.DefaultDispatchProvider
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.messages.testErrorUIMessage
import de.gematik.ti.erp.app.messages.testUIMessage
import de.gematik.ti.erp.app.messages.ui.models.UIMessage
import de.gematik.ti.erp.app.messages.usecase.MessageUseCase
import de.gematik.ti.erp.app.theme.AppTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
class MessageComponentsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var viewModel: MessageViewModel
    private lateinit var useCase: MessageUseCase
    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = MessageViewModel(useCase, DefaultDispatchProvider())
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
    }

    @Test
    fun testEmptyMessages_showsEmptyScreen() {
        every { useCase.loadCommunicationsLocally(any()) } returns flow { emit(listOf<UIMessage>()) }
        composeTestRule.setContent {
            AppTheme {
                MessageScreen(navController, viewModel)
            }
        }
        composeTestRule.onNodeWithText("Keine Mitteilungen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sie haben", substring = true).assertIsDisplayed()
    }

    @Test
    fun testNonEmptyMessages_showsMessages() {
        every { useCase.loadCommunicationsLocally(any()) } returns
            flow {
                emit(
                    listOf(
                        testUIMessage()
                    )
                )
            }

        composeTestRule.setContent {
            AppTheme {
                MessageScreen(navController, viewModel)
            }
        }
        composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    }

    @Test
    fun testNonEmptyMessages_showErrorMessage() {
        every { useCase.loadCommunicationsLocally(any()) } returns
            flow {
                emit(
                    listOf(
                        testUIMessage(),
                        testErrorUIMessage()
                    )
                )
            }

        composeTestRule.setContent {
            AppTheme {
                MessageScreen(navController, viewModel)
            }
        }
        composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fehlerhafte", substring = true).assertIsDisplayed()
    }
}
