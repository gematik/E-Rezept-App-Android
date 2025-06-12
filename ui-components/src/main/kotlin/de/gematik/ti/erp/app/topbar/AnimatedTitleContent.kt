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

package de.gematik.ti.erp.app.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow

/**
 * A composable that animates the visibility of a title based on the scroll state of a LazyList.
 *
 * This is typically used in scenarios where the title should appear only after scrolling past
 * the top of the list (e.g., in a collapsing toolbar or animated top app bar).
 *
 * The animation combines a vertical slide and fade transition using [AnimatedContent].
 *
 * @param listState The [LazyListState] whose scroll position determines when the title appears.
 * @param title The text to be shown as the title when the list is scrolled down.
 * @param label An optional label used for debugging and inspection of the [AnimatedContent].
 * @param transitionSpec A lambda specifying the enter/exit animations based on scroll state.
 * Defaults to a vertical slide and fade with [SizeTransform] that does not clip content.
 *
 * Usage example:
 * ```
 * AnimatedTitleContent(
 *     listState = rememberLazyListState(),
 *     title = "Digital Health Application"
 * )
 * ```
 */
@Composable
fun AnimatedTitleContent(
    listState: LazyListState,
    title: String,
    label: String = "AnimatedTitleContent",
    transitionSpec: AnimatedContentTransitionScope<Boolean>.() -> ContentTransform = {
        if (targetState) {
            slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                slideOutVertically(targetOffsetY = { it }) + fadeOut()
        } else {
            slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        }.using(sizeTransform = SizeTransform(clip = false))
    }
) {
    val isElevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    AnimatedContent(
        targetState = isElevated,
        label = label,
        transitionSpec = transitionSpec
    ) { elevated ->
        val animatedTitle = if (elevated) title else ""
        Text(animatedTitle, overflow = TextOverflow.Ellipsis)
        Text(
            text = animatedTitle,
            overflow = TextOverflow.Ellipsis
        )
    }
}
