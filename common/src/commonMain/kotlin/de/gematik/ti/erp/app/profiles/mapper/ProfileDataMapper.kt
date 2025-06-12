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

package de.gematik.ti.erp.app.profiles.mapper

import de.gematik.ti.erp.app.db.entities.v1.AvatarFigureV1
import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileColorNamesV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.idp.repository.toSingleSignOnTokenScope
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.SetToActiveProfile

@Suppress("CyclomaticComplexMethod")
fun ProfileEntityV1.toProfileData(
    state: SetToActiveProfile = SetToActiveProfile.NoChange
): ProfilesData.Profile =
    ProfilesData.Profile(
        id = this.id,
        color = when (this.color) {
            ProfileColorNamesV1.SPRING_GRAY -> ProfilesData.ProfileColorNames.SPRING_GRAY
            ProfileColorNamesV1.SUN_DEW -> ProfilesData.ProfileColorNames.SUN_DEW
            ProfileColorNamesV1.PINK -> ProfilesData.ProfileColorNames.PINK
            ProfileColorNamesV1.TREE -> ProfilesData.ProfileColorNames.TREE
            ProfileColorNamesV1.BLUE_MOON -> ProfilesData.ProfileColorNames.BLUE_MOON
        },
        avatar = when (this.avatarFigure) {
            AvatarFigureV1.PersonalizedImage -> ProfilesData.Avatar.PersonalizedImage
            AvatarFigureV1.FemaleDoctor -> ProfilesData.Avatar.FemaleDoctor
            AvatarFigureV1.WomanWithHeadScarf -> ProfilesData.Avatar.WomanWithHeadScarf
            AvatarFigureV1.Grandfather -> ProfilesData.Avatar.Grandfather
            AvatarFigureV1.BoyWithHealthCard -> ProfilesData.Avatar.BoyWithHealthCard
            AvatarFigureV1.OldManOfColor -> ProfilesData.Avatar.OldManOfColor
            AvatarFigureV1.WomanWithPhone -> ProfilesData.Avatar.WomanWithPhone
            AvatarFigureV1.Grandmother -> ProfilesData.Avatar.Grandmother
            AvatarFigureV1.ManWithPhone -> ProfilesData.Avatar.ManWithPhone
            AvatarFigureV1.WheelchairUser -> ProfilesData.Avatar.WheelchairUser
            AvatarFigureV1.Baby -> ProfilesData.Avatar.Baby
            AvatarFigureV1.MaleDoctorWithPhone -> ProfilesData.Avatar.MaleDoctorWithPhone
            AvatarFigureV1.FemaleDoctorWithPhone -> ProfilesData.Avatar.FemaleDoctorWithPhone
            AvatarFigureV1.FemaleDeveloper -> ProfilesData.Avatar.FemaleDeveloper
        },
        image = this.personalizedImage,
        name = this.name,
        insurantName = this.insurantName ?: "",
        insuranceIdentifier = this.insuranceIdentifier,
        insuranceName = this.insuranceName,
        insuranceOrganizationIdentifier = this.organizationIdentifier,
        insuranceType = when (this.insuranceType) {
            InsuranceTypeV1.GKV -> ProfilesData.InsuranceType.GKV
            InsuranceTypeV1.PKV -> ProfilesData.InsuranceType.PKV
            InsuranceTypeV1.None -> ProfilesData.InsuranceType.None
        },
        isConsentDrawerShown = this.isConsentDrawerShown,
        lastAuthenticated = this.lastAuthenticated?.toInstant(),
        lastAuditEventSynced = this.lastAuditEventSynced?.toInstant(),
        lastTaskSynced = this.lastTaskSynced?.toInstant(),
        active = when (state) {
            SetToActiveProfile.NoChange -> this.active
            is SetToActiveProfile.ChangeActiveState -> true
        },
        singleSignOnTokenScope = this.idpAuthenticationData?.toSingleSignOnTokenScope()
    )
