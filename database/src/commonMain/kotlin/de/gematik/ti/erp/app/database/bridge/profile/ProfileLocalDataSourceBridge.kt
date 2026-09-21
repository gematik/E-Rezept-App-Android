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

package de.gematik.ti.erp.app.database.bridge.profile

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class ProfileLocalDataSourceBridge(
    private val profileLocalDataSourceV1: ProfileLocalDataSource,
    private val profileLocalDataSourceV2: ProfileLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : ProfileLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun loadProfiles(): Flow<List<ProfileErpModel>> {
        return when {
            useRoom -> profileLocalDataSourceV2.loadProfiles()
            else -> profileLocalDataSourceV1.loadProfiles()
        }
    }

    override fun activeProfile(): Flow<ProfileErpModel?> {
        return when {
            useRoom -> profileLocalDataSourceV2.activeProfile()
            else -> profileLocalDataSourceV1.activeProfile()
        }
    }

    override fun getProfileById(profileId: String): Flow<ProfileErpModel?> {
        return when {
            useRoom -> profileLocalDataSourceV2.getProfileById(profileId)
            else -> profileLocalDataSourceV1.getProfileById(profileId)
        }
    }

    override suspend fun saveProfile(profile: ProfileErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.saveProfile(profile)
            else -> profileLocalDataSourceV1.saveProfile(profile)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun deleteProfile(profileId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.deleteProfile(profileId)
            else -> profileLocalDataSourceV1.deleteProfile(profileId)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun activateProfile(profileId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.activateProfile(profileId)
            else -> profileLocalDataSourceV1.activateProfile(profileId)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun deactivateAllProfiles() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.deactivateAllProfiles()
            else -> profileLocalDataSourceV1.deactivateAllProfiles()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateProfileName(profileId: String, profileName: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateProfileName(profileId, profileName)
            else -> profileLocalDataSourceV1.updateProfileName(profileId, profileName)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateProfileColor(profileId: String, color: ProfileColorNames) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateProfileColor(profileId, color)
            else -> profileLocalDataSourceV1.updateProfileColor(profileId, color)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun saveAvatarFigure(profileId: String, avatar: Avatar) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.saveAvatarFigure(profileId, avatar)
            else -> profileLocalDataSourceV1.saveAvatarFigure(profileId, avatar)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun savePersonalizedProfileImage(profileId: String, profileImage: ByteArray) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.savePersonalizedProfileImage(profileId, profileImage)
            else -> profileLocalDataSourceV1.savePersonalizedProfileImage(profileId, profileImage)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun clearPersonalizedProfileImage(profileId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.clearPersonalizedProfileImage(profileId)
            else -> profileLocalDataSourceV1.clearPersonalizedProfileImage(profileId)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateInsuranceInformation(
        profileId: String,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateInsuranceInformation(
                profileId,
                insurantName,
                insuranceIdentifier,
                organizationIdentifier,
                insuranceName
            )

            else -> profileLocalDataSourceV1.updateInsuranceInformation(
                profileId,
                insurantName,
                insuranceIdentifier,
                organizationIdentifier,
                insuranceName
            )
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateInsuranceType(profileId: String, insuranceType: InsuranceType) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateInsuranceType(profileId, insuranceType)
            else -> profileLocalDataSourceV1.updateInsuranceType(profileId, insuranceType)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateOrganizationIdentifier(profileId: String, organizationIdentifier: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateOrganizationIdentifier(profileId, organizationIdentifier)
            else -> profileLocalDataSourceV1.updateOrganizationIdentifier(profileId, organizationIdentifier)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateLastAuthenticated(profileId: String, lastAuthenticated: Instant) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateLastAuthenticated(profileId, lastAuthenticated)
            else -> profileLocalDataSourceV1.updateLastAuthenticated(profileId, lastAuthenticated)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun updateLastTaskSynced(profileId: String, lastTaskSynced: Instant) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> profileLocalDataSourceV2.updateLastTaskSynced(profileId, lastTaskSynced)
            else -> profileLocalDataSourceV1.updateLastTaskSynced(profileId, lastTaskSynced)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean {
        val operationName = getCurrentMethodName()
        val result = if (useRoom) {
            profileLocalDataSourceV2.wasProfileEverAuthenticated(profileId)
        } else {
            profileLocalDataSourceV1.wasProfileEverAuthenticated(profileId)
        }
        /*
        logger.logOperation(operationName, useRoom) */
        return result
    }
}
