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

package de.gematik.ti.erp.app.eurezept.di

import de.gematik.ti.erp.app.eurezept.domain.usecase.GenerateEuAccessCodeUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetAllEuCountriesUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetCountryLocaleRedemptionCodeUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionsUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetPrescriptionPhrasesUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GrantEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.LocationBasedCountryDetectionUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.ToggleIsEuRedeemableByPatientAuthorizationUseCase
import de.gematik.ti.erp.app.eurezept.repository.DefaultEuRepository
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.eurezept.repository.EuTaskLocalDataSource
import de.gematik.ti.erp.app.eurezept.repository.EuTaskRemoteDataSource
import de.gematik.ti.erp.app.eurezept.util.QrCodeGenerator
import de.gematik.ti.erp.app.fhir.euredeem.parser.EuRedeemAccessCodeResponseParser
import de.gematik.ti.erp.app.localization.DefaultXmlResourceParserWrapper
import de.gematik.ti.erp.app.localization.GetSupportedCountriesFromXmlUseCase
import de.gematik.ti.erp.app.shared.usecase.GetLocationUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val euModule = DI.Module("euModule", allowSilentOverride = true) {

    bindProvider<EuRepository> {
        DefaultEuRepository(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider { GenerateEuAccessCodeUseCase(instance(), instance()) }
    bindProvider { EuTaskRemoteDataSource(instance()) }
    bindProvider { EuTaskLocalDataSource(instance()) }
    bindProvider { GetEuPrescriptionConsentUseCase(instance()) }
    bindProvider { GrantEuPrescriptionConsentUseCase(instance()) }
    bindProvider { GetAllEuCountriesUseCase(instance<EuRepository>()) }
    bindProvider { GetEuPrescriptionsUseCase(prescriptionRepository = instance(), profileRepository = instance()) }
    bindProvider { GetLocationUseCase(instance()) }
    bindProvider { ToggleIsEuRedeemableByPatientAuthorizationUseCase(instance()) }
    bindProvider { LocationBasedCountryDetectionUseCase(instance()) }
    bindProvider { QrCodeGenerator() }
    bindProvider { GetPrescriptionPhrasesUseCase(instance()) }
    bindProvider { GetCountryLocaleRedemptionCodeUseCase(instance()) }
    bindProvider { EuRedeemAccessCodeResponseParser() }

    bindProvider<GetSupportedCountriesFromXmlUseCase> {
        val context = instance<android.content.Context>()
        val resId = context.resources.getIdentifier(
            "countries_config",
            "xml",
            context.packageName
        )
        val countriesXmlParser = context.resources.getXml(resId)
        GetSupportedCountriesFromXmlUseCase(parser = DefaultXmlResourceParserWrapper(countriesXmlParser))
    }
}
