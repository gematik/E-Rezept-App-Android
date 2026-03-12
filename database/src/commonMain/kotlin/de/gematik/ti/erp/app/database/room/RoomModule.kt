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

package de.gematik.ti.erp.app.database.room

import de.gematik.ti.erp.app.database.room.v2.accesstoken.SearchAccessTokenDao
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyDao
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileDao
import de.gematik.ti.erp.app.database.room.v2.settings.SettingsDao
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoDao
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskMultiplePrescriptionDao
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.truststrore.TrustStoreDao
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val roomModule = DI.Module("roomModule") {
    bindSingleton<AppDatabase> { buildAppDatabase() }

    // Expose DAOs
    bindSingleton<ProfileDao> { instance<AppDatabase>().profileDao() }
    bindSingleton<PharmacyDao> { instance<AppDatabase>().pharmacyFavoriteDao() }
    bindSingleton<SettingsDao> { instance<AppDatabase>().settingsDao() }
    bindSingleton<InvoiceDao> { instance<AppDatabase>().invoiceDao() }
    bindSingleton<ErpTaskDao> { instance<AppDatabase>().taskDao() }
    bindSingleton<ErpTaskWithRefsDao> { instance<AppDatabase>().taskWithRefsDao() }
    bindSingleton<CommunicationDao> { instance<AppDatabase>().communicationDao() }
    bindSingleton<MedicationDispenseDao> { instance<AppDatabase>().medicationDispenseDao() }
    bindSingleton<OrganizationDao> { instance<AppDatabase>().organizationDao() }
    bindSingleton<TrustStoreDao> { instance<AppDatabase>().trustStoreDao() }
    bindSingleton<SearchAccessTokenDao> { instance<AppDatabase>().searchAccessTokenDao() }
    bindSingleton<ShippingInfoDao> { instance<AppDatabase>().shippingInfoDao() }
    bindSingleton<ErpTaskMultiplePrescriptionDao> { instance<AppDatabase>().erpTaskMultiplePrescriptionDao() }
}
