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

package de.gematik.ti.erp.app.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.SizeDefaults.one

object SizeDefaults {
    /** `8dp` in design specification. */
    val one: Dp = 8.dp

    /** `0dp` in design specification. */
    val zero: Dp get() = multiple(0.0)

    /** `0.5dp` in design specification. */
    val sixteenth: Dp get() = multiple(.0625)

    /** `1dp` in design specification. */
    val eighth: Dp get() = multiple(.125)

    /** `2dp` in design specification. */
    val quarter: Dp get() = multiple(.25)

    /** `2.5dp` in design specification. */
    val fiveEighth: Dp get() = multiple(.3125)

    /** `3dp` in design specification. */
    val threeSeventyFifth: Dp get() = multiple(.375)

    /** `4dp` in design specification. */
    val half: Dp get() = multiple(.5)

    /** `5dp` in design specification. */
    val fivefoldHalf: Dp get() = multiple(0.625)

    /** `6dp` in design specification. */
    val threeQuarter: Dp get() = multiple(.75)

    /** `10dp` in design specification. */
    val oneQuarter: Dp get() = multiple(1.25)

    /** `12dp` in design specification. */
    val oneHalf: Dp get() = multiple(1.5)

    /** `14dp` in design specification. */
    val oneThreeQuarter: Dp get() = multiple(1.75)

    /** `16dp` in design specification. */
    val double: Dp get() = multiple(2.0)

    /** `20dp` in design specification. */
    val doubleHalf: Dp get() = multiple(2.5)

    /** `22dp` in design specification. */
    val doubleThreeQuarter: Dp get() = multiple(2.75)

    /** `24dp` in design specification. */
    val triple: Dp get() = multiple(3.0)

    /** `28dp` in design specification. */
    val tripleHalf: Dp get() = multiple(3.5)

    /** `32dp` in design specification. */
    val fourfold: Dp get() = multiple(4.0)

    /** `36dp` in design specification. */
    val fourfoldAndHalf: Dp get() = multiple(4.5)

    /** `40dp` in design specification. */
    val fivefold: Dp get() = multiple(5.0)

    /** `48dp` in design specification. */
    val sixfold: Dp get() = multiple(6.0)

    /** `50dp` in design specification. */
    val sixfoldAndQuarter: Dp get() = multiple(6.25)

    /** `56dp` in design specification. */
    val sevenfold: Dp get() = multiple(7.0)

    /** `60dp` in design specification. */
    val sevenfoldAndHalf: Dp get() = multiple(7.5)

    /** `64dp` in design specification. */
    val eightfold: Dp get() = multiple(8.0)

    /** `66dp` in design specification. */
    val eightfoldAndHalf: Dp get() = multiple(8.5)

    /** `68dp` in design specification. */
    val eightfoldAndThreeQuarter: Dp get() = multiple(8.75)

    /** `72dp` in design specification. */
    val ninefold: Dp get() = multiple(9.0)

    /** `80dp` in design specification. */
    val tenfold: Dp get() = multiple(10.0)

    /** `88dp` in design specification. */
    val elevenfold: Dp get() = multiple(11.0)

    /** `96dp` in design specification. */
    val twelvefold: Dp get() = multiple(12.0)

    /** `104dp` in design specification. */
    val thirteenfold: Dp get() = multiple(13.0)

    /** `120dp` in design specification. */
    val fifteenfold: Dp get() = multiple(15.0)

    /** `120dp` in design specification. */
    val fifteenfoldAndHalf: Dp get() = multiple(15.5)

    /** `128dp` in design specification. */
    val sixteenfold: Dp get() = multiple(16.0)

    /** `144dp` in design specification. */
    val eighteenfold: Dp get() = multiple(18.0)

    /** `160dp` in design specification. */
    val twentyfold: Dp get() = multiple(20.0)

    /** `184dp` in design specification. */
    val twentythreefold: Dp get() = multiple(23.0)

    /** `192dp` in design specification. */
    val twentyfourfold: Dp get() = multiple(24.0)

    /** `200dp` in design specification. */
    val twentyfivefold: Dp get() = multiple(25.0)

    /** `304dp` in design specification. */
    val thirtyEightfold: Dp get() = multiple(38.0)

    /** `320dp` in design specification. */
    val fortyfold: Dp get() = multiple(40.0)

    /** `336dp` in design specification. */
    val fortyTwofold: Dp get() = multiple(42.0)

    /** `352dp` in design specification. */
    val fortyFourfold: Dp get() = multiple(44.0)

    /** `540dp` in design specification. */
    val sixtySevenfold: Dp get() = multiple(67.0)

    /** `640dp` in design specification. */
    val eightyfold: Dp get() = multiple(80.0)

    /** `680dp` in design specification. */
    val eightyFivefold: Dp get() = multiple(85.0)

    /** `700dp` in design specification. */
    val eightyEightfold: Dp get() = multiple(88.0)

    /** Default horizontal padding. */
    val defaultStartEnd: Dp get() = triple
}

private fun multiple(multiplier: Float) = one * multiplier

private fun multiple(multiplier: Double) = multiple(multiplier.toFloat())
