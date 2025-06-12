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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.RequestIntent
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType
import kotlinx.datetime.LocalDate

internal object FhirDeviceRequestErpTestData {
    val fhirTaskKbvDeviceRequestErpModelWithoutAccident = FhirTaskKbvDeviceRequestErpModel(
        id = "a1533e28-4631-4afa-b5e6-f233fad87f53",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "19205615",
        appName = "Vantis KHK und Herzinfarkt 001",
        accident = null,
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelWithAccident = FhirTaskKbvDeviceRequestErpModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "17850263",
        appName = "companion patella",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.WorkAccident,
            date = FhirTemporal.LocalDate(
                value = LocalDate.parse("2023-03-26"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            location = "Dummy-Betrieb"
        ),
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelType2 = FhirTaskKbvDeviceRequestErpModel(
        id = null,
        intent = RequestIntent.Order,
        status = "active",
        pzn = "17850263",
        appName = "companion patella",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.WorkAccident,
            date = FhirTemporal.LocalDate(
                value = LocalDate.parse("2023-03-26"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            location = "Dummy-Betrieb"
        ),
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelType3 = FhirTaskKbvDeviceRequestErpModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "17850263",
        appName = "companion patella",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.WorkAccident,
            date = FhirTemporal.LocalDate(
                value = LocalDate.parse("2023-03-26"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            location = "Dummy-Betrieb"
        ),
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelWithOccupationalDisease = FhirTaskKbvDeviceRequestErpModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "17622734",
        appName = "Mawendo 001",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.OccupationalDisease,
            date = null,
            location = null
        ),
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelTinnitus = FhirTaskKbvDeviceRequestErpModel(
        id = "d41f1c25-bf46-4226-aceb-9948ab2b5bdd",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "18053770",
        appName = "Meine Tinnitus App 001",
        accident = null,
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelWithoutSelfUse = FhirTaskKbvDeviceRequestErpModel(
        id = "d933d532-ecba-44f5-8a6d-c40376ffcf04",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "19205615",
        appName = "Vantis KHK und Herzinfarkt 001",
        accident = null,
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelWithInjury = FhirTaskKbvDeviceRequestErpModel(
        id = "a1533e28-4631-4afa-b5e6-f233fad87f53",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "19205615",
        appName = "Vantis KHK und Herzinfarkt 001",
        accident = FhirAccidentInformationErpModel(
            type = FhirTaskAccidentType.Accident,
            date = FhirTemporal.LocalDate(
                value = LocalDate.parse("2023-03-26"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            location = null
        ),
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )

    val fhirTaskKbvDeviceRequestErpModelWithDentist = FhirTaskKbvDeviceRequestErpModel(
        id = "625e0b13-3a43-43ee-98f2-be7f8539089d",
        intent = RequestIntent.Order,
        status = "active",
        pzn = "17946626",
        appName = "HelloBetter Schmerzen 001",
        accident = null,
        isSelfUse = false,
        authoredOn = FhirTemporal.LocalDate(
            value = LocalDate.parse("2023-03-26"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        isNew = true,
        isArchived = false
    )
}
