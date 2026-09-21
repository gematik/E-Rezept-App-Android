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

package de.gematik.ti.erp.app.database.realm.v1.profile

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.userauthentication.toIdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.database.realm.v1.userauthentication.toUserAuthenticationErpModel
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel

fun ProfileEntityV1.toProfileErpModel(): ProfileErpModel =
    ProfileErpModel(
        id = id,
        name = name,
        active = active,
        isNewlyCreated = isNewlyCreated,
        profileImageData = ProfileImageDataErpModel(
            color = color.toErpEnum(),
            avatar = avatarFigure.toErpEnum(),
            image = personalizedImage
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = insurantName,
            insuranceIdentifier = insuranceIdentifier,
            insuranceName = insuranceName,
            insuranceType = insuranceType.toErpEnum(),
            organizationIdentifier = organizationIdentifier
        ),
        isConsentDrawerShown = isConsentDrawerShown,
        lastAuthenticated = lastAuthenticated?.toInstant(),
        lastAuditEventSynced = lastAuditEventSynced?.toInstant(),
        lastTaskSynced = lastTaskSynced?.toInstant(),
        userAuthentication = idpAuthenticationData?.toUserAuthenticationErpModel() ?: UserAuthenticationErpModel.NotInitialized
    )

internal fun ProfileErpModel.toRealmEntity(): ProfileEntityV1 {
    return ProfileEntityV1().apply {
        id = this@toRealmEntity.id
        name = this@toRealmEntity.name
        color = this@toRealmEntity.profileImageData.color.toRealmEnum()
        avatarFigure = this@toRealmEntity.profileImageData.avatar.toRealmEnum()
        personalizedImage = this@toRealmEntity.profileImageData.image
        isConsentDrawerShown = this@toRealmEntity.isConsentDrawerShown
        insurantName = this@toRealmEntity.insuranceData.insurantName
        insuranceIdentifier = this@toRealmEntity.insuranceData.insuranceIdentifier
        insuranceName = this@toRealmEntity.insuranceData.insuranceName
        insuranceType = this@toRealmEntity.insuranceData.insuranceType.toRealmEnum()
        organizationIdentifier = this@toRealmEntity.insuranceData.organizationIdentifier
        lastAuthenticated = this@toRealmEntity.lastAuthenticated?.toRealmInstant()
        lastAuditEventSynced = this@toRealmEntity.lastAuditEventSynced?.toRealmInstant()
        lastTaskSynced = this@toRealmEntity.lastTaskSynced?.toRealmInstant()
        active = this@toRealmEntity.active
        isNewlyCreated = this@toRealmEntity.isNewlyCreated
        idpAuthenticationData = this@toRealmEntity.userAuthentication.toIdpAuthenticationDataEntityV1()
    }
}

internal fun ProfileColorNames.toRealmEnum() = when (this) {
    ProfileColorNames.SPRING_GRAY -> ProfileColorNamesV1.SPRING_GRAY
    ProfileColorNames.SUN_DEW -> ProfileColorNamesV1.SUN_DEW
    ProfileColorNames.PINK -> ProfileColorNamesV1.PINK
    ProfileColorNames.TREE -> ProfileColorNamesV1.TREE
    ProfileColorNames.BLUE_MOON -> ProfileColorNamesV1.BLUE_MOON
}

internal fun Avatar.toRealmEnum() = when (this) {
    Avatar.PersonalizedImage -> AvatarFigureV1.PersonalizedImage
    Avatar.FemaleDoctor -> AvatarFigureV1.FemaleDoctor
    Avatar.WomanWithHeadScarf -> AvatarFigureV1.WomanWithHeadScarf
    Avatar.Grandfather -> AvatarFigureV1.Grandfather
    Avatar.BoyWithHealthCard -> AvatarFigureV1.BoyWithHealthCard
    Avatar.OldManOfColor -> AvatarFigureV1.OldManOfColor
    Avatar.WomanWithPhone -> AvatarFigureV1.WomanWithPhone
    Avatar.Grandmother -> AvatarFigureV1.Grandmother
    Avatar.ManWithPhone -> AvatarFigureV1.ManWithPhone
    Avatar.WheelchairUser -> AvatarFigureV1.WheelchairUser
    Avatar.Baby -> AvatarFigureV1.Baby
    Avatar.MaleDoctorWithPhone -> AvatarFigureV1.MaleDoctorWithPhone
    Avatar.FemaleDoctorWithPhone -> AvatarFigureV1.FemaleDoctorWithPhone
    Avatar.FemaleDeveloper -> AvatarFigureV1.FemaleDeveloper
}

internal fun InsuranceType.toRealmEnum() = when (this) {
    InsuranceType.GKV -> InsuranceTypeV1.GKV
    InsuranceType.PKV -> InsuranceTypeV1.PKV
    InsuranceType.BUND -> InsuranceTypeV1.BUND
    InsuranceType.NONE -> InsuranceTypeV1.None
}

internal fun ProfileColorNamesV1.toErpEnum() = when (this) {
    ProfileColorNamesV1.SPRING_GRAY -> ProfileColorNames.SPRING_GRAY
    ProfileColorNamesV1.SUN_DEW -> ProfileColorNames.SUN_DEW
    ProfileColorNamesV1.PINK -> ProfileColorNames.PINK
    ProfileColorNamesV1.TREE -> ProfileColorNames.TREE
    ProfileColorNamesV1.BLUE_MOON -> ProfileColorNames.BLUE_MOON
}

internal fun AvatarFigureV1.toErpEnum() = when (this) {
    AvatarFigureV1.PersonalizedImage -> Avatar.PersonalizedImage
    AvatarFigureV1.FemaleDoctor -> Avatar.FemaleDoctor
    AvatarFigureV1.WomanWithHeadScarf -> Avatar.WomanWithHeadScarf
    AvatarFigureV1.Grandfather -> Avatar.Grandfather
    AvatarFigureV1.BoyWithHealthCard -> Avatar.BoyWithHealthCard
    AvatarFigureV1.OldManOfColor -> Avatar.OldManOfColor
    AvatarFigureV1.WomanWithPhone -> Avatar.WomanWithPhone
    AvatarFigureV1.Grandmother -> Avatar.Grandmother
    AvatarFigureV1.ManWithPhone -> Avatar.ManWithPhone
    AvatarFigureV1.WheelchairUser -> Avatar.WheelchairUser
    AvatarFigureV1.Baby -> Avatar.Baby
    AvatarFigureV1.MaleDoctorWithPhone -> Avatar.MaleDoctorWithPhone
    AvatarFigureV1.FemaleDoctorWithPhone -> Avatar.FemaleDoctorWithPhone
    AvatarFigureV1.FemaleDeveloper -> Avatar.FemaleDeveloper
}

internal fun InsuranceTypeV1.toErpEnum() = when (this) {
    InsuranceTypeV1.GKV -> InsuranceType.GKV
    InsuranceTypeV1.PKV -> InsuranceType.PKV
    InsuranceTypeV1.BUND -> InsuranceType.BUND
    InsuranceTypeV1.None -> InsuranceType.NONE
}
