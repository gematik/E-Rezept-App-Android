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

package de.gematik.ti.erp.app.profile.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ProfileImageDataErpModel(
    val color: ProfileColorNames,
    val avatar: Avatar,
    @Transient val image: ByteArray? = null
) {
    fun hasNoImageSelected() = avatar == Avatar.PersonalizedImage &&
        image == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProfileImageDataErpModel
        if (color != other.color) return false
        if (avatar != other.avatar) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + avatar.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}

enum class ProfileColorNames {
    SPRING_GRAY,
    SUN_DEW,
    PINK,
    TREE,
    BLUE_MOON
}

enum class Avatar {
    PersonalizedImage,
    FemaleDoctor,
    WomanWithHeadScarf,
    Grandfather,
    BoyWithHealthCard,
    OldManOfColor,
    WomanWithPhone,
    Grandmother,
    ManWithPhone,
    WheelchairUser,
    Baby,
    MaleDoctorWithPhone,
    FemaleDoctorWithPhone,
    FemaleDeveloper
}
