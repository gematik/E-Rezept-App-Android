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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.prescription.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.gematik.ti.erp.app.CoroutineTestRule
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("UnusedPrivateMember")
class TwoDCodeValidatorTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var validator: TwoDCodeValidator

    private val scannedTask1 = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\"\n" +
            "  ]\n" +
            "}",
        Clock.System.now()
    )

    private val scannedTask3 = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\",\n" +
            "    \"Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629\",\n" +
            "    \"Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5\"\n" +
            "  ]\n" +
            "}",
        Clock.System.now()
    )

    private val scannedTask4 = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\",\n" +
            "    \"Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629\",\n" +
            "    \"Task/2aef43b8c5e8f263d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629\",\n" +
            "    \"Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5\"\n" +
            "  ]\n" +
            "}",
        Clock.System.now()
    )

    private val notWellFormatted = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\",\n" +
            "    \"Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5\",\n" +
            "  ]\n" +
            "}",
        Clock.System.now()
    )

    private val emptyUrls = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "  ]\n" +
            "}",
        Clock.System.now()
    )

    private val checkedTask1 = ValidScannedCode(
        ScannedCode(
            "{\n" +
                "  \"urls\": [\n" +
                "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\"\n" +
                "  ]\n" +
                "}",
            Clock.System.now()
        ),
        mutableListOf(
            "Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea"
        )
    )

    private val checkedTask3 = ValidScannedCode(
        ScannedCode(
            "{\n" +
                "  \"urls\": [\n" +
                "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\",\n" +
                "    \"Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629\",\n" +
                "    \"Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5\"\n" +
                "  ]\n" +
                "}",
            Clock.System.now()
        ),
        mutableListOf(
            "Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            "Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629",
            "Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5"
        )
    )

    @Before
    fun setup() {
        validator = TwoDCodeValidator()
    }

    @Test
    fun `validate 1 task json - returns checked bundle with 1 task`() {
        val checkedBundle = validator.validate(scannedTask1)
        assertEquals(checkedTask1.urls, checkedBundle!!.urls)
    }

    @Test
    fun `validate 3 task json - returns checked bundle with 3 tasks`() {
        val checkedBundle = validator.validate(scannedTask3)
        assertEquals(checkedTask3.urls, checkedBundle!!.urls)
    }

    @Test
    fun `validate empty urls - returns null`() {
        val checkedBundle = validator.validate(emptyUrls)
        assertEquals(null, checkedBundle)
    }

    @Test
    fun `validate 4 task json urls - returns null`() {
        val checkedBundle = validator.validate(scannedTask4)
        assertEquals(null, checkedBundle)
    }
}
