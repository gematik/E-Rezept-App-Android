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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun Body1Text(text: String) {
    Text(text = text, style = AppTheme.typography.body1)
}

@Composable
fun Body1lText(text: String) {
    Text(text = text, style = AppTheme.typography.body1l)
}

@Composable
fun Body2Text(text: String) {
    Text(text = text, style = AppTheme.typography.body2)
}

@Composable
fun Body2lText(text: String) {
    Text(text = text, style = AppTheme.typography.body2l)
}

@Composable
fun Subtitle1Text(text: String) {
    Text(text = text, style = AppTheme.typography.subtitle1)
}

@Composable
fun Subtitle1lText(text: String) {
    Text(text = text, style = AppTheme.typography.subtitle1l)
}

@Composable
fun Subtitle2Text(text: String) {
    Text(text = text, style = AppTheme.typography.subtitle2)
}

@Composable
fun Subtitle2lText(text: String) {
    Text(text = text, style = AppTheme.typography.subtitle2l)
}

@Composable
fun Caption1Text(text: String) {
    Text(text = text, style = AppTheme.typography.caption1)
}

@Composable
fun Caption1lText(text: String) {
    Text(text = text, style = AppTheme.typography.caption1l)
}

@Composable
fun Caption2Text(text: String) {
    Text(text = text, style = AppTheme.typography.caption2)
}

@Composable
fun H1Text(text: String) {
    Text(text = text, style = AppTheme.typography.h1)
}

@Composable
fun H2Text(text: String) {
    Text(text = text, style = AppTheme.typography.h2)
}

@Composable
fun H3Text(text: String) {
    Text(text = text, style = AppTheme.typography.h3)
}

@Composable
fun H4Text(text: String) {
    Text(text = text, style = AppTheme.typography.h4)
}

@Composable
fun H5Text(text: String) {
    Text(text = text, style = AppTheme.typography.h5)
}

@Composable
fun H6Text(text: String) {
    Text(text = text, style = AppTheme.typography.h6)
}

@Composable
fun ButtonText(text: String) {
    Text(text = text, style = AppTheme.typography.button)
}
