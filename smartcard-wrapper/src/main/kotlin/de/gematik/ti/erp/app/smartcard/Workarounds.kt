/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.smartcard

internal object Workarounds {
    // workaround for https://bugs.openjdk.java.net/browse/JDK-8255877
    fun `workaround for MacOSX Big Sur And Monterey - PCSC not found bug`() {
        val javaVersion = System.getProperty("java.version")
        val majorJavaVersion = javaVersion.substring(0, javaVersion.indexOf('.')).toInt()
        val osVersion = System.getProperty("os.version")
        val osName = System.getProperty("os.name")
        val majorOsVersion = osVersion.substring(0, osVersion.indexOf('.')).toInt()
        if (osName == "Mac OS X" && majorJavaVersion <= 16 && (majorOsVersion == 11 || majorOsVersion == 12)) {
            System.setProperty(
                "sun.security.smartcardio.library",
                "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC"
            )
        }
    }
}
