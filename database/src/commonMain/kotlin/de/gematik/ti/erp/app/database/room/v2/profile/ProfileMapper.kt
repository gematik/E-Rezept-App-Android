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

package de.gematik.ti.erp.app.database.room.v2.profile

import de.gematik.ti.erp.app.database.room.v2.userAuthentication.UserAuthenticationEntity
import de.gematik.ti.erp.app.database.room.v2.userAuthentication.toUserAuthenticationErpModel
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel

fun ProfileEntity.toProfileErpModel(
    userAuthentication: UserAuthenticationEntity?
): ProfileErpModel =
    ProfileErpModel(
        id = identifier,
        name = name,
        active = active,
        isNewlyCreated = isNew,
        profileImageData = profileImageData.toProfileImageDataErpModel(),
        insuranceData = insuranceData.toProfileInsuranceDataErpModel(),
        isConsentDrawerShown = showInvoiceConsentDrawer,
        lastAuthenticated = lastAuthenticated,
        lastAuditEventSynced = lastAuditEventSynced,
        lastTaskSynced = lastTaskSynced,
        userAuthentication = userAuthentication?.toUserAuthenticationErpModel()
            ?: UserAuthenticationErpModel.NotInitialized
    )

fun ProfileErpModel.toProfileEntity(): ProfileEntity =
    ProfileEntity(
        identifier = id,
        isNew = isNewlyCreated,
        active = active,
        name = name,

        profileImageData = profileImageData.toProfileImageDataEmbeddable(),
        insuranceData = insuranceData.toProfileInsuranceDataEmbeddable(),
        showInvoiceConsentDrawer = isConsentDrawerShown,

        lastAuthenticated = lastAuthenticated,
        lastAuditEventSynced = lastAuditEventSynced,
        lastTaskSynced = lastTaskSynced
    )

fun ProfileWithUserAuthenticationEntity.toProfileErpModel(): ProfileErpModel =
    profile.toProfileErpModel(userAuthentication)

private fun ProfileImageDataErpModel.toProfileImageDataEmbeddable(): ProfileImageDataEmbeddable =
    ProfileImageDataEmbeddable(
        avatar = avatar.name,
        image = image,
        color = color.name
    )

private fun ProfileImageDataEmbeddable.toProfileImageDataErpModel(): ProfileImageDataErpModel =
    ProfileImageDataErpModel(
        color = ProfileColorNames.valueOf(color),
        avatar = Avatar.valueOf(avatar),
        image = image
    )

private fun ProfileInsuranceDataErpModel.toProfileInsuranceDataEmbeddable(): ProfileInsuranceDataEmbeddable =
    ProfileInsuranceDataEmbeddable(
        insurantName = insurantName,
        insuranceName = insuranceName,
        insuranceId = insuranceIdentifier,
        insuranceType = insuranceType.name,
        organizationId = organizationIdentifier
    )

private fun ProfileInsuranceDataEmbeddable.toProfileInsuranceDataErpModel(): ProfileInsuranceDataErpModel =
    ProfileInsuranceDataErpModel(
        insurantName = insurantName,
        insuranceIdentifier = insuranceId,
        insuranceName = insuranceName?.ifEmpty { null },
        insuranceType = InsuranceType.valueOf(insuranceType),
        organizationIdentifier = organizationId
    )
