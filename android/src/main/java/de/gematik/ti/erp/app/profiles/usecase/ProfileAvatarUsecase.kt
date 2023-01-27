/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val BitmapQuality = 100

class ProfileAvatarUseCase(
    private val profilesRepository: ProfilesRepository,
    private val dispatcher: DispatchProvider
) {
    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatarFigure: ProfilesData.AvatarFigure) {
        withContext(dispatcher.IO) {
            profilesRepository.saveAvatarFigure(profileId, avatarFigure)
        }
    }

    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: Bitmap) {
        withContext(dispatcher.IO) {
            val outputStream = ByteArrayOutputStream()
            profileImage.compress(Bitmap.CompressFormat.PNG, BitmapQuality, outputStream)
            val byteArray: ByteArray = outputStream.toByteArray()
            profilesRepository.savePersonalizedProfileImage(profileId, byteArray)
        }
    }

    suspend fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        withContext(dispatcher.IO) {
            profilesRepository.clearPersonalizedProfileImage(profileId)
        }
    }
}
