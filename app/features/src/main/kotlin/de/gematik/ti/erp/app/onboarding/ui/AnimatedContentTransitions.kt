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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

fun <T> AnimatedContentTransitionScope<T>.fade() = fadeIn(tween(durationMillis = 770)) togetherWith
    fadeOut(tween(durationMillis = 770))
fun <T> AnimatedContentTransitionScope<T>.slideRight() =
    slideIntoContainer(Right) togetherWith slideOutOfContainer(Right)
fun <T> AnimatedContentTransitionScope<T>.slideLeft() = slideIntoContainer(Left) togetherWith slideOutOfContainer(Left)
fun <T> AnimatedContentTransitionScope<T>.slideHorizontal() = slideInHorizontally { it } + fadeIn() togetherWith
    slideOutHorizontally { it } + fadeOut()
