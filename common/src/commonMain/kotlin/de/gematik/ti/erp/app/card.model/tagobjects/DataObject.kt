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

package de.gematik.ti.erp.app.cardwall.model.nfc.tagobjects

import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject

private const val DO_87_TAG = 0x07
private const val DO_81_EXTRACTED_TAG = 0x81
private const val DO_81_TAG = 0x01

/**
 * Data object with TAG 87
 *
 * @param data byte array with extracted data from plain CommandApdu or encrypted ResponseApdu
 * @param tag int with extracted tag number
 */
class DataObject(val data: ByteArray, val tag: Byte = 0) {
    val taggedObject: DERTaggedObject
        get() =
            if (tag == DO_81_EXTRACTED_TAG.toByte()) {
                DERTaggedObject(false, DO_81_TAG, DEROctetString(data))
            } else {
                DERTaggedObject(false, DO_87_TAG, DEROctetString(data))
            }
}
