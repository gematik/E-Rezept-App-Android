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

package de.gematik.ti.erp.app.navigation

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.extensions.navigationBarsWithImePadding

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun NavigationGraphBuilder(
    bottomSheetNavigator: BottomSheetNavigator,
    navHostController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    ModalBottomSheetLayout(
        modifier = Modifier.navigationBarsWithImePadding().systemBarsPadding(),
        sheetShape = RoundedCornerShape(topStart = SizeDefaults.double, topEnd = SizeDefaults.double),
        bottomSheetNavigator = bottomSheetNavigator
    ) {
        NavHost(
            navHostController,
            startDestination = startDestination
        ) {
            builder(this)
        }
    }
}
