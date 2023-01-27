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

package de.gematik.ti.erp.app.core

import io.github.aakira.napier.Napier
import okhttp3.logging.HttpLoggingInterceptor

class NapierLogger() : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Napier.d(message)
    }
}

class PrefixedLogger(val prefix: String) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Napier.d("[$prefix] $message")
    }
}
