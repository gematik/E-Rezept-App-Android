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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

class PrescriptionDetailIngredientsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val selectedIngredient = remember {
            requireNotNull(
                navBackStackEntry.arguments?.getString(PrescriptionDetailRoutes.SelectedIngredient)
            )
        }
        val ingredient = remember(selectedIngredient) {
            fromNavigationString<SyncedTaskData.Ingredient>(selectedIngredient)
        }
        val scaffoldState = rememberScaffoldState()
        val listState = rememberLazyListState()

        AnimatedElevationScaffold(
            scaffoldState = scaffoldState,
            listState = listState,
            onBack = navController::popBackStack,
            topBarTitle = stringResource(R.string.synced_medication_ingredient_header),
            navigationMode = NavigationBarMode.Back,
            snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
            actions = {}
        ) { innerPadding ->
            PrescriptionDetailIngredientsScreenContent(
                listState,
                innerPadding,
                ingredient
            )
        }
    }
}

@Composable
private fun PrescriptionDetailIngredientsScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    ingredient: SyncedTaskData.Ingredient
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize().padding(innerPadding),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            IngredientNameLabel(ingredient.text)
        }
        ingredient.amount?.let {
            item {
                IngredientAmountLabel(it)
            }
        }
        ingredient.number?.let {
            item {
                IngredientNumberLabel(it)
            }
        }
        ingredient.form?.let {
            item {
                FormLabel(it)
            }
        }
        ingredient.strength?.let {
            item {
                StrengthLabel(it)
            }
        }
        item {
            SpacerMedium()
        }
    }
}

@Composable
private fun IngredientAmountLabel(amount: String) {
    Label(
        text = amount,
        label = stringResource(id = R.string.pres_detail_medication_label_ingredient_amount)
    )
}

@Composable
private fun IngredientNumberLabel(number: String) {
    Label(
        text = number,
        label = stringResource(id = R.string.pres_detail_medication_label_ingredient_number)
    )
}

@Composable
fun IngredientNameLabel(text: String, index: Int? = null, onClickLabel: (() -> Unit)? = null) {
    Label(
        text = text,
        label = annotatedStringResource(
            id = R.string.pres_detail_medication_label_ingredient_name,
            index ?: ""
        ).toString(),
        onClick = onClickLabel
    )
}

@Composable
private fun StrengthLabel(strength: SyncedTaskData.Ratio) {
    strength.numerator?.let {
        Label(
            text = it.value + " " + it.unit,
            label = stringResource(id = R.string.pres_detail_medication_label_ingredient_strength_unit)
        )
    }
}

@Preview
@Composable
private fun PrescriptionDetailIngredientsScreenContentPreview() {
    AppTheme {
        val listState = rememberLazyListState()
        val innerPadding = PaddingValues(PaddingDefaults.Medium)
        val ingredient = SyncedTaskData.Ingredient(
            text = "Ingredient",
            form = "some form text",
            number = "5",
            amount = "20 ml",
            strength = SyncedTaskData.Ratio(
                SyncedTaskData.Quantity("10", "ng"),
                SyncedTaskData.Quantity("10", "ml")
            )
        )
        PrescriptionDetailIngredientsScreenContent(
            listState,
            innerPadding,
            ingredient
        )
    }
}
