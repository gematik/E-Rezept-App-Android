/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.mocks

import kotlinx.datetime.Instant

const val PROFILE_ID = "profile-id-1"
const val TASK_ID = "task-id-1"
val DATE_2024_01_01 = Instant.parse("2024-01-01T10:00:00Z")
val DATE_3024_01_01 = Instant.parse("3024-01-01T10:00:00Z")
val DATE_3023_12_31 = Instant.parse("3023-12-31T10:00:00Z")
