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

package de.gematik.ti.erp.app.idp.usecase

import javax.inject.Inject

class IdpDeviceInfoProvider @Inject constructor() {
    val deviceName: String = "Some Android"
    val manufacturer: String = android.os.Build.MANUFACTURER
    val productName: String = android.os.Build.PRODUCT
    val model: String = android.os.Build.MODEL
    val operatingSystem: String = "Android"
    val operatingSystemVersion: String = android.os.Build.VERSION.SDK_INT.toString()
}
