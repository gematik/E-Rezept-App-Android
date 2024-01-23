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

package de.gematik.ti.erp.app

import android.os.Build
import java.security.SecureRandom
@Requirement(
    "O.Rand_1",
    "O.Rand_2",
    "O.Rand_3",
    "O.Rand_4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Generation of random values by secure random generator specified in FIPS 140-2, " +
        "Security Requirements for Cryptographic Modules, section 4.9.1."
)
actual fun secureRandomInstance(): SecureRandom =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SecureRandom.getInstanceStrong()
    } else {
        SecureRandom()
    }
