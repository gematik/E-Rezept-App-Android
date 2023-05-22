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

package de.gematik.ti.erp.app.analytics

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCase
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCaseData
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenBottomSheetContentState
import de.gematik.ti.erp.app.pharmacy.ui.PharmacySearchSheetContentState
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailBottomSheetContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine

private const val PrefsName = "analyticsAllowed"

// `gemSpec_eRp_FdV A_20187`
class Analytics(
    private val context: Context,
    private val prefs: SharedPreferences,
    analyticsUseCase: AnalyticsUseCase
) {
    private val _analyticsAllowed = MutableStateFlow(false)
    val analyticsAllowed: StateFlow<Boolean>
        get() = _analyticsAllowed

    init {
        Napier.d("Init Analytics")

        Contentsquare.forgetMe()

        _analyticsAllowed.value = prefs.getBoolean(PrefsName, false)
        if (_analyticsAllowed.value) {
            allowTracking()
        } else {
            disallowTracking()
        }
    }

    private val popUpFlow = MutableStateFlow(AnalyticsData.defaultPopUp)

    private var analyticsScreenFlow = combine(
        analyticsUseCase.screenNamesFlow,
        popUpFlow
    ) {
            screenNames, popUp ->
        AnalyticsData.AnalyticsScreenState(screenNames, popUp)
    }

    val screenState
        @Composable
        get() = analyticsScreenFlow.collectAsState(AnalyticsData.defaultAnalyticsState)

    fun onPopUpShown(popUpScreenName: String) {
        if (analyticsAllowed.value) {
            popUpFlow.value = AnalyticsData.PopUp(true, popUpScreenName)
        }
    }

    fun onPopUpClosed() {
        if (analyticsAllowed.value) {
            popUpFlow.value = AnalyticsData.PopUp(false, "")
        }
    }

    fun trackScreen(screenName: String) {
        if (analyticsAllowed.value) {
            Contentsquare.send(screenName)
            Napier.d("Analytics send $screenName")
        }
    }

    fun allowTracking() {
        _analyticsAllowed.value = true

        Contentsquare.optIn(context)

        prefs.edit {
            putBoolean(PrefsName, true)
        }

        Napier.d("Analytics allowed")
    }

    fun disallowTracking() {
        _analyticsAllowed.value = false

        Contentsquare.optOut(context)

        prefs.edit {
            putBoolean(PrefsName, false)
        }

        Napier.d("Analytics disallowed")
    }

    fun trackIdentifiedWithIDP() {
        trackScreen("idp_authenticated")
    }

    enum class AuthenticationProblem(val event: String) {
        CardBlocked("card_blocked"),
        CardAccessNumberWrong("card_can_wrong"),
        CardCommunicationInterrupted("card_com_interrupted"),
        CardPinWrong("card_pin_wrong"),
        IDPCommunicationFailed("idp_com_failed"),
        IDPCommunicationInvalidCertificate("idp_com_invalid_certificate"),
        IDPCommunicationInvalidOCSPOfCard("idp_com_invalid_ocsp_of_card"),
        SecureElementCryptographyFailed("secure_element_cryptography_failed"),
        UserNotAuthenticated("user_not_authenticated")
    }

    fun trackAuthenticationProblem(kind: AuthenticationProblem) {
        trackScreen("auth_error_${kind.event}")
    }

    fun trackSaveScannedPrescriptions() {
        trackScreen("pres_scanned_saved")
    }
}

@Composable
fun TrackNavigationChanges(
    navController: NavHostController,
    previousNavEntry: String,
    onNavEntryChange: (String) -> Unit
) {
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState

    LaunchedEffect(navController.currentBackStackEntry) {
        try {
            val route = Uri.parse(navController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            if (route != previousNavEntry) {
                onNavEntryChange(route)
                trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
            }
        } catch (expected: Exception) {
            Napier.e("Couldn't track navigation screen", expected)
        }
    }
}

fun trackScreenUsingNavEntry(
    route: String,
    analytics: Analytics,
    analyticsList: List<AnalyticsUseCaseData.AnalyticsScreenName>
) {
    try {
        val name = analyticsList.find { it.key == route }?.name ?: ""
        if (name.isNotEmpty()) {
            analytics.trackScreen(name)
        } else {
            analytics.trackScreen(route)
        }
    } catch (expected: Exception) {
        Napier.e("Couldn't track navigation screen", expected)
    }
}

@Composable
fun TrackPopUps(
    analytics: Analytics,
    analyticsState: AnalyticsData.AnalyticsScreenState
) {
    LaunchedEffect(analyticsState) {
        if (analyticsState.popUp.visible) {
            analytics.trackScreen(
                analyticsState.screenNamesList.find {
                    it.key == analyticsState.popUp.name
                }?.name ?: ""
            )
        }
    }
}

fun Analytics.trackAuth(state: AuthenticationState) {
    if (analyticsAllowed.value) {
        when (state) {
            AuthenticationState.HealthCardBlocked ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardBlocked)
            AuthenticationState.HealthCardCardAccessNumberWrong ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardAccessNumberWrong)
            AuthenticationState.HealthCardCommunicationInterrupted ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardCommunicationInterrupted)
            AuthenticationState.HealthCardPin1RetryLeft,
            AuthenticationState.HealthCardPin2RetriesLeft ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardPinWrong)
            AuthenticationState.IDPCommunicationFailed ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationFailed)
            AuthenticationState.IDPCommunicationInvalidCertificate ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationInvalidCertificate)
            AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationInvalidOCSPOfCard)
            AuthenticationState.SecureElementCryptographyFailed ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.SecureElementCryptographyFailed)
            AuthenticationState.UserNotAuthenticated ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.UserNotAuthenticated)
            else -> {}
        }
    }
}

fun Analytics.trackPrescriptionDetailPopUps(content: PrescriptionDetailBottomSheetContent) {
    if (analyticsAllowed.value) {
        when (content) {
            is PrescriptionDetailBottomSheetContent.HowLongValid -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.SubstitutionAllowed -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.DirectAssignment -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.EmergencyFee -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.EmergencyFeeNotExempt -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.AdditionalFeeNotExempt -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.AdditionalFeeExempt -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.Scanned -> {
                onPopUpShown(content.popUp.name)
            }

            is PrescriptionDetailBottomSheetContent.Failure -> {
                onPopUpShown(content.popUp.name)
            }
        }
    }
}

fun Analytics.trackMainScreenBottomPopUps(content: MainScreenBottomSheetContentState) {
    if (analyticsAllowed.value) {
        when (content) {
            is MainScreenBottomSheetContentState.AddProfile -> {
                onPopUpShown(content.popUp.name)
            }

            is MainScreenBottomSheetContentState.Welcome -> {
                onPopUpShown(content.popUp.name)
            }

            is MainScreenBottomSheetContentState.EditProfileName -> {
                onPopUpShown(content.popUp.name)
            }

            is MainScreenBottomSheetContentState.EditProfilePicture -> {
                onPopUpShown(content.popUp.name)
            }
        }
    }
}

fun Analytics.trackPharmacySearchPopUps(content: PharmacySearchSheetContentState) {
    if (analyticsAllowed.value) {
        when (content) {
            is PharmacySearchSheetContentState.PharmacySelected -> {
                onPopUpShown(content.popUp.name)
            }

            is PharmacySearchSheetContentState.FilterSelected -> {
                onPopUpShown(content.popUp.name)
            }
        }
    }
}

fun Analytics.trackScannerPopUps() {
    if (analyticsAllowed.value) {
        onPopUpShown("main_scanner_successDrawer")
    }
}

fun Analytics.trackOrderPopUps() {
    if (analyticsAllowed.value) {
        onPopUpShown("orders_pickupCode")
    }
}

object AnalyticsData {
    @Immutable
    data class AnalyticsScreenState(
        val screenNamesList: List<AnalyticsUseCaseData.AnalyticsScreenName>,
        var popUp: PopUp
    )

    data class PopUp(
        var visible: Boolean,
        var name: String
    )

    val defaultPopUp = PopUp(
        visible = false,
        name = ""
    )

    val defaultAnalyticsState = AnalyticsScreenState(
        screenNamesList = emptyList(),
        popUp = defaultPopUp
    )
}
