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

package de.gematik.ti.erp.app.debugsettings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.AdaptableHeight
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.FullScreenHeight
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.SmallHeight
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerMedium

class BottomSheetSampleScreen(
    private val height: BottomSheetSampleHeight,
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(
    skipPartiallyExpanded = true,
    allowStateChange = true
) {

    sealed class BottomSheetSampleHeight(val value: Dp?) {
        data object AdaptableHeight : BottomSheetSampleHeight(null)
        data object SmallHeight : BottomSheetSampleHeight(200.dp)
        data object FullScreenHeight : BottomSheetSampleHeight(1800.dp)
    }

    @Composable
    override fun Content() {
        BackHandler {
            navController.popBackStack()
        }
        BottomSheetSample(height = height)
    }
}

@Composable
private fun BottomSheetSample(
    height: BottomSheetSampleScreen.BottomSheetSampleHeight
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (height != AdaptableHeight) {
                    Modifier
                        .height(height.value!!)
                        .padding(horizontal = PaddingDefaults.Medium)
                } else {
                    Modifier
                        .padding(horizontal = PaddingDefaults.Medium)
                }
            )

    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpacerMedium()
            Icon(
                painter = painterResource(R.drawable.ic_onboarding_logo_gematik),
                contentDescription = "onboarding-logo-gematik",
                tint = AppTheme.colors.primary900
            )
            SpacerMedium()
            when (height) {
                AdaptableHeight -> Text(
                    text = "(Height as per the content)",
                    style = AppTheme.typography.subtitle1
                )

                FullScreenHeight -> Text(
                    text = "(Height = ${height.value})",
                    style = AppTheme.typography.subtitle1
                )

                SmallHeight -> Text(
                    text = "(Height = ${height.value})",
                    style = AppTheme.typography.subtitle1
                )
            }
            SpacerMedium()
            Text(
                text = "Qu'est-ce que le Lorem Ipsum?",
                style = AppTheme.typography.h6
            )
            SpacerMedium()
            Text(
                text = "Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page " +
                    "avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les " +
                    "années 1500, quand un imprimeur anonyme assembla ensemble des morceaux de texte pour " +
                    "réaliser un livre spécimen de polices de texte. Il n'a pas fait que survivre cinq siècles, " +
                    "mais s'est aussi adapté à la bureautique informatique, sans que son contenu n'en soit " +
                    "modifié. Il a été popularisé dans les années 1960 grâce à la vente de feuilles Letraset " +
                    "contenant des passages du Lorem Ipsum, et, plus récemment, par son inclusion dans des " +
                    "applications de mise en page de texte, comme Aldus PageMaker.",
                style = AppTheme.typography.body1
            )
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
fun BottomSheetSampleAdjustableHeightPreview() {
    PreviewAppTheme {
        BottomSheetSample(AdaptableHeight)
    }
}

@LightDarkPreview
@Composable
fun BottomSheetSampleFixedHeightPreview() {
    PreviewAppTheme {
        BottomSheetSample(height = SmallHeight)
    }
}
