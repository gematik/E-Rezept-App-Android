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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp

object DependencyInjector {

    fun accompanist(init: Dependencies.Accompanist.() -> Unit) =
        Dependencies.Accompanist.init()

    fun android(init: Dependencies.Android.() -> Unit) =
        Dependencies.Android.init()

    fun androidX(init: Dependencies.AndroidX.() -> Unit) =
        Dependencies.AndroidX.init()

    fun androidXTest(init: Dependencies.AndroidX.Test.() -> Unit) =
        Dependencies.AndroidX.Test.init()

    fun compose(init: Dependencies.Compose.() -> Unit) =
        Dependencies.Compose.init()

    fun composeTest(init: Dependencies.Compose.Test.() -> Unit) =
        Dependencies.Compose.Test.init()

    fun coroutines(init: Dependencies.Coroutines.() -> Unit) =
        Dependencies.Coroutines.init()

    fun coroutinesTest(init: Dependencies.Coroutines.Test.() -> Unit) =
        Dependencies.Coroutines.Test.init()

    fun dependencyInjection(init: Dependencies.DependencyInjection.() -> Unit) =
        Dependencies.DependencyInjection.init()

    fun dataMatrix(init: Dependencies.DataMatrix.() -> Unit) =
        Dependencies.DataMatrix.init()

    fun dateTime(init: Dependencies.Datetime.() -> Unit) =
        Dependencies.Datetime.init()

    fun playServices(init: Dependencies.PlayServices.() -> Unit) =
        Dependencies.PlayServices.init()

    fun logging(init: Dependencies.Logging.() -> Unit) =
        Dependencies.Logging.init()

    fun serialization(init: Dependencies.Serialization.() -> Unit) =
        Dependencies.Serialization.init()

    fun shimmer(init: Dependencies.Shimmer.() -> Unit) =
        Dependencies.Shimmer.init()

    fun crypto(init: Dependencies.Crypto.() -> Unit) =
        Dependencies.Crypto.init()

    fun material(init: Dependencies.ComposeMaterial.() -> Unit) =
        Dependencies.ComposeMaterial.init()
    fun network(init: Dependencies.Network.() -> Unit) =
        Dependencies.Network.init()

    fun networkTest(init: Dependencies.Network.Test.() -> Unit) =
        Dependencies.Network.Test.init()

    fun database(init: Dependencies.Database.() -> Unit) =
        Dependencies.Database.init()

    fun imageLoad(init: Dependencies.Coil.() -> Unit) =
        Dependencies.Coil.init()

    fun passwordStrength(init: Dependencies.PasswordStrength.() -> Unit) =
        Dependencies.PasswordStrength.init()

    fun stateManagement(init: Dependencies.StateManagement.() -> Unit) =
        Dependencies.StateManagement.init()

    fun tracking(init: Dependencies.Tracking.() -> Unit) =
        Dependencies.Tracking.init()

    fun test(init: Dependencies.Test.() -> Unit) =
        Dependencies.Test.init()

    fun lottie(init: Dependencies.Lottie.() -> Unit) =
        Dependencies.Lottie.init()

    // Should move to open-street-maps
    fun maps(init: Dependencies.GoogleMaps.() -> Unit) =
        Dependencies.GoogleMaps.init()

    fun tracing(init: Dependencies.AndroidX.Tracing.() -> Unit) =
        Dependencies.AndroidX.Tracing.init()
}

fun inject(init: DependencyInjector.() -> Unit) = DependencyInjector.init()
