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

package de.gematik.ti.erp.app.localization

import android.content.res.XmlResourceParser

interface XmlResourceParserWrapper {
    fun getEventType(): Int
    fun next(): Int

    fun name(): String

    fun getAttributeValue(): String?
}

class DefaultXmlResourceParserWrapper(
    private val parser: XmlResourceParser
) : XmlResourceParserWrapper {
    override fun getEventType(): Int = parser.eventType
    override fun next() = parser.next()
    override fun name(): String = parser.name
    override fun getAttributeValue(): String? =
        parser.getAttributeValue(
            "http://schemas.android.com/apk/res/android",
            "name"
        )
}

class MockXmlResourceParserWrapper : XmlResourceParserWrapper {

    private var eventType = 2
    private var isNextUsedOnce = false
    override fun getEventType(): Int = eventType

    override fun name(): String = "locale"

    override fun next(): Int {
        when {
            !isNextUsedOnce -> isNextUsedOnce = true
            else -> eventType = 1
        }
        return eventType
    }

    override fun getAttributeValue(): String = "de"
}
