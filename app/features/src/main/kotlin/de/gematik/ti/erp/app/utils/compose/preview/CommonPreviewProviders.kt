/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.utils.compose.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.card.model.command.UnlockMethod

class BooleanPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

data class CanBoolean(val can: String, val boolean: Boolean)

class CanBooleanPreviewParameterProvider : PreviewParameterProvider<CanBoolean> {
    override val values = sequenceOf(
        CanBoolean("123123", false),
        CanBoolean("123123", true),
        CanBoolean("", false),
        CanBoolean("", true)
    )
}

class UnlockMethodPreviewParameterProvider : PreviewParameterProvider<UnlockMethod> {
    override val values = UnlockMethod.entries.asSequence()
}

data class Pin(val pin: String)

class PinPreviewParameterProvider : PreviewParameterProvider<Pin> {
    override val values = sequenceOf(
        Pin(""),
        Pin("123"),
        Pin("123456"),
        Pin("123456789"),
        Pin("123 123")
    )
}

class PukPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "",
        "123",
        "123123",
        "123123123",
        "123 123"
    )
}

class CanPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "",
        "123",
        "123123",
        "123123123",
        "123 123"
    )
}
