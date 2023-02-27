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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.fhir.model.ChargeableItem
import de.gematik.ti.erp.app.fhir.model.SpecialPZN

object PkvHtmlTemplate {
    fun createOrganization(
        organizationName: String,
        organizationAddress: String,
        organizationIKNR: String?
    ) = "$organizationName<br>$organizationAddress${organizationIKNR?.let { "<br>IKNR: $it" } ?: ""}"

    fun createPrescriber(
        prescriberName: String,
        prescriberAddress: String,
        prescriberLANR: String
    ) = "$prescriberName<br>$prescriberAddress<br>LANR: $prescriberLANR"

    fun createPatient(
        patientName: String,
        patientAddress: String,
        patientKVNR: String
    ) = "$patientName<br>$patientAddress<br>KVNR: $patientKVNR"

    fun createArticle(
        article: String,
        factor: Double,
        tax: Double,
        bruttoAmount: Double
    ) = """
        <div>$article</div>
        <div>${factor.currencyString()}</div>
        <div>${tax.currencyString()}%</div>
        <div>${bruttoAmount.currencyString()}</div>
    """.trimIndent()

    fun createPriceData(
        currency: String,
        totalBruttoAmount: Double,
        items: List<ChargeableItem>
    ): String {
        val (fees, articles) = items.partition {
            (it.description as? ChargeableItem.Description.PZN)?.isSpecialPZN() ?: false
        }

        return createPriceData(
            currency = currency,
            totalBruttoAmount = totalBruttoAmount,
            articles = articles.map {
                val article = when (it.description) {
                    is ChargeableItem.Description.HMNR -> "HMKNR ${it.description.hmnr}"
                    is ChargeableItem.Description.PZN -> "PZN ${it.description.pzn}"
                    is ChargeableItem.Description.TA1 -> "TA1 ${it.description.ta1}"
                }

                createArticle(
                    article = article,
                    factor = it.factor,
                    tax = it.price.tax,
                    bruttoAmount = it.price.value
                )
            },
            fees = fees.map {
                require(it.description is ChargeableItem.Description.PZN)
                val article = when (SpecialPZN.valueOfPZN(it.description.pzn)) {
                    SpecialPZN.EmergencyServiceFee -> "Notdienstgebühr"
                    SpecialPZN.BTMFee -> "BTM-Gebühr"
                    SpecialPZN.TPrescriptionFee -> "T-Rezept Gebühr"
                    SpecialPZN.ProvisioningCosts -> "Beschaffungskosten"
                    SpecialPZN.DeliveryServiceCosts -> "Botendienst"
                    null -> error("wrong mapping")
                }
                createArticle(
                    article = article,
                    factor = it.factor,
                    tax = it.price.tax,
                    bruttoAmount = it.price.value
                )
            }
        )
    }

    fun createPriceData(
        currency: String,
        totalBruttoAmount: Double,
        articles: List<String>,
        fees: List<String>
    ) = """
        <div class="frame">
        <h5>Kosten</h5>
        <div class="content costs">
            <div class="header">Artikel</div>
            <div class="header">Anzahl</div>
            <div class="header">MwSt.</div>
            <div class="header">Bruttopreis in $currency</div>
            ${articles.joinToString("")}
            <div class="header" style="padding-top: 0.5em;">Zusätzliche Gebühren</div>
            <div></div>
            <div></div>
            <div></div>
            ${fees.joinToString("")}
            <div class="header">Gesamtsumme</div>
            <div></div>
            <div></div>
            <div class="header">${totalBruttoAmount.currencyString()}</div>
        </div>
    </div>
    """.trimIndent()
}

@Suppress("LongParameterList", "LongMethod")
fun createPkvHtmlInvoiceTemplate(
    patient: String,
    patientBirthdate: String,
    prescriber: String,
    prescribedOn: String,
    organization: String,
    dispensedOn: String,
    priceData: String
) = """
    <!DOCTYPE html>
    <html lang="de">
    <head>
        <meta charset="UTF-8">
        <title>Abrechnung zur Vorlage bei Kostenträger</title>
    </head>
    <style>
        * {
            padding: 0;
            margin: 0;
        }

        body {
            font-family: sans-serif;
        }

        h1 {
            margin: 0 16px 8px;
            font-size: 24px;
        }

        h1 + sub {
            margin: 0 16px 16px;
            font-size: 18px;
        }

        .frame_row {
            display: grid;
            grid-template-columns: 1fr 1fr;
        }

        .frame {
            padding-top: 16pt;
        }

        .frame > h5 {
            padding: 8px;
            margin-left: 8px;
        }

        .frame > .content {
            border: 1px solid black;
            border-radius: 8px;
            padding: 8px;
            margin: 0 8px 8px;

            display: grid;
            grid-template-columns: 1fr 1fr;
        }

        .content > .top_right {
            justify-self: end;
        }

        .content > .bottom_end {
            justify-self: end;
            align-self: end;
        }

        .content > .bottom_start {
            justify-self: start;
            align-self: end;
        }

        .frame > .costs {
            line-height: 1.5em;

            display: grid;
            grid-template-columns: 4fr 1fr 1fr 1fr;
        }

        .costs > div:nth-last-child(-n+4),
        .costs > .header {
            font-weight: bold;
            line-height: 2em;
        }

        .costs > div:nth-child(4n+2),
        .costs > div:nth-child(4n+3),
        .costs > div:nth-child(4n+4) {
            text-align: end;
        }
    </style>
    <body>
    <h1>PDF für Privatversicherte zur Abrechnung Ihres E-Rezeptes</h1>
    <sub>Bitte reichen Sie diesen Beleg als PDF bei Ihrem Kostenträger ein</sub>
    <div class="frame">
        <h5>Patient</h5>
        <div class="content">
            <div>
                $patient
            </div>
            <div class="top_right">
                Geb. $patientBirthdate
            </div>
        </div>
    </div>
    <div class="frame_row">
        <div class="frame">
            <h5>Aussteller</h5>
            <div class="content">
                <div style="grid-area: 1/span 2;">
                    $prescriber
                </div>
                <div style="padding-top: 1em;">
                    ausgestellt am:
                </div>
                <div class="bottom_end">
                    $prescribedOn
                </div>
            </div>
        </div>
        <div class="frame">
            <h5>Eingelöst</h5>
            <div class="content">
                <div style="grid-area: 1/span 2;">
                    $organization
                </div>
                <div style="padding-top: 1em;">
                    abgegeben am:
                </div>
                <div class="bottom_end">
                    $dispensedOn
                </div>
            </div>
        </div>
    </div>
    $priceData
    </body>
    </html>
""".trimIndent()

private fun Double.currencyString() = "%.2f".format(this)
