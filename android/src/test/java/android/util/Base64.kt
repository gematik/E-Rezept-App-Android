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

@file:JvmName("Base64")

package android.util

fun decode(input: ByteArray, flag: Int): ByteArray {
    return java.util.Base64.getDecoder().decode(input)
}

fun decode(input: String, flag: Int): ByteArray {
    return when (flag) {
        0 -> java.util.Base64.getDecoder().decode(input)
        8 -> java.util.Base64.getUrlDecoder().decode(input)
        else -> java.util.Base64.getUrlDecoder().decode(input)
    }
}
