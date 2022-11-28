/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.card.model.command

import de.gematik.ti.erp.app.card.model.identifier.ApplicationIdentifier
import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier

private const val CLA = 0x00
private const val INS = 0xA4
private const val SELECTION_MODE_DF_BY_FID = 0x01
private const val SELECTION_MODE_EF_BY_FID = 0x02
private const val SELECTION_MODE_PARENT = 0x03
private const val SELECTION_MODE_AID = 0x04
private const val RESPONSE_TYPE_NO_RESPONSE = 0x0C
private const val RESPONSE_TYPE_FCP = 0x04
private const val FILE_OCCURRENCE_FIRST = 0x00
private const val FILE_OCCURRENCE_NEXT = 0x02
private const val P2_FCP = 0x04
private const val P2 = 0x0C

// Note: Left out use case Select parent folder requesting File Control Parameter gemSpec_Cos#14.2.6.12
// Note: Left out use case Select parent folder requesting File Control Parameter gemSpec_Cos#14.2.6.14
private fun calculateP2(requestFCP: Boolean, nextOccurrence: Boolean): Int =
    if (requestFCP) {
        RESPONSE_TYPE_FCP
    } else {
        RESPONSE_TYPE_NO_RESPONSE
    } + if (nextOccurrence) {
        FILE_OCCURRENCE_NEXT
    } else {
        FILE_OCCURRENCE_FIRST
    }

/**
 * Commands representing Select Command gemSpec_COS#14.2.6
 */

/**
 * Use case Select root of object system gemSpec_Cos#14.2.6.1 + use case Select parent folder gemSpec_Cos#14.2.6.11
 * Use case Select root of object system requesting File Control Parameter gemSpec_Cos#14.2.6.2 with Parameter readFirst true
 *
 * @param selectParentElseRoot if true SELECTION_MODE_PARENT else SELECTION_MODE_AID
 * @param readFirst if true read FCP else only select
 */
fun HealthCardCommand.Companion.select(selectParentElseRoot: Boolean, readFirst: Boolean) =
    HealthCardCommand(
        expectedStatus = selectStatus,
        cla = CLA,
        ins = INS,
        p1 = if (selectParentElseRoot) SELECTION_MODE_PARENT else SELECTION_MODE_AID,
        p2 = calculateP2(readFirst, false),
        ne = if (readFirst) EXPECT_ALL_WILDCARD else null
    )

// Note: Left out use cases Select without Application Identifier, next gemSpec_Cos#14.2.6.3 - 14.2.6.4
/**
 * Use case Select file with Application Identifier, first occurrence, no File Control Parameter gemSpec_Cos#14.2.6.5
 *
 * @param aid
 */
fun HealthCardCommand.Companion.select(aid: ApplicationIdentifier) =
    HealthCardCommand.select(
        aid,
        selectNextElseFirstOccurrence = false,
        requestFcp = false,
        fcpLength = 0
    )

/**
 * Use cases Select file with Application Identifier gemSpec_Cos#14.2.6.5 - 14.2.6.8
 *
 * @param fcpLength determine expected size of response if File Control Parameter requested
 */
fun HealthCardCommand.Companion.select(
    aid: ApplicationIdentifier,
    selectNextElseFirstOccurrence: Boolean,
    requestFcp: Boolean,
    fcpLength: Int
) =
    HealthCardCommand(
        expectedStatus = selectStatus,
        cla = CLA,
        ins = INS,
        p1 = SELECTION_MODE_AID,
        p2 = calculateP2(requestFcp, selectNextElseFirstOccurrence),
        data = aid.aid,
        ne = if (requestFcp) fcpLength else null
    )

/**
 * Use case Select DF with File Identifier gemSpec_Cos#14.2.6.9 and
 * use case Select EF with File Identifier gemSpec_Cos#14.2.6.13
 */
fun HealthCardCommand.Companion.select(fid: FileIdentifier, selectDfElseEf: Boolean) =
    HealthCardCommand.select(fid, selectDfElseEf, false, 0)

/**
 * Use cases Select DF with File Identifier gemSpec_Cos#14.2.6.9 - 14.2.6.10 and
 * use cases Select EF with File Identifier gemSpec_Cos#14.2.6.13 - 14.2.6.14
 *
 * @param selectDfElseEf true if Dedicated File shall be selected, false if Elementary File shall be selected
 * @param fcpLength determine expected size of response if File Control Parameter requested
 */
fun HealthCardCommand.Companion.select(
    fid: FileIdentifier,
    selectDfElseEf: Boolean,
    requestFcp: Boolean,
    fcpLength: Int
) =
    HealthCardCommand(
        expectedStatus = selectStatus,
        cla = CLA,
        ins = INS,
        p1 = if (selectDfElseEf) SELECTION_MODE_DF_BY_FID else SELECTION_MODE_EF_BY_FID,
        p2 = if (requestFcp) P2_FCP else P2,
        data = fid.getFid(),
        ne = if (requestFcp) fcpLength else null
    )
