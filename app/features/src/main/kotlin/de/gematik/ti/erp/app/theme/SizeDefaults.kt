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

package de.gematik.ti.erp.app.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.SizeDefaults.one

object SizeDefaults {
    /** `8dp` in design specification. */
    val one: Dp = 8.dp

    /** `0dp` in design specification. */
    val zero: Dp get() = multiple(0.0)

    /** `1dp` in design specification. */
    val eighth: Dp get() = multiple(.125)

    /** `2dp` in design specification. */
    val quarter: Dp get() = multiple(.25)

    /** `4dp` in design specification. */
    val half: Dp get() = multiple(.5)

    /** `6dp` in design specification. */
    val threeQuarter: Dp get() = multiple(.75)

    /** `10dp` in design specification. */
    val oneQuarter: Dp get() = multiple(1.25)

    /** `12dp` in design specification. */
    val oneHalf: Dp get() = multiple(1.5)

    /** `16dp` in design specification. */
    val double: Dp get() = multiple(2.0)

    /** `20dp` in design specification. */
    val doubleHalf: Dp get() = multiple(2.5)

    /** `24dp` in design specification. */
    val triple: Dp get() = multiple(3.0)

    /** `28dp` in design specification. */
    val tripleHalf: Dp get() = multiple(3.5)

    /** `32dp` in design specification. */
    val fourfold: Dp get() = multiple(4.0)

    /** `40dp` in design specification. */
    val fivefold: Dp get() = multiple(5.0)

    /** `48dp` in design specification. */
    val sixfold: Dp get() = multiple(6.0)

    /** `56dp` in design specification. */
    val sevenfold: Dp get() = multiple(7.0)

    /** `64dp` in design specification. */
    val eightfold: Dp get() = multiple(8.0)

    /** `72dp` in design specification. */
    val ninefold: Dp get() = multiple(9.0)

    /** `80dp` in design specification. */
    val tenfold: Dp get() = multiple(10.0)

    /** `88dp` in design specification. */
    val elevenfold: Dp get() = multiple(11.0)

    /** `96dp` in design specification. */
    val twelvefold: Dp get() = multiple(12.0)

    /** `184dp` in design specification. */
    val twentythreefold: Dp get() = multiple(23.0)

    /** `192dp` in design specification. */
    val twentyfourfold: Dp get() = multiple(24.0)

    /** `304dp` in design specification. */
    val thirtyEightfold: Dp get() = multiple(38.0)

    /** Default horizontal padding. */
    val defaultStartEnd: Dp get() = triple
}

private fun multiple(multiplier: Float) = one * multiplier

private fun multiple(multiplier: Double) = multiple(multiplier.toFloat())
