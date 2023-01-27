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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.deleteAll
import de.gematik.ti.erp.app.db.entities.v1.AvatarFigureV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileColorNamesV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.idp.repository.toSingleSignOnTokenScope
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

typealias ProfileIdentifier = String

class KVNRAlreadyAssignedException(
    message: String,
    val isActiveProfile: Boolean,
    val inProfile: String,
    val insuranceIdentifier: String
) : IllegalStateException(message)

class ProfilesRepository constructor(
    private val dispatchers: DispatchProvider,
    private val realm: Realm
) {
    private val lock = Mutex()

    fun profiles() =
        realm.query<ProfileEntityV1>().asFlow().mapNotNull {
            val hasActiveProfile = it.list.any { profile -> profile.active }

            it.list.mapIndexed { index, profile ->
                ProfilesData.Profile(
                    id = profile.id,
                    color = when (profile.color) {
                        ProfileColorNamesV1.SPRING_GRAY -> ProfilesData.ProfileColorNames.SPRING_GRAY
                        ProfileColorNamesV1.SUN_DEW -> ProfilesData.ProfileColorNames.SUN_DEW
                        ProfileColorNamesV1.PINK -> ProfilesData.ProfileColorNames.PINK
                        ProfileColorNamesV1.TREE -> ProfilesData.ProfileColorNames.TREE
                        ProfileColorNamesV1.BLUE_MOON -> ProfilesData.ProfileColorNames.BLUE_MOON
                    },
                    avatarFigure = when (profile.avatarFigure) {
                        AvatarFigureV1.PersonalizedImage -> ProfilesData.AvatarFigure.PersonalizedImage
                        AvatarFigureV1.FemaleDoctor -> ProfilesData.AvatarFigure.FemaleDoctor
                        AvatarFigureV1.WomanWithHeadScarf -> ProfilesData.AvatarFigure.WomanWithHeadScarf
                        AvatarFigureV1.Grandfather -> ProfilesData.AvatarFigure.Grandfather
                        AvatarFigureV1.BoyWithHealthCard -> ProfilesData.AvatarFigure.BoyWithHealthCard
                        AvatarFigureV1.OldManOfColor -> ProfilesData.AvatarFigure.OldManOfColor
                        AvatarFigureV1.WomanWithPhone -> ProfilesData.AvatarFigure.WomanWithPhone
                        AvatarFigureV1.Grandmother -> ProfilesData.AvatarFigure.Grandmother
                        AvatarFigureV1.ManWithPhone -> ProfilesData.AvatarFigure.ManWithPhone
                        AvatarFigureV1.WheelchairUser -> ProfilesData.AvatarFigure.WheelchairUser
                        AvatarFigureV1.Baby -> ProfilesData.AvatarFigure.Baby
                        AvatarFigureV1.MaleDoctorWithPhone -> ProfilesData.AvatarFigure.MaleDoctorWithPhone
                        AvatarFigureV1.FemaleDoctorWithPhone -> ProfilesData.AvatarFigure.FemaleDoctorWithPhone
                        AvatarFigureV1.FemaleDeveloper -> ProfilesData.AvatarFigure.FemaleDeveloper
                    },
                    personalizedImage = profile.personalizedImage,
                    name = profile.name,
                    insurantName = profile.insurantName ?: "",
                    insuranceIdentifier = profile.insuranceIdentifier,
                    insuranceName = profile.insuranceName,
                    lastAuthenticated = profile.lastAuthenticated?.toInstant(),
                    lastAuditEventSynced = profile.lastAuditEventSynced?.toInstant(),
                    lastTaskSynced = profile.lastTaskSynced?.toInstant(),
                    // TODO change architecture of active profile
                    active = if (!hasActiveProfile && index == 0) {
                        true
                    } else {
                        profile.active
                    },
                    singleSignOnTokenScope = profile.idpAuthenticationData?.toSingleSignOnTokenScope()

                )
            }
        }.flowOn(dispatchers.Main)

    suspend fun saveProfile(profileName: String, activate: Boolean) {
        realm.write {
            if (activate) {
                query<ProfileEntityV1>().find().forEach {
                    it.active = false
                }
            }
            copyToRealm(
                ProfileEntityV1().apply {
                    this.name = profileName
                    this.active = activate
                    this.color = ProfileColorNamesV1.values().random()
                }
            )
        }
    }

    // tag::SwitchActiveProfileRepository[]
    suspend fun activateProfile(profileId: ProfileIdentifier) {
        realm.write {
            query<ProfileEntityV1>("id != $0", profileId).find().forEach {
                it.active = false
            }
            query<ProfileEntityV1>("id = $0", profileId).first().find()?.apply {
                this.active = true
            }
        }
    }
    // end::SwitchActiveProfileRepository[]

    suspend fun removeProfile(profileId: ProfileIdentifier) {
        lock.withLock {
            realm.writeBlocking {
                val profiles = query<ProfileEntityV1>().find()

                if (profiles.size == 1) {
                    error("Can't remove the last profile!")
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

    suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
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
                    this.insurantName = insurantName
                }
            }
        }
    }

    suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.name = profileName
            }
        }
    }

    suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfilesData.ProfileColorNames) {
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

    suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.lastAuthenticated = lastAuthenticated.toRealmInstant()
            }
        }
    }

    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatarFigure: ProfilesData.AvatarFigure) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.avatarFigure = when (avatarFigure) {
                    ProfilesData.AvatarFigure.PersonalizedImage -> AvatarFigureV1.PersonalizedImage
                    ProfilesData.AvatarFigure.FemaleDoctor -> AvatarFigureV1.FemaleDoctor
                    ProfilesData.AvatarFigure.WomanWithHeadScarf -> AvatarFigureV1.WomanWithHeadScarf
                    ProfilesData.AvatarFigure.Grandfather -> AvatarFigureV1.Grandfather
                    ProfilesData.AvatarFigure.BoyWithHealthCard -> AvatarFigureV1.BoyWithHealthCard
                    ProfilesData.AvatarFigure.OldManOfColor -> AvatarFigureV1.OldManOfColor
                    ProfilesData.AvatarFigure.WomanWithPhone -> AvatarFigureV1.WomanWithPhone
                    ProfilesData.AvatarFigure.Grandmother -> AvatarFigureV1.Grandmother
                    ProfilesData.AvatarFigure.ManWithPhone -> AvatarFigureV1.ManWithPhone
                    ProfilesData.AvatarFigure.WheelchairUser -> AvatarFigureV1.WheelchairUser
                    ProfilesData.AvatarFigure.Baby -> AvatarFigureV1.Baby
                    ProfilesData.AvatarFigure.MaleDoctorWithPhone -> AvatarFigureV1.MaleDoctorWithPhone
                    ProfilesData.AvatarFigure.FemaleDoctorWithPhone -> AvatarFigureV1.FemaleDoctorWithPhone
                    ProfilesData.AvatarFigure.FemaleDeveloper -> AvatarFigureV1.FemaleDeveloper
                }
            }
        }
    }

    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.personalizedImage = profileImage
            }
        }
    }

    suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier) {
        realm.write {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.apply {
                this.personalizedImage = null
                this.avatarFigure = AvatarFigureV1.PersonalizedImage
            }
        }
    }
}
