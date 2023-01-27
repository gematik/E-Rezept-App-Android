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

package de.gematik.ti.erp.app.protocol

import de.gematik.ti.erp.app.protocol.repository.AuditEventLocalDataSource
import de.gematik.ti.erp.app.protocol.repository.AuditEventRemoteDataSource
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val protocolModule = DI.Module("protocolModule") {
    bindProvider { AuditEventsRepository(instance(), instance(), instance()) }
    bindProvider { AuditEventLocalDataSource(instance()) }
    bindProvider { AuditEventRemoteDataSource(instance()) }
}
