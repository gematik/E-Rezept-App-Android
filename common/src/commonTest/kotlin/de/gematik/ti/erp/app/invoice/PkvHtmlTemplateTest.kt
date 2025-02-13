/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.invoice
import de.gematik.ti.erp.app.invoice.model.createPkvHtmlInvoiceTemplate
import kotlin.test.assertEquals
import kotlin.test.Test

class PkvHtmlTemplateTest {

    @Test
    fun `test createPkvHtmlInvoiceTemplate generates correct HTML structure`() {
        val patient = "TEST_PATIENT_NAME<br>TEST_PATIENT_ADDRESS<br>KVNr: TEST_KVNR"
        val patientBirthdate = "TEST_BIRTHDATE"
        val prescriber = "TEST_PRESCRIBER_NAME<br>TEST_PRESCRIBER_ADDRESS<br>LANR: TEST_LANR"
        val prescribedOn = "TEST_PRESCRIBED_DATE"
        val pharmacy = "TEST_PHARMACY_NAME<br>TEST_PHARMACY_ADDRESS<br>IKNR: TEST_IKNR"
        val dispensedOn = "TEST_DISPENSED_DATE"
        val priceData = """
            <div class="frame">
                <div class="content costs">
                    <div class="header">Abgabe</div>
                    <div class="header">PZN</div>
                    <div class="header">Anz.</div>
                    <div class="header">Bruttopreis [€]</div>
                </div>
            </div>
        """.trimIndent()

        val result = createPkvHtmlInvoiceTemplate(
            patient = patient,
            patientBirthdate = patientBirthdate,
            prescriber = prescriber,
            prescribedOn = prescribedOn,
            pharmacy = pharmacy,
            dispensedOn = dispensedOn,
            priceData = priceData
        )

        val expectedHtml = """
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

        assertEquals(expectedHtml, result, "Generated HTML does not match expected structure")
    }
}
