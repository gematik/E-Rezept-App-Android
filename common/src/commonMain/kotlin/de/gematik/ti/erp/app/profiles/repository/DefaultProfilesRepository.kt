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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.database.realm.utils.deleteAll
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.AvatarFigureV1
import de.gematik.ti.erp.app.database.realm.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.database.realm.v1.ProfileColorNamesV1
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.mapper.toProfileData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant

sealed interface SetToActiveProfile {
    data object NoChange : SetToActiveProfile
    data object ChangeActiveState : SetToActiveProfile
}

class KVNRAlreadyAssignedException(
    message: String,
    val isActiveProfile: Boolean,
    val inProfile: String,
    val insuranceIdentifier: String
) : IllegalStateException(message)

private const val DEBUG_ORGANIZATION_IDENTIFIER = "104212505"

class DefaultProfilesRepository(
    private val realm: Realm,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProfileRepository {
    private val lock = Mutex()

    override fun profiles(): Flow<List<ProfilesData.Profile>> =
        realm.query<ProfileEntityV1>().asFlow().mapNotNull {
            val hasActiveProfile = it.list.any { profile -> profile.active }

            val state = if (it.list.size == 1 && !hasActiveProfile) {
                SetToActiveProfile.ChangeActiveState
            } else {
                SetToActiveProfile.NoChange
            }

            it.list.map { profile ->
                profile.toProfileData(state)
            }
        }.flowOn(dispatcher)

    override fun activeProfile(): Flow<ProfilesData.Profile> =
        profiles().mapNotNull {
            it.find { profile -> profile.active }
        }

    override suspend fun createNewProfile(profileName: String) {
        realm.write {
            query<ProfileEntityV1>().find().forEach {
                it.active = false
            }

            copyToRealm(
                ProfileEntityV1().apply {
                    this.name = profileName
                    this.active = true
                    this.color = ProfileColorNamesV1.entries.toTypedArray().random()
                }
            )
        }
    }

    override suspend fun activateProfile(profileId: ProfileIdentifier) {
        realm.write {
            query<ProfileEntityV1>("id != $0", profileId).find().forEach {
                it.active = false
            }
            query<ProfileEntityV1>("id = $0", profileId).first().find()?.apply {
                this.active = true
            }
        }
    }

    override suspend fun removeProfile(profileId: ProfileIdentifier, profileName: String) {
        lock.withLock {
            realm.writeBlocking {
                val profiles = query<ProfileEntityV1>().find()

                if (profiles.size == 1) {
                    // create new default profile before deleting the last profile
                    query<ProfileEntityV1>().find().forEach {
                        it.active = false
                    }
                    copyToRealm(
                        ProfileEntityV1().apply {
                            this.name = profileName
                            this.active = true
                            this.color = ProfileColorNamesV1.entries.toTypedArray().random()
                        }
                    )
                }

                queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profileToDelete ->
                    if (profileToDelete.active) {
                        profiles.query("id != $0", profileId).first().find()?.let {
                            findLatest(it)?.active = true
                        }
                    }
                    deleteAll(profileToDelete)
                }
            }
        }
    }

    override suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    ) {
        lock.withLock {
            realm.queryFirst<ProfileEntityV1>("insuranceIdentifier == $0 AND id != $1", insuranceIdentifier, profileId)
                ?.let {
                    throw KVNRAlreadyAssignedException(
                        "KVNR already assigned to another profile",
                        false,
                        it.name,
                        it.insuranceIdentifier!!
                    )
                }

            realm.queryFirst<ProfileEntityV1>(
                "insuranceIdentifier != NULL && insuranceIdentifier != $0 AND id == $1",
                insuranceIdentifier,
                profileId
            )
                ?.let {
                    throw KVNRAlreadyAssignedException(
                        "Profile already assigned to another KVNR",
                        true,
                        profileId,
                        it.insuranceIdentifier!!
                    )
                }

            realm.write {
                queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                    this.insuranceName = insuranceName
                    this.insuranceIdentifier = insuranceIdentifier
                    this.organizationIdentifier = organizationIdentifier
                    this.insurantName = insurantName
                    if (this.isNewlyCreated) {
                        this.name = insurantName
                    }
                }
            }
        }
    }

    override suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.name = profileName
                this.isNewlyCreated = false
            }
        }
    }

    override suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfilesData.ProfileColorNames) {
        realm.write<Unit> {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.color = when (color) {
                    ProfilesData.ProfileColorNames.SPRING_GRAY -> ProfileColorNamesV1.SPRING_GRAY
                    ProfilesData.ProfileColorNames.SUN_DEW -> ProfileColorNamesV1.SUN_DEW
                    ProfilesData.ProfileColorNames.PINK -> ProfileColorNamesV1.PINK
                    ProfilesData.ProfileColorNames.TREE -> ProfileColorNamesV1.TREE
                    ProfilesData.ProfileColorNames.BLUE_MOON -> ProfileColorNamesV1.BLUE_MOON
                }
            }
        }
    }

    override suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.lastAuthenticated = lastAuthenticated.toRealmInstant()
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.avatarFigure = when (avatar) {
                    ProfilesData.Avatar.PersonalizedImage -> AvatarFigureV1.PersonalizedImage
                    ProfilesData.Avatar.FemaleDoctor -> AvatarFigureV1.FemaleDoctor
                    ProfilesData.Avatar.WomanWithHeadScarf -> AvatarFigureV1.WomanWithHeadScarf
                    ProfilesData.Avatar.Grandfather -> AvatarFigureV1.Grandfather
                    ProfilesData.Avatar.BoyWithHealthCard -> AvatarFigureV1.BoyWithHealthCard
                    ProfilesData.Avatar.OldManOfColor -> AvatarFigureV1.OldManOfColor
                    ProfilesData.Avatar.WomanWithPhone -> AvatarFigureV1.WomanWithPhone
                    ProfilesData.Avatar.Grandmother -> AvatarFigureV1.Grandmother
                    ProfilesData.Avatar.ManWithPhone -> AvatarFigureV1.ManWithPhone
                    ProfilesData.Avatar.WheelchairUser -> AvatarFigureV1.WheelchairUser
                    ProfilesData.Avatar.Baby -> AvatarFigureV1.Baby
                    ProfilesData.Avatar.MaleDoctorWithPhone -> AvatarFigureV1.MaleDoctorWithPhone
                    ProfilesData.Avatar.FemaleDoctorWithPhone -> AvatarFigureV1.FemaleDoctorWithPhone
                    ProfilesData.Avatar.FemaleDeveloper -> AvatarFigureV1.FemaleDeveloper
                }
            }
        }
    }

    override suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.personalizedImage = profileImage
            }
        }
    }

    override suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.personalizedImage = null
                this.avatarFigure = AvatarFigureV1.PersonalizedImage
            }
        }
    }

    override suspend fun switchProfileToPKV(profileId: ProfileIdentifier): Boolean {
        val entity = realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.insuranceType = InsuranceTypeV1.PKV
            }
        }
        return entity?.insuranceType == InsuranceTypeV1.PKV
    }

    override suspend fun switchProfileToGKV(profileId: ProfileIdentifier): Boolean {
        val entity = realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.insuranceType = InsuranceTypeV1.GKV
            }
        }
        return entity?.insuranceType == InsuranceTypeV1.GKV
    }

    override suspend fun switchProfileToBUND(profileId: ProfileIdentifier): Boolean {
        val entity = realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.insuranceType = InsuranceTypeV1.BUND
            }
        }
        return entity?.insuranceType == InsuranceTypeV1.BUND
    }

    override suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean =
        getProfileById(profileId).first().insuranceType == ProfilesData.InsuranceType.PKV

    override fun getProfileById(
        profileId: ProfileIdentifier
    ): Flow<ProfilesData.Profile> =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileId)?.asFlow()?.mapNotNull {
            it.obj?.toProfileData()
        } ?: emptyFlow()

    override suspend fun isSsoTokenValid(profileId: ProfileIdentifier): Flow<Boolean> =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileId)
            ?.asFlow()?.mapNotNull { it.obj?.toProfileData() }
            ?.map { it.isSSOTokenValid() } ?: flowOf(false)

    override suspend fun getOrganizationIdentifier(profileId: ProfileIdentifier): Flow<String> =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileId)?.asFlow()?.mapNotNull {
            it.obj?.organizationIdentifier
        } ?: emptyFlow()

    override suspend fun updateOrganizationIdentifier(iknr: String) {
        activeProfile()
            .first()
            .let { activeProfile ->
                realm.write {
                    queryFirst<ProfileEntityV1>("id = $0", activeProfile.id)?.apply {
                        this.organizationIdentifier = iknr
                    }
                }
            }
    }
}
