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

@file:Suppress("TopLevelPropertyNaming")

package de.gematik.ti.erp.app.permissions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.Requirement
import java.io.ByteArrayOutputStream

const val cameraPermission = android.Manifest.permission.CAMERA

@Composable
fun getBitmapFromCamera(
    onResult: (Bitmap) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
) {
    it?.let { originalBitmap ->
        onResult(removeMetadataFromBitmap(originalBitmap))
    }
}

@Suppress("MagicNumber")
@Requirement(
    "O.Data_8#2",
    "O.Data_9#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Metadata is removed from the bitmap before it is stored for avatar image."
)
fun removeMetadataFromBitmap(originalBitmap: Bitmap): Bitmap {
    // Compress the original bitmap to a byte array
    val byteArrayOutputStream = ByteArrayOutputStream()
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()

    // Decode the byte array back into a bitmap
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
