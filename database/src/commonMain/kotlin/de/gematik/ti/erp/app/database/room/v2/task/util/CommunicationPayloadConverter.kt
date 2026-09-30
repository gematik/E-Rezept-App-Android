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

package de.gematik.ti.erp.app.database.room.v2.task.util

import androidx.room.TypeConverter
import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationPayloadParser
import de.gematik.ti.erp.app.fhir.constant.SafeJson

class CommunicationPayloadConverter {
    @TypeConverter
    fun fromPayload(payload: CommunicationPayloadErpModel?): String? =
        payload?.let { SafeJson.value.encodeToString(CommunicationPayloadErpModel.serializer(), it) }

    @TypeConverter
    fun toPayload(value: String?): CommunicationPayloadErpModel? =
        value?.let {
            // Fast path: the column already holds the structured erp-model JSON written by [fromPayload]
            // (polymorphic, with a "type" discriminator), so a plain deserialize is enough here - no need to
            // re-run the raw-backend-payload parsing/disambiguation logic on every read.
            runCatching {
                SafeJson.value.decodeFromString(CommunicationPayloadErpModel.serializer(), it)
            }.getOrElse {
                // Legacy fallback: DBs created before the CommResV3 refactor (Room migration 11->12) may
                // still contain the original, un-parsed backend payload string in this column, since
                // migrations copy the column's raw value across schema versions without transforming it.
                // Only those rows need the full parser.
                CommunicationPayloadParser.extract(value)
            }
        }
}
