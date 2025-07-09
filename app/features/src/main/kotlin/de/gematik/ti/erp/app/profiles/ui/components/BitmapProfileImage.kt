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

package de.gematik.ti.erp.app.profiles.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
internal fun CircularBitmapImage(
    modifier: Modifier = Modifier,
    image: Bitmap,
    size: Dp = SizeDefaults.twentyfold
) {
    Image(
        painter = BitmapPainter(image.asImageBitmap()),
        contentDescription = null,
        modifier = modifier.size(size),
        contentScale = ContentScale.Crop
    )
}

@Composable
internal fun BitmapImage(
    modifier: Modifier = Modifier,
    image: ByteArray?
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, image) {
        value = image?.let { img ->
            BitmapFactory.decodeByteArray(img, 0, img.size).asImageBitmap()
        }
    }

    bitmap?.let {
        Image(
            modifier = modifier.fillMaxSize(),
            bitmap = it,
            contentDescription = null
        )
    }
}
