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

package de.gematik.ti.erp.app.profiles.usecase

import android.graphics.Bitmap
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Avatar
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.ClearImage
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Color
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Image
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Name
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class UpdateProfileUseCase(
    private val repository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(modifier: ProfileModifier, id: ProfileIdentifier) {
        withContext(dispatcher) {
            when (modifier) {
                is Avatar -> repository.saveAvatarFigure(id, modifier.value)
                is Color -> repository.updateProfileColor(id, modifier.value)
                is Image -> repository.savePersonalizedProfileImage(id, modifier.value.toByteArray())
                is Name -> repository.updateProfileName(id, modifier.value)
                is ClearImage -> repository.clearPersonalizedProfileImage(id)
            }
        }
    }

    companion object {
        private const val BitmapQuality = 100
        private fun Bitmap.toByteArray(): ByteArray {
            val outputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.PNG, BitmapQuality, outputStream)
            return outputStream.toByteArray()
        }

        sealed interface ProfileModifier {
            data class Name(val value: String) : ProfileModifier
            data class Color(val value: ProfilesData.ProfileColorNames) : ProfileModifier
            data class Image(val value: Bitmap) : ProfileModifier
            data class Avatar(val value: ProfilesData.Avatar) : ProfileModifier
            data object ClearImage : ProfileModifier
        }
    }
}
