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

package de.gematik.ti.erp.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

private const val TARGET_OFFSET = -300
private const val INITIAL_OFFSET = 300
private const val SLIDE_TIME = 450
private const val TWEEN_DURATION = 770

fun <T> AnimatedContentTransitionScope<T>.slideInRight() = slideIntoContainer(
    initialOffset = { INITIAL_OFFSET },
    animationSpec = tween(SLIDE_TIME),
    towards = AnimatedContentTransitionScope.SlideDirection.Right
) + fadeInShort()

fun <T> AnimatedContentTransitionScope<T>.slideOutLeft() = slideOutOfContainer(
    targetOffset = { TARGET_OFFSET },
    animationSpec = tween(SLIDE_TIME),
    towards = AnimatedContentTransitionScope.SlideDirection.Left
) + fadeOutShort()

fun <T> AnimatedContentTransitionScope<T>.slideInDown() = slideIntoContainer(
    initialOffset = { INITIAL_OFFSET },
    animationSpec = tween(SLIDE_TIME),
    towards = AnimatedContentTransitionScope.SlideDirection.Down
) + fadeInShort()

fun <T> AnimatedContentTransitionScope<T>.slideOutUp() = slideOutOfContainer(
    targetOffset = { TARGET_OFFSET },
    animationSpec = tween(SLIDE_TIME),
    towards = AnimatedContentTransitionScope.SlideDirection.Up
) + fadeOutShort()

fun fadeInShort() = fadeIn(tween(durationMillis = SLIDE_TIME))

fun fadeOutShort() = fadeOut(tween(durationMillis = SLIDE_TIME))

fun fadeInLong() = fadeIn(tween(durationMillis = TWEEN_DURATION))

fun fadeOutLong() = fadeOut(tween(durationMillis = TWEEN_DURATION))
