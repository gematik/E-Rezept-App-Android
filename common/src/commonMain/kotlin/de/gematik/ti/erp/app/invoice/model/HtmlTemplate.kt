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

package de.gematik.ti.erp.app.invoice.model

import de.gematik.ti.erp.app.fhir.temporal.toFormattedDate
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo

object PkvHtmlTemplate {
    private fun createOrganization(
        organizationName: String,
        organizationAddress: String,
        organizationIKNR: String?
    ) = "$organizationName<br>$organizationAddress${organizationIKNR?.let { "<br>IKNR: $it" } ?: ""}"

    private fun createPrescriber(
        prescriberName: String,
        prescriberAddress: String,
        prescriberLANR: String
    ) = "$prescriberName<br>$prescriberAddress<br>LANR: $prescriberLANR"

    private fun createPatient(
        patientName: String,
        patientAddress: String,
        patientKVNR: String
    ) = "$patientName<br>$patientAddress<br>KVNr: $patientKVNR"

    private fun createArticle(
        text: String,
        pzn: String,
        factor: Double,
        bruttoAmount: Double?
    ) = """
        <div>$text</div>
        <div>$pzn</div>
        <div>${factor.currencyString()}</div>
        <div>${bruttoAmount?.currencyString() ?: ""}</div>
    """.trimIndent()

    @Suppress("CyclomaticComplexMethod")
    fun createPriceData(
        medicationRequest: MedicationRequestErpModel?,
        taskId: String,
        totalBruttoAmount: Double,
        items: List<ChargeableItemErpModel>,
        additionalDispenseItems: List<ChargeableItemErpModel>,
        additionalInformation: List<String>
    ): String {
        val (fees, articles) = items.partition {
            InvoiceData.SpecialPZN.isAnyOf(it.description.value)
        }

        val medication = joinMedicationInfo(medicationRequest)

        val multiplePrescriptionInfo = medicationRequest?.multiplePrescriptionInfo?.let {
            createMultiplePrescriptionInfo(it)
        } ?: ""

        // Process articles with Teilmengenabgabe support
        val processedArticles = articles.flatMap { article ->
            val pzn = article.description.value

            val text = if (medicationRequest?.medication?.identifier?.pzn == pzn) {
                "wie verordnet"
            } else {
                article.text
            }

            val baseArticle = createArticle(
                text = text,
                pzn = pzn,
                factor = article.factor,
                bruttoAmount = article.price?.value
            )

            listOf(baseArticle)
        }

        // Process fees with PZN display
        val processedFees = fees.map { fee ->
            val pzn = fee.description.value
            val article = when (InvoiceData.SpecialPZN.valueOfPZN(pzn)) {
                InvoiceData.SpecialPZN.EmergencyServiceFee -> "Notdienstgebühr"
                InvoiceData.SpecialPZN.BTMFee -> "BTM-Gebühr"
                InvoiceData.SpecialPZN.TPrescriptionFee -> "T-Rezept Gebühr"
                InvoiceData.SpecialPZN.ProvisioningCosts -> "Beschaffungskosten"
                InvoiceData.SpecialPZN.DeliveryServiceCosts -> "Botendienst"
                InvoiceData.SpecialPZN.SupplyShortageFee -> "Lieferengpass-Pauschale"
                null -> "Zusätzliche Gebühr"
            }

            createArticle(
                text = article,
                pzn = pzn,
                factor = 0.0,
                bruttoAmount = fee.price?.value
            )
        }

        val articlesHtml = createArticlesPriceData(
            medication,
            taskId,
            multiplePrescriptionInfo,
            processedArticles,
            additionalDispenseItems.map {
                createArticle(
                    text = it.text,
                    pzn = it.description.value,
                    factor = 0.0,
                    bruttoAmount = null
                )
            },
            additionalInformation
        )
        val header = if (fees.isNotEmpty()) {
            """<div class="header" style="padding-top: 0.5em;">Zusätzliche Gebühren [€]</div>
            <div></div>
            <div></div>
            <div></div>
            """.trimIndent()
        } else {
            ""
        }
        val feesHtml = createFeesPriceData(totalBruttoAmount, processedFees, header)

        return articlesHtml.plus(feesHtml)
    }

    private fun createMultiplePrescriptionInfo(
        multiplePrescriptionInfo: MultiplePrescriptionInfo
    ): String {
        var info = ""
        if (multiplePrescriptionInfo.indicator) {
            info = " gültig ab " + multiplePrescriptionInfo.start?.toFormattedDate() +
                " bis " + multiplePrescriptionInfo.end?.toFormattedDate() + " " +
                (multiplePrescriptionInfo.numbering?.numerator?.value ?: "") + " von " +
                (multiplePrescriptionInfo.numbering?.denominator?.value ?: "") + " Verordnungen"
        }
        return info
    }

    fun joinMedicationInfo(medicationRequest: MedicationRequestErpModel?): String =
        medicationRequest?.medication?.let { medication ->
            "${medicationRequest.quantity}x ${medication.name()} / " +
                "${medication.amount?.numerator?.value ?: ""} " +
                "${medication.amount?.numerator?.unit ?: ""} " +
                "${medication.normSizeCode ?: ""} " + "PZN: " + (medication.identifier.pzn ?: "")
        } ?: ""

    private fun createFeesPriceData(
        totalBruttoAmount: Double,
        fees: List<String>,
        header: String
    ) = """
        <div class="frame">
            <div class="content costs">
            $header
                ${fees.joinToString("")}
                <div>&nbsp;</div>
                <div></div>
                <div></div>
                <div></div>
                <div class="header">Gesamtsumme [€]</div>
                <div></div>
                <div></div>
                <div class="header">${totalBruttoAmount.currencyString()}</div>
            </div>
        </div>
    """.trimIndent()

    private fun createArticlesPriceData(
        requestMedication: String,
        taskId: String,
        multiplePrescriptionInfo: String,
        articles: List<String>,
        additionalArticles: List<String>,
        additionalInformation: List<String>
    ) = """
        <div class="frame">
            <div class="content costs">
              <div class="header" style="grid-area: 1/span 4;">Arzneimittel-ID: $taskId $multiplePrescriptionInfo</div>
              <div style="grid-area: 2/span 4;">$requestMedication</div>
                <div>&nbsp;</div>
                <div></div>
                <div></div>
                <div></div>
                <div class="header">Abgabe</div>
                <div class="header">PZN</div>
                <div class="header">Anz.</div>
                <div class="header">Bruttopreis [€]</div>
                ${articles.joinToString("")}
                ${additionalArticles.joinToString("</br>")}
                <div style="grid-area: 6/span 4; padding-top: 2em">${additionalInformation.joinToString("</br>")}</div>
            </div>
        </div>
    """.trimIndent()

    fun createHTML(invoice: PKVInvoiceErpModel): String {
        val patient = createPatient(
            patientName = invoice.patient?.name ?: "",
            patientAddress = invoice.patient?.address?.joinToHtmlString() ?: "",
            patientKVNR = invoice.patient?.insuranceIdentifier ?: ""
        )

        val prescriber = createPrescriber(
            prescriberName = invoice.practitioner?.name ?: "",
            prescriberAddress = invoice.practitionerOrganization?.address?.joinToHtmlString() ?: "",
            prescriberLANR = invoice.practitioner?.practitionerIdentifier ?: ""
        )
        val pharmacy = createOrganization(
            organizationName = invoice.pharmacyOrganization?.name ?: "",
            organizationAddress = invoice.pharmacyOrganization?.address?.joinToHtmlString() ?: "",
            organizationIKNR = invoice.pharmacyOrganization?.uniqueIdentifier
        )

        val priceData = createPriceData(
            medicationRequest = invoice.medicationRequest,
            taskId = invoice.taskId,
            totalBruttoAmount = invoice.invoice?.totalBruttoAmount ?: 0.0,
            items = invoice.invoice?.chargeableItems ?: emptyList(),
            additionalDispenseItems = invoice.invoice?.additionalDispenseItems ?: emptyList(),
            additionalInformation = invoice.invoice?.additionalInformation ?: emptyList()
        )

        return createPkvHtmlInvoiceTemplate(
            patient = patient,
            patientBirthdate = invoice.patient?.dateOfBirth?.toFormattedDate() ?: "",
            prescriber = prescriber,
            prescribedOn = invoice.medicationRequest?.authoredOn?.toFormattedDate() ?: "",
            pharmacy = pharmacy,
            dispensedOn = invoice.whenHandedOver?.toFormattedDate() ?: "",
            priceData = priceData
        )
    }
}

@Suppress("LongParameterList", "LongMethod")
fun createPkvHtmlInvoiceTemplate(
    patient: String,
    patientBirthdate: String,
    prescriber: String,
    prescribedOn: String,
    pharmacy: String,
    dispensedOn: String,
    priceData: String
): String = """
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
            grid-template-columns: 3fr 1fr 1fr 1fr;
        }

        .costs > .header {
            font-weight: bold;
            line-height: 2em;
        }

    </style>
    <body>
        <h1>Digitaler Beleg zur Abrechnung Ihres E-Rezeptes</h1>
        <sub>Bitte leiten Sie diesen Beleg über die App an Ihre Versicherung weiter.</sub>
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
                    $pharmacy
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

fun Double.currencyString() = "%.2f".format(this)
