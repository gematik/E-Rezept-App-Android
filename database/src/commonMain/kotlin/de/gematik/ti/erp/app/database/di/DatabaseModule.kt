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
package de.gematik.ti.erp.app.database.di

import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.api.IdpConfigurationLocalDataSource
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.database.bridge.InternalMessageLocalDataBridge
import de.gematik.ti.erp.app.database.bridge.appauthentication.AppAuthenticationLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.eurezept.EuTaskLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.idp.IdpConfigurationLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.invoice.InvoiceLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.medicationplan.MedicationPlanLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.pharmacy.PharmacyLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.pharmacy.PharmacySearchAccessTokenLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.profile.ProfileLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.settings.SettingsLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.shipping.ShippingInfoLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.task.TaskLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.task.communication.CommunicationLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.truststore.TrustStoreLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.userauthentication.UserAuthenticationLocalDataSourceBridge
import de.gematik.ti.erp.app.database.datastore.appauthentication.AppAuthenticationLocalDataSourceV2
import de.gematik.ti.erp.app.database.datastore.featuretoggle.IsRoomEnabled
import de.gematik.ti.erp.app.database.datastore.settings.SettingsLocalDataSourceV2
import de.gematik.ti.erp.app.database.migration.DataMigrator
import de.gematik.ti.erp.app.database.migration.DefaultDataMigrator
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AppAuthenticationLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.eurezept.EuTaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.idp.IdpConfigurationLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.internalmessage.InternalMessagesLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.InvoiceLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationPlanLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.PharmacyLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.PharmacySearchAccessTokenLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.profile.ProfileLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.settings.SettingsLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.shipping.ShippingInfoLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.task.communication.CommunicationLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.task.datasource.TaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.truststore.TrustStoreLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.userauthentication.UserAuthenticationLocalDataSourceV1
import de.gematik.ti.erp.app.database.room.AppDatabase
import de.gematik.ti.erp.app.database.room.roomModule
import de.gematik.ti.erp.app.database.room.v2.accesstoken.PharmacySearchAccessTokenLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.eurezept.EuTaskLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.idp.IdpConfigurationLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.internalmessage.InternalMessagesLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.medicationplan.MedicationPlanLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.task.TaskLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.truststore.TrustStoreLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.userAuthentication.UserAuthenticationLocalDataSourceV2
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Provides Kodein bindings for task-related local data sources, supporting both legacy (V1)
 * and new (V2) implementations. In debug mode, it wires a bridge implementation that compares
 * data between V1 and V2 for validation and migration purposes.
 *
 * @param IsRoomEnabled Indicates whether the app is running in debug mode. If true, a bridge
 * implementation (`LocalDataSourceBridge`) is bound to the default `LocalDataSource`
 * interface to enable runtime comparison between V1 and V2. In release mode, only V1 is expected
 * to be used.
 *
 * @return A Kodein `DI.Module` containing bindings for V1, V2, and bridge data sources.
 */
fun databaseModule() = DI.Module("databaseModule", allowSilentOverride = true) {
    // task module
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V1) { TaskLocalDataSourceV1(instance()) }
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V2) {
        TaskLocalDataSourceV2(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider<TaskLocalDataSource> {
        TaskLocalDataSourceBridge(
            instance(tag = ModuleTags.TASK_V1),
            instance(tag = ModuleTags.TASK_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // medicationPlan module
    bindProvider<MedicationPlanLocalDataSource>(tag = ModuleTags.MEDICATION_PLAN_V1) { MedicationPlanLocalDataSourceV1(instance()) }
    bindProvider<MedicationPlanLocalDataSource>(tag = ModuleTags.MEDICATION_PLAN_V2) { MedicationPlanLocalDataSourceV2(instance()) }
    bindProvider<MedicationPlanLocalDataSource> {
        MedicationPlanLocalDataSourceBridge(
            instance(tag = ModuleTags.MEDICATION_PLAN_V1),
            instance(tag = ModuleTags.MEDICATION_PLAN_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // pharmacy module
    bindProvider<PharmacyLocalDataSource>(tag = ModuleTags.PHARMACY_V1) { PharmacyLocalDataSourceV1(instance()) }
    bindProvider<PharmacyLocalDataSource>(tag = ModuleTags.PHARMACY_V2) { PharmacyLocalDataSourceV2(instance()) }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<PharmacyLocalDataSource> {
        PharmacyLocalDataSourceBridge(
            instance(tag = ModuleTags.PHARMACY_V1),
            instance(tag = ModuleTags.PHARMACY_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // EU task module
    bindProvider<EuTaskLocalDataSource>(tag = ModuleTags.EU_TASK_V1) { EuTaskLocalDataSourceV1(instance()) }
    bindProvider<EuTaskLocalDataSource>(tag = ModuleTags.EU_TASK_V2) { EuTaskLocalDataSourceV2(instance()) }
    bindProvider<EuTaskLocalDataSource> {
        EuTaskLocalDataSourceBridge(
            instance(tag = ModuleTags.EU_TASK_V1),
            instance(tag = ModuleTags.EU_TASK_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // app authentication module
    bindProvider<AppAuthenticationLocalDataSource>(tag = ModuleTags.APP_AUTHENTICATION_V1) { AppAuthenticationLocalDataSourceV1(instance()) }
    bindSingleton<AppAuthenticationLocalDataSource>(tag = ModuleTags.APP_AUTHENTICATION_V2) { instance<AppAuthenticationLocalDataSourceV2>() }
    bindProvider<AppAuthenticationLocalDataSource> {
        AppAuthenticationLocalDataSourceBridge(
            instance(tag = ModuleTags.APP_AUTHENTICATION_V1),
            instance(tag = ModuleTags.APP_AUTHENTICATION_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // shipping info module
    bindProvider<ShippingInfoLocalDataSource>(tag = ModuleTags.SHIPPING_INFO_V1) { ShippingInfoLocalDataSourceV1(instance()) }
    bindProvider<ShippingInfoLocalDataSource>(tag = ModuleTags.SHIPPING_INFO_V2) { ShippingInfoLocalDataSourceV2(instance()) }
    bindProvider<ShippingInfoLocalDataSource> {
        ShippingInfoLocalDataSourceBridge(
            instance(tag = ModuleTags.SHIPPING_INFO_V1),
            instance(tag = ModuleTags.SHIPPING_INFO_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // communication module
    bindProvider<CommunicationLocalDataSource>(tag = ModuleTags.COMMUNICATION_V1) { CommunicationLocalDataSourceV1(instance()) }
    bindProvider<CommunicationLocalDataSource>(tag = ModuleTags.COMMUNICATION_V2) { CommunicationLocalDataSourceV2(instance()) }
    bindProvider<CommunicationLocalDataSource> {
        CommunicationLocalDataSourceBridge(
            instance(tag = ModuleTags.COMMUNICATION_V1),
            instance(tag = ModuleTags.COMMUNICATION_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }
    // truststore module
    bindProvider<TrustStoreLocalDataSource>(tag = ModuleTags.TRUSTSTORE_V1) { TrustStoreLocalDataSourceV1(instance()) }
    bindProvider<TrustStoreLocalDataSource>(tag = ModuleTags.TRUSTSTORE_V2) { TrustStoreLocalDataSourceV2(instance()) }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<TrustStoreLocalDataSource> {
        TrustStoreLocalDataSourceBridge(
            instance(tag = ModuleTags.TRUSTSTORE_V1),
            instance(tag = ModuleTags.TRUSTSTORE_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }
    // PharmacySearchAccessToken module
    bindProvider<PharmacySearchAccessTokenLocalDataSource>(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V1) {
        PharmacySearchAccessTokenLocalDataSourceV1(instance())
    }
    bindProvider<PharmacySearchAccessTokenLocalDataSource>(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V2) {
        PharmacySearchAccessTokenLocalDataSourceV2(instance())
    }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<PharmacySearchAccessTokenLocalDataSource> {
        PharmacySearchAccessTokenLocalDataSourceBridge(
            instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V1),
            instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // profile module
    bindProvider<ProfileLocalDataSource>(tag = ModuleTags.PROFILE_V1) { ProfileLocalDataSourceV1(instance()) }
    bindProvider<ProfileLocalDataSource>(tag = ModuleTags.PROFILE_V2) { ProfileLocalDataSourceV2(instance()) }
    bindProvider<ProfileLocalDataSource> {
        ProfileLocalDataSourceBridge(
            instance(tag = ModuleTags.PROFILE_V1),
            instance(tag = ModuleTags.PROFILE_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }
    // internal message module
    bindProvider<InternalMessagesLocalDataSource>(tag = ModuleTags.INTERNAL_MESSAGE_V1) {
        InternalMessagesLocalDataSourceV1(instance())
    }
    bindProvider<InternalMessagesLocalDataSource>(tag = ModuleTags.INTERNAL_MESSAGE_V2) {
        InternalMessagesLocalDataSourceV2(instance())
    }
    bindProvider<InternalMessagesLocalDataSource> {
        InternalMessageLocalDataBridge(
            v1 = instance(tag = ModuleTags.INTERNAL_MESSAGE_V1),
            v2 = instance(tag = ModuleTags.INTERNAL_MESSAGE_V2),
            logger = instance(),
            roomFeatureToggle = instance(tag = IsRoomEnabled)
        )
    }

    import(roomModule)

    bindProvider<IdpConfigurationLocalDataSource>(tag = ModuleTags.IDP_CONFIGURATION_V1) {
        IdpConfigurationLocalDataSourceV1(instance())
    }
    bindProvider<IdpConfigurationLocalDataSource>(tag = ModuleTags.IDP_CONFIGURATION_V2) {
        IdpConfigurationLocalDataSourceV2(instance())
    }
    bindProvider<IdpConfigurationLocalDataSource> {
        IdpConfigurationLocalDataSourceBridge(
            instance(tag = ModuleTags.IDP_CONFIGURATION_V1),
            instance(tag = ModuleTags.IDP_CONFIGURATION_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    bindProvider<UserAuthenticationLocalDataSource>(tag = ModuleTags.USER_AUTHENTICATION_V1) {
        UserAuthenticationLocalDataSourceV1(instance())
    }
    bindProvider<UserAuthenticationLocalDataSource>(tag = ModuleTags.USER_AUTHENTICATION_V2) {
        UserAuthenticationLocalDataSourceV2(instance())
    }
    bindProvider<UserAuthenticationLocalDataSource> {
        UserAuthenticationLocalDataSourceBridge(
            instance(tag = ModuleTags.USER_AUTHENTICATION_V1),
            instance(tag = ModuleTags.USER_AUTHENTICATION_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    bindProvider<SettingsLocalDataSource>(tag = ModuleTags.SETTINGS_V1) {
        SettingsLocalDataSourceV1(instance())
    }
    bindProvider<SettingsLocalDataSource>(tag = ModuleTags.SETTINGS_V2) {
        instance<SettingsLocalDataSourceV2>()
    }
    bindProvider<SettingsLocalDataSource> {
        SettingsLocalDataSourceBridge(
            instance(tag = ModuleTags.SETTINGS_V1),
            instance(tag = ModuleTags.SETTINGS_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // invoice module
    bindProvider<InvoiceLocalDataSource>(tag = ModuleTags.INVOICE_V1) {
        InvoiceLocalDataSourceV1(instance())
    }
    bindProvider<InvoiceLocalDataSource>(tag = ModuleTags.INVOICE_V2) {
        InvoiceLocalDataSourceV2(instance<AppDatabase>().invoiceDao())
    }
    bindProvider<InvoiceLocalDataSource> {
        InvoiceLocalDataSourceBridge(
            instance(tag = ModuleTags.INVOICE_V1),
            instance(tag = ModuleTags.INVOICE_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    bindSingleton<DataMigrator> {
        DefaultDataMigrator(
            profileV1 = instance(tag = ModuleTags.PROFILE_V1),
            profileV2 = instance(tag = ModuleTags.PROFILE_V2),
            userAuthV1 = instance(tag = ModuleTags.USER_AUTHENTICATION_V1),
            userAuthV2 = instance(tag = ModuleTags.USER_AUTHENTICATION_V2),
            pharmacyV1 = instance(tag = ModuleTags.PHARMACY_V1),
            pharmacyV2 = instance(tag = ModuleTags.PHARMACY_V2),
            searchTokenV1 = instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V1),
            searchTokenV2 = instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V2),
            internalMessageV1 = instance(tag = ModuleTags.INTERNAL_MESSAGE_V1),
            internalMessageV2 = instance(tag = ModuleTags.INTERNAL_MESSAGE_V2),
            settingsV1 = instance(tag = ModuleTags.SETTINGS_V1),
            settingsV2 = instance(tag = ModuleTags.SETTINGS_V2),
            invoiceV1 = instance(tag = ModuleTags.INVOICE_V1),
            invoiceV2 = instance(tag = ModuleTags.INVOICE_V2),
            taskV1 = instance(tag = ModuleTags.TASK_V1),
            taskV2 = instance(tag = ModuleTags.TASK_V2),
            communicationV1 = instance(tag = ModuleTags.COMMUNICATION_V1),
            communicationV2 = instance(tag = ModuleTags.COMMUNICATION_V2),
            idpConfigV1 = instance(tag = ModuleTags.IDP_CONFIGURATION_V1),
            idpConfigV2 = instance(tag = ModuleTags.IDP_CONFIGURATION_V2),
            trustStoreV1 = instance(tag = ModuleTags.TRUSTSTORE_V1),
            trustStoreV2 = instance(tag = ModuleTags.TRUSTSTORE_V2),
            shippingInfoV1 = instance(tag = ModuleTags.SHIPPING_INFO_V1),
            shippingInfoV2 = instance(tag = ModuleTags.SHIPPING_INFO_V2),
            appAuthV1 = instance(tag = ModuleTags.APP_AUTHENTICATION_V1),
            appAuthV2 = instance(tag = ModuleTags.APP_AUTHENTICATION_V2),
            euTaskV1 = instance(tag = ModuleTags.EU_TASK_V1),
            euTaskV2 = instance(tag = ModuleTags.EU_TASK_V2),
            medicationPlanV1 = instance(tag = ModuleTags.MEDICATION_PLAN_V1),
            medicationPlanV2 = instance(tag = ModuleTags.MEDICATION_PLAN_V2)
        )
    }
}
