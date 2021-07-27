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

// package de.gematik.ti.erp.app.cardwall.ui
//
// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
// import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
// import de.gematik.ti.erp.app.di.AppSharedPreferences
// import de.gematik.ti.erp.app.di.SecureCardWallSharedPreferences
// import de.gematik.ti.erp.app.utils.CoroutineTestRule
// import de.gematik.ti.erp.app.utils.getOrAwaitValue
// import io.mockk.mockk
// import io.mockk.verify
// import kotlinx.coroutines.ExperimentalCoroutinesApi
// import org.junit.Assert.assertEquals
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
//
// @ExperimentalCoroutinesApi
// class CardWallViewModelTest {
//
//    private lateinit var viewModel: CardWallViewModel
//    private lateinit var appPrefs: AppSharedPreferences
//    private lateinit var secPrefs: SecureCardWallSharedPreferences
//    private lateinit var cardWallUseCase: CardWallUseCase
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    @get:Rule
//    val coroutineRule = CoroutineTestRule()
//
//    @Before
//    fun setup() {
//        appPrefs = mockk(relaxed = true)
//        secPrefs = mockk(relaxed = true)
//        cardWallUseCase = mockk(relaxed = true)
//
//        viewModel = CardWallViewModel(cardWallUseCase)
//    }
//
//    @Test
//    fun `reload view model`() {
//        viewModel.reload()
//
//        verify {
//            cardWallUseCase.getStoredCan()
//        }
//
//        val can = viewModel.cardAccessNumber.getOrAwaitValue()
//        assertEquals(can, "")
//    }
//
//    @Test
//    fun `pin check`() {
//        assertEquals(false, viewModel.checkPersonalIdentificationNumber("123"))
//        assertEquals(true, viewModel.checkPersonalIdentificationNumber("123456"))
//        assertEquals(true, viewModel.checkPersonalIdentificationNumber("1234567"))
//        assertEquals(true, viewModel.checkPersonalIdentificationNumber("12345678"))
//        assertEquals(false, viewModel.checkPersonalIdentificationNumber("123456789"))
//    }
//
//    @Test
//    fun `can check`() {
//        assertEquals(false, viewModel.checkPersonalIdentificationNumber("123"))
//        assertEquals(true, viewModel.checkPersonalIdentificationNumber("123456"))
//        assertEquals(false, viewModel.checkPersonalIdentificationNumber("123456789"))
//    }
// }
