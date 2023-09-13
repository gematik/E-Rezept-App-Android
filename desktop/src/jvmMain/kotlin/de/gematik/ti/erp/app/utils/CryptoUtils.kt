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

package de.gematik.ti.erp.app.utils

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.prng.FixedSecureRandom
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun generateRandomAES256Key(seed: ByteArray): SecretKey =
    KeyGenerator.getInstance("AES").apply {
        init(256, secureRandomInstance(seed))
    }.generateKey()

fun secureRandomInstance(seed: ByteArray): SecureRandom =
    SP800SecureRandomBuilder(FixedSecureRandom(seed), false)
        .buildHash(SHA256Digest(), seed, false)
