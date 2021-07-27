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

package de.gematik.ti.erp.app.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.WindowDecorationColors

typealias ViewModelSet = Set<BaseViewModel>

/**
 * Return a single view model matching the type [T].
 */
inline fun <reified T : BaseViewModel> ViewModelSet.get(): T =
    this.find { it is T } as? T ?: error("No view model with type ${T::class} found!")

/**
 * Convenience functions to return view models matching the type [T].
 * E.g. `val (vm1: VM1, vm2: VM2) = viewModelSet`
 */
inline operator fun <reified T : BaseViewModel> ViewModelSet.component1(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component2(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component3(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component4(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component5(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component6(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component7(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component8(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component9(): T =
    this.get()

inline operator fun <reified T : BaseViewModel> ViewModelSet.component10(): T =
    this.get()

/**
 * Ambient of ViewModels of the containing fragment.
 */
val LocalViewModels =
    staticCompositionLocalOf<ViewModelSet> { error("No view models provided!") }

val LocalActivity =
    staticCompositionLocalOf<ComponentActivity> { error("No activity provided!") }

/**
 * Ambient [NavController] of the underlying fragment.
 */
val LocalFragmentNavController =
    staticCompositionLocalOf<NavController> { error("No navigation controller provided!") }

/**
 * Clipboard manager.
 */
val LocalClipBoardManager =
    staticCompositionLocalOf<(label: String, text: String) -> Unit> { error("No clipboard handler provided!") }

abstract class ComposeBaseFragment : Fragment() {
    abstract val content: @Composable () -> Unit

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalActivity provides requireActivity(),
                    LocalViewModels provides viewModels(),
                    LocalFragmentNavController provides findNavController()
                ) {
                    AppTheme {
                        WindowDecorationColors(MaterialTheme.colors.surface, MaterialTheme.colors.surface)

                        content()
                    }
                }
            }
        }
    }

    open fun viewModels(): ViewModelSet = setOf()
}
