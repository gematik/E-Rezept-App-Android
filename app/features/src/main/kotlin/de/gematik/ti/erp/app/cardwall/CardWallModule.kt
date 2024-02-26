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

package de.gematik.ti.erp.app.cardwall

import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallLoadNfcPositionUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.cardwall.usecase.MiniCardWallUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val cardWallModule = DI.Module("cardWallModule") {
    bindProvider { AuthenticationUseCase(instance()) }
    bindProvider { CardWallLoadNfcPositionUseCase(instance()) }
    bindProvider { CardWallUseCase(instance(), instance()) }
    bindSingleton { MiniCardWallUseCase(instance(), instance()) }
    bindProvider { CardWallGraphController() }
}
