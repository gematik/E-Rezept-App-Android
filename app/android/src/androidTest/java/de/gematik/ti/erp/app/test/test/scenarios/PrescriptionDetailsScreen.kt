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

package de.gematik.ti.erp.app.test.test.scenarios

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.LargeTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.core.TaskCollection
import de.gematik.ti.erp.app.test.test.core.prescription.PrescriptionUtils
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.OnboardingScreen
import de.gematik.ti.erp.app.test.test.screens.PrescriptionsScreen
import org.junit.Rule
import org.junit.Test

@LargeTest
class PrescriptionDetailsScreen(fontScale: String) : WithFontScale(fontScale) {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }
    private val prescriptionsScreen by lazy { PrescriptionsScreen(composeRule) }
    private val prescriptionUtils by lazy { PrescriptionUtils(composeRule, mainScreen, prescriptionsScreen) }

    @Test
    fun pzn_medication_details() {
        val tasks = TaskCollection.generate(1, TestConfig.AppDefaultVirtualEgkKvnr, composeRule.activity.testWrapper)
        val prescriptionData = tasks.taskData.first().prescription

        try {
            onboardingScreen.tapSkipOnboardingButton()
            mainScreen.tapConnectLater()
            mainScreen.tapTooltips()
            prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()
            prescriptionsScreen.awaitPrescriptions()

            // details main page
            prescriptionsScreen.clickOnPrescription(prescriptionData)
            prescriptionsScreen.userSeesPrescriptionDetails()
            prescriptionsScreen.userExpectsPrescriptionData(prescriptionData)

            // technical details
            prescriptionsScreen.userClicksOnTechnicalDetails()
            prescriptionsScreen.userSeesTechnicalDetailsScreen()
            prescriptionsScreen.userExpectsTechnicalInformationData(prescriptionData)

            prescriptionsScreen.userClicksBack()
            prescriptionsScreen.userSeesPrescriptionDetails()

            // patient page
            prescriptionsScreen.userClicksOnPatientDetails()
            prescriptionsScreen.userSeesPatientDetailsScreen()
            prescriptionsScreen.userExpectsPatientDetailsData(prescriptionData)

            prescriptionsScreen.userClicksBack()
            prescriptionsScreen.userSeesPrescriptionDetails()

            // organization page
            prescriptionsScreen.userClicksOnOrganizationDetails()
            prescriptionsScreen.userSeesOrganizationDetailsScreen()
            prescriptionsScreen.userExpectsOrganizationDetailsData(prescriptionData)

            prescriptionsScreen.userClicksBack()
            prescriptionsScreen.userSeesPrescriptionDetails()

            // medication page
            prescriptionsScreen.userClicksOnMedicationDetails()
            prescriptionsScreen.userSeesMedicationDetailsScreen()
            prescriptionsScreen.userExpectsMedicationDetailsData(prescriptionData)

            prescriptionsScreen.userClicksBack()
            prescriptionsScreen.userSeesPrescriptionDetails()
        } finally {
            tasks.deleteAll()
        }
    }

    @Test
    fun delete_task() {
        val tasks = TaskCollection.generate(1, TestConfig.AppDefaultVirtualEgkKvnr, composeRule.activity.testWrapper)
        val prescriptionData = tasks.taskData.first().prescription

        try {
            onboardingScreen.tapSkipOnboardingButton()
            mainScreen.tapConnectLater()
            mainScreen.tapTooltips()
            prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()
            prescriptionsScreen.awaitPrescriptions()

            // details main page
            prescriptionsScreen.clickOnPrescription(prescriptionData)
            prescriptionsScreen.userSeesPrescriptionDetails()

            prescriptionsScreen.userClicksMoreButton()
            prescriptionsScreen.userClicksDeleteButton()
            prescriptionsScreen.userSeesConfirmDeleteDialog()
            prescriptionsScreen.userConfirmsDeletion()

            mainScreen.userSeesMainScreen()
            prescriptionsScreen.awaitPrescriptions()

            prescriptionsScreen.userMissesPrescription(prescriptionData)
        } finally {
            tasks.deleteAll()
        }
    }

    @Test
    fun main_screen_with_many_prescriptions() {
        val tasks = TaskCollection.generate(6, TestConfig.AppDefaultVirtualEgkKvnr, composeRule.activity.testWrapper)

        try {
            onboardingScreen.tapSkipOnboardingButton()
            mainScreen.tapConnectLater()
            mainScreen.tapTooltips()
            prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()

            tasks.taskData.forEach { data ->
                prescriptionsScreen.awaitPrescriptions()
                prescriptionsScreen.clickOnPrescription(data.prescription)
                prescriptionsScreen.userSeesPrescriptionDetails()

                // technical details
                prescriptionsScreen.userClicksOnTechnicalDetails()
                prescriptionsScreen.userSeesTechnicalDetailsScreen()
                prescriptionsScreen.userExpectsTechnicalInformationData(data.prescription)

                prescriptionsScreen.userClicksBack()
                prescriptionsScreen.userSeesPrescriptionDetails()
                prescriptionsScreen.userClicksClose()
                mainScreen.userSeesMainScreen()
            }

            // TODO use expiresOn and TODO sorting is unstable (uses expiresOn with day accuracy) in app
            // val fromOldToNew = tasks.prescriptions.sortedBy { it.authoredOn }
            // prescriptionsScreen.userSeesPrescriptionSortedBy(fromOldToNew)
        } finally {
            tasks.deleteAll()
        }
    }

    @Test
    fun main_screen_check_if_prescriptions_exist() {
        val tasks = TaskCollection.generate(6, TestConfig.AppDefaultVirtualEgkKvnr, composeRule.activity.testWrapper)

        try {
            onboardingScreen.tapSkipOnboardingButton()
            mainScreen.tapConnectLater()
            mainScreen.tapTooltips()
            prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()
            prescriptionsScreen.awaitPrescriptions()

            tasks.taskData.forEach { data ->
                prescriptionsScreen.userSeesPZNPrescription(data.prescription)
            }
        } finally {
            tasks.deleteAll()
        }
    }
}
