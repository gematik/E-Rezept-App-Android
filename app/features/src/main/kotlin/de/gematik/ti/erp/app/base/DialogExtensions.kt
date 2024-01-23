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

package de.gematik.ti.erp.app.base

import android.app.Dialog
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

fun Dialog.remove() {
    dismiss()
    cancel()
}

fun Dialog.setViewAndShow(view: ComposeView) {
    setContentView(view)
    setCancelable(true)
    show()
}

fun Dialog.setDecorView(activity: BaseActivity) {
    window?.decorView?.let {
        it.setViewTreeLifecycleOwner(activity)
        it.setViewTreeSavedStateRegistryOwner(activity)
        it.setViewTreeOnBackPressedDispatcherOwner(activity)
        it.setViewTreeViewModelStoreOwner(activity)
    }
}
