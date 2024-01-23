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

package de.gematik.ti.erp.app.test.test.screens

import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.assertHasText
import de.gematik.ti.erp.app.test.test.core.await
import de.gematik.ti.erp.app.test.test.core.hasInsuranceState
import de.gematik.ti.erp.app.test.test.core.hasMedicationCategory
import de.gematik.ti.erp.app.test.test.core.hasPrescriptionId
import de.gematik.ti.erp.app.test.test.core.hasSubstitutionAllowed
import de.gematik.ti.erp.app.test.test.core.hasSupplyForm
import de.gematik.ti.erp.app.test.test.core.prescription.Prescription
import de.gematik.ti.erp.app.test.test.core.sleep
import org.junit.Assert.assertTrue

@Suppress("UnusedPrivateMember")
class PrescriptionsScreen(private val composeRule: ComposeTestRule) : SemanticsNodeInteractionsProvider by composeRule {
    enum class PrescriptionState {
        Redeemable, // ready
        WaitForResponse, // artificial state
        InProgress, // inProgress
        Redeemed
    }

    fun userClicksBack() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userClicksClose() {
        onNodeWithTag(TestTag.TopNavigation.CloseButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun awaitPrescriptions() {
        composeRule.await(TestConfig.LoadPrescriptionsTimeout) {
            onNodeWithTag(TestTag.Prescriptions.Content)
                .assertIsDisplayed()
                .assert(hasAnyChild(hasTestTag(TestTag.Prescriptions.FullDetailPrescription)))
        }
        composeRule.sleep(2500L)
    }

    fun awaitArchivedPrescriptions() {
        composeRule.await(TestConfig.LoadPrescriptionsTimeout) {
            onNodeWithTag(TestTag.Prescriptions.Archive.Content)
                .assertIsDisplayed()
                .assert(hasAnyChild(hasTestTag(TestTag.Prescriptions.FullDetailPrescription)))
        }
        composeRule.sleep(2500L)
    }

    fun awaitPrescriptionScreen() {
        composeRule.await(TestConfig.ScreenChangeTimeout) {
            onNodeWithTag(TestTag.Prescriptions.Content)
                .assertIsDisplayed()
        }
    }

    fun refreshPrescriptions() {
        onNodeWithTag(TestTag.Prescriptions.Content)
            .performScrollToIndex(0)
            .performTouchInput {
                swipeDown()
            }
        composeRule.sleep(2500L)
    }

    fun userSeesPZNPrescription(
        data: Prescription,
        inState: PrescriptionState = PrescriptionState.Redeemable
    ) {
        val prescriptionNode = onNodeWithTag(TestTag.Prescriptions.Content, useUnmergedTree = true)
            .assertIsDisplayed()
            .performScrollToKey("prescription-${data.taskId}")
            .onChildren()
            .filterToOne(hasPrescriptionId(data.taskId))
            .onChildren()

        prescriptionNode
            .filterToOne(hasTestTag(TestTag.Prescriptions.FullDetailPrescriptionName))
            .assertTextContains(data.medication?.name ?: "")

        val expectedTestTag = when (inState) {
            PrescriptionState.Redeemable -> TestTag.Prescriptions.PrescriptionRedeemable
            PrescriptionState.WaitForResponse -> TestTag.Prescriptions.PrescriptionWaitForResponse
            PrescriptionState.InProgress -> TestTag.Prescriptions.PrescriptionInProgress
            PrescriptionState.Redeemed -> TestTag.Prescriptions.PrescriptionRedeemed
        }
        prescriptionNode
            .filterToOne(hasTestTag(expectedTestTag))
            .assertIsDisplayed()
    }

    fun userSeesPrescriptionInArchive(
        data: Prescription,
        inState: PrescriptionState = PrescriptionState.Redeemable
    ) {
        val prescriptionNode = onNodeWithTag(TestTag.Prescriptions.Archive.Content, useUnmergedTree = true)
            .assertIsDisplayed()
            .performScrollToKey("prescription-${data.taskId}")
            .onChildren()
            .filterToOne(hasPrescriptionId(data.taskId))
            .onChildren()

        prescriptionNode
            .filterToOne(hasTestTag(TestTag.Prescriptions.FullDetailPrescriptionName))
            .assertTextContains(data.medication?.name ?: "")

        val expectedTestTag = when (inState) {
            PrescriptionState.Redeemable -> TestTag.Prescriptions.PrescriptionRedeemable
            PrescriptionState.WaitForResponse -> TestTag.Prescriptions.PrescriptionWaitForResponse
            PrescriptionState.InProgress -> TestTag.Prescriptions.PrescriptionInProgress
            PrescriptionState.Redeemed -> TestTag.Prescriptions.PrescriptionRedeemed
        }
        prescriptionNode
            .filterToOne(hasTestTag(expectedTestTag))
            .assertIsDisplayed()
    }

    fun clickOnPrescription(data: Prescription) {
        onNodeWithTag(TestTag.Prescriptions.Content)
            .assertIsDisplayed()
            .performScrollToKey("prescription-${data.taskId}")
            .onChildren()
            .filterToOne(hasPrescriptionId(data.taskId))
            .performClick()
    }

    fun userSeesPrescriptionDetails() {
        composeRule.await(TestConfig.ScreenChangeTimeout) {
            onNodeWithTag(TestTag.Prescriptions.Details.Screen)
                .assertIsDisplayed()
        }
    }

    fun userClicksMoreButton() {
        onNodeWithTag(TestTag.Prescriptions.Details.MoreButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userClicksDeleteButton() {
        onNodeWithTag(TestTag.Prescriptions.Details.DeleteButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userSeesConfirmDeleteDialog() {
        onNodeWithTag(TestTag.AlertDialog.Modal, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun userConfirmsDeletion() {
        onNodeWithTag(TestTag.AlertDialog.ConfirmButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userMissesPrescription(data: Prescription) {
        onNodeWithTag(TestTag.Prescriptions.Content)
            .assertIsDisplayed()
            .onChildren()
            .filter(hasPrescriptionId(data.taskId))
            .assertCountEquals(0)
    }

    fun userClicksArchiveButton() {
        onNodeWithTag(TestTag.Prescriptions.Content)
            .performScrollToNode(hasTestTag(TestTag.Prescriptions.ArchiveButton))

        onNodeWithTag(TestTag.Prescriptions.ArchiveButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userSeesPrescriptionSortedBy(prescriptions: List<Prescription>) {
        val node = onNodeWithTag(TestTag.Prescriptions.Content)
            .fetchSemanticsNode()
        val config = node.config[SemanticsProperties.IndexForKey]

        val all = prescriptions.map { prescription ->
            val index = config.invoke("prescription-${prescription.taskId}")
            "$index - ${prescription.taskId} - ${prescription.authoredOn}"
        }.joinToString("\n")

        prescriptions.fold(-1) { previousIndex, prescription ->
            val index = config.invoke("prescription-${prescription.taskId}")
            val msg = "Index should match: $index > $previousIndex for ${prescription.taskId}\n$all"
            assertTrue(msg, index > previousIndex)
            index
        }
    }

    //

    private fun onDetailsNode(testTag: String, contentTestTag: String) =
        onNodeWithTag(contentTestTag)
            .assertIsDisplayed()
            .performScrollToNode(hasTestTag(testTag))
            .onChildren()
            .filterToOne(hasTestTag(testTag))

    private fun assertWith(
        testTag: String,
        contentTestTag: String,
        with: SemanticsNodeInteraction.() -> Unit
    ) {
        onDetailsNode(testTag, contentTestTag).with()
    }

    private fun assertText(testTag: String, contentTestTag: String, vararg data: String?) {
        val node = onDetailsNode(testTag, contentTestTag).assertHasText(includeEditableText = false)
        val dataFiltered = data.filterNotNull()
        Log.d("assertText", "Assert hasText: ${dataFiltered.joinToString(" and ") { "[$it]" }} on $testTag")

        dataFiltered.forEach {
            node
                .assertTextContains(it, substring = true, ignoreCase = true)
        }
    }

    private fun assertDetailsText(testTag: String, vararg data: String?) =
        assertText(testTag = testTag, contentTestTag = TestTag.Prescriptions.Details.Content, *data)

    fun userExpectsPrescriptionData(data: Prescription) {
        assertDetailsText(TestTag.Prescriptions.Details.MedicationButton, data.medication?.name)
        assertDetailsText(TestTag.Prescriptions.Details.PatientButton, data.patient?.firstName, data.patient?.lastName)
        assertDetailsText(TestTag.Prescriptions.Details.PrescriberButton, data.practitioner?.name)
    }

    // technical details

    fun userClicksOnTechnicalDetails() {
        onDetailsNode(TestTag.Prescriptions.Details.TechnicalInformationButton, TestTag.Prescriptions.Details.Content)
            .assertHasClickAction()
            .performClick()
    }

    fun userSeesTechnicalDetailsScreen() {
        onNodeWithTag(TestTag.Prescriptions.Details.TechnicalInformation.Screen)
            .assertIsDisplayed()
    }

    private fun assertTechnicalText(testTag: String, vararg data: String?) =
        assertText(
            testTag = testTag,
            contentTestTag = TestTag.Prescriptions.Details.TechnicalInformation.Content,
            *data
        )

    fun userExpectsTechnicalInformationData(data: Prescription) {
        assertTechnicalText(TestTag.Prescriptions.Details.TechnicalInformation.TaskId, data.taskId)
        assertTechnicalText(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode, data.accessCode)
    }

    // patient

    fun userClicksOnPatientDetails() {
        onDetailsNode(TestTag.Prescriptions.Details.PatientButton, TestTag.Prescriptions.Details.Content)
            .assertHasClickAction()
            .performClick()
    }

    fun userSeesPatientDetailsScreen() {
        onNodeWithTag(TestTag.Prescriptions.Details.Patient.Screen)
            .assertIsDisplayed()
    }

    private fun assertPatientText(testTag: String, vararg data: String?) =
        assertText(
            testTag = testTag,
            contentTestTag = TestTag.Prescriptions.Details.Patient.Content,
            *data
        )

    fun userExpectsPatientDetailsData(data: Prescription) {
        assertPatientText(TestTag.Prescriptions.Details.Patient.KVNR, data.patient?.kvnr)
        assertPatientText(TestTag.Prescriptions.Details.Patient.BirthDate, data.patient?.birthDate)
        assertPatientText(TestTag.Prescriptions.Details.Patient.Name, data.patient?.firstName, data.patient?.lastName)
        assertPatientText(TestTag.Prescriptions.Details.Patient.InsuranceName, data.coverage?.insuranceName)
        assertPatientText(
            TestTag.Prescriptions.Details.Patient.Address,
            data.patient?.city,
            data.patient?.postal,
            data.patient?.street
        )
        assertWith(
            testTag = TestTag.Prescriptions.Details.Patient.InsuranceState,
            contentTestTag = TestTag.Prescriptions.Details.Patient.Content,
            with = {
                data.coverage?.insuranceState?.let {
                    assert(hasInsuranceState(it))
                }
            }
        )
    }

    // organization

    fun userClicksOnOrganizationDetails() {
        onDetailsNode(TestTag.Prescriptions.Details.OrganizationButton, TestTag.Prescriptions.Details.Content)
            .assertHasClickAction()
            .performClick()
    }

    fun userSeesOrganizationDetailsScreen() {
        onNodeWithTag(TestTag.Prescriptions.Details.Organization.Screen)
            .assertIsDisplayed()
    }

    private fun assertOrganizationText(testTag: String, vararg data: String?) =
        assertText(
            testTag = testTag,
            contentTestTag = TestTag.Prescriptions.Details.Organization.Content,
            *data
        )

    fun userExpectsOrganizationDetailsData(data: Prescription) {
        assertOrganizationText(TestTag.Prescriptions.Details.Organization.Name, data.practitioner?.officeName)
        assertOrganizationText(
            TestTag.Prescriptions.Details.Organization.Address,
            data.practitioner?.city,
            data.practitioner?.postal,
            data.practitioner?.street
        )
        assertOrganizationText(TestTag.Prescriptions.Details.Organization.BSNR, data.practitioner?.bsnr)
        assertOrganizationText(TestTag.Prescriptions.Details.Organization.Phone, data.practitioner?.phone)
        assertOrganizationText(TestTag.Prescriptions.Details.Organization.EMail, data.practitioner?.email)
    }

    // medication

    fun userClicksOnMedicationDetails() {
        onDetailsNode(TestTag.Prescriptions.Details.MedicationButton, TestTag.Prescriptions.Details.Content)
            .assertHasClickAction()
            .performClick()
    }

    fun userSeesMedicationDetailsScreen() {
        onNodeWithTag(TestTag.Prescriptions.Details.Medication.Screen)
            .assertIsDisplayed()
    }

    private fun assertMedicationText(testTag: String, vararg data: String?) =
        assertText(
            testTag = testTag,
            contentTestTag = TestTag.Prescriptions.Details.Medication.Content,
            *data
        )

    fun userExpectsMedicationDetailsData(data: Prescription) {
        assertMedicationText(TestTag.Prescriptions.Details.Medication.Name, data.medication?.name)
        assertMedicationText(TestTag.Prescriptions.Details.Medication.Amount, data.medication?.amount.toString())
        assertMedicationText(TestTag.Prescriptions.Details.Medication.PZN, data.medication?.pzn.toString())
        assertMedicationText(TestTag.Prescriptions.Details.Medication.DosageInstruction, data.medication?.dosage)
        assertWith(
            testTag = TestTag.Prescriptions.Details.Medication.SubstitutionAllowed,
            contentTestTag = TestTag.Prescriptions.Details.Medication.Content,
            with = {
                data.medication?.substitutionAllowed?.let {
                    assert(hasSubstitutionAllowed(it))
                }
            }
        )
        assertWith(
            testTag = TestTag.Prescriptions.Details.Medication.SupplyForm,
            contentTestTag = TestTag.Prescriptions.Details.Medication.Content,
            with = {
                data.medication?.supplyForm?.let {
                    assert(hasSupplyForm(it))
                }
            }
        )
        assertWith(
            testTag = TestTag.Prescriptions.Details.Medication.Category,
            contentTestTag = TestTag.Prescriptions.Details.Medication.Content,
            with = {
                data.medication?.category?.let {
                    assert(hasMedicationCategory(it))
                }
            }
        )
    }
}
