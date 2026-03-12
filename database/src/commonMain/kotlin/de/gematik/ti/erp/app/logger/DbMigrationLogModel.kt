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

package de.gematik.ti.erp.app.logger

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.OperationNoCheck
import kotlinx.serialization.Serializable
import java.util.UUID

enum class DbMigrationExpandedState {
    CLOSED,
    OPEN
}

@Serializable
sealed class DbComparisonState(val name: String) {
    object NoData : DbComparisonState("no data")
    object OnlyRealm : DbComparisonState("only realm")
    object OnlyRoom : DbComparisonState("only room")
    object RealmAndRoom : DbComparisonState("realm and room")
}

@Serializable
sealed class DbMigrationFunctionalState(val name: String) {
    @Serializable
    object ChecksVersionsForSameModel : DbMigrationFunctionalState("same model")

    @Serializable
    object CheckFunctionalityForDifferentModels : DbMigrationFunctionalState("different models")

    @Serializable
    object NewModelInRoom : DbMigrationFunctionalState("new model")

    @Serializable
    object OperationNoCheck : DbMigrationFunctionalState("only operation")
}

@Serializable
data class DbMigrationLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = System.currentTimeMillis().toString(),
    val operation: String,
    val usesRoom: Boolean = false,
    val expandedState: DbMigrationExpandedState = DbMigrationExpandedState.CLOSED,
    val functionalState: DbMigrationFunctionalState = OperationNoCheck,
    val realmData: String? = null,
    val roomData: String? = null
) {
    companion object {
        fun DbMigrationLogEntry.checkVersions(): Boolean? {
            return if (realmRoomComparison() is DbComparisonState.RealmAndRoom) {
                realmData == roomData
            } else {
                null
            }
        }

        fun DbMigrationLogEntry.toJson(): String {
            return SafeJson.value.encodeToString(this)
        }

        fun DbMigrationLogEntry.realmRoomComparison(): DbComparisonState {
            val isRealmEmpty = realmData.isNullOrEmpty() || realmData == "[]"
            val isRoomEmpty = roomData.isNullOrEmpty() || roomData == "[]"

            return when {
                isRealmEmpty && isRoomEmpty -> DbComparisonState.NoData
                isRealmEmpty && !isRoomEmpty -> DbComparisonState.OnlyRoom
                !isRealmEmpty && isRoomEmpty -> DbComparisonState.OnlyRealm
                else -> DbComparisonState.RealmAndRoom
            }
        }
    }
}
