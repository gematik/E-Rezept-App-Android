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

package de.gematik.ti.erp.app.utils

import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import de.gematik.ti.erp.app.app

/**
 * Easy to access Resource Helper, prevents storing context.
 * Usage: myString = strings\[stringResId] or drawables\[drawableResId]
 */

val strings by lazyFast { AppStrings() }
val drawables by lazyFast { AppDrawables() }
val colors by lazyFast { AppColors() }
val dimensions by lazyFast { AppDimensions() }
val animations by lazyFast { AppAnimations() }
val vectors by lazyFast { AppVectors() }
val intArrays by lazyFast { AppIntArray() }

class AppStrings {
    operator fun get(id: Int) = app().getString(id)
    fun getPlural(id: Int, quantity: Int, vararg formatArgs: Any) =
        app().resources.getQuantityString(id, quantity, *formatArgs)

    fun format(id: Int, vararg formatArgs: Any) =
        app().getString(id, *formatArgs)
}

class AppAnimations {
    operator fun get(id: Int) = app().resources.getAnimation(id)
}

class AppDimensions {
    operator fun get(id: Int) = app().resources.getDimension(id)
    fun getPx(id: Int) = app().resources.getDimensionPixelSize(id)
}

class AppDrawables {
    operator fun get(id: Int) = ContextCompat.getDrawable(app(), id)
}

class AppColors {
    operator fun get(id: Int) = ContextCompat.getColor(app(), id)
}

class AppVectors {
    operator fun get(id: Int) = VectorDrawableCompat.create(app().resources, id, null)
}

class AppIntArray {
    operator fun get(id: Int) = app().resources.getIntArray(id)
}
