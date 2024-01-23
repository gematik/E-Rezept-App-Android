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

package de.gematik.ti.erp.app.analytics

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCase
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCaseData
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenBottomSheetContentState
import de.gematik.ti.erp.app.pharmacy.ui.PharmacySearchSheetContentState
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailBottomSheetContent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Requirement(
    "A_19095",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Resets the analytics id and creates a new one [-> init block]."
)
class Analytics(
    private val context: Context,
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val analyticsUseCase: AnalyticsUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val scope = CoroutineScope(dispatcher)

    private val isAnalyticsAllowed by lazy {
        isAnalyticsAllowedUseCase.invoke()
    }

    val analyticsAllowed: StateFlow<Boolean>
        get() = isAnalyticsAllowed.stateIn(scope, SharingStarted.Eagerly, false)

    @Requirement(
        "A_19093",
        "A_19094",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Only screen names and data like error states are transmitted."
    )
    @Requirement(
        "O.Purp_2#5",
        "O.Purp_4#1",
        "O.Data_6#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "User interaction analytics trigger records data only if user opt-in is given."
    )
    fun trackScreen(screenName: String) {
        if (analyticsAllowed.value) {
            Contentsquare.send(screenName)
            Napier.d("Analytics send $screenName")
        }
    }

    init {
        Napier.d("Init Analytics")

        Contentsquare.forgetMe()

        scope.launch {
            val isAllowed = isAnalyticsAllowed.first()
            setAnalyticsPreference(isAllowed)
        }
    }

    private val popUpFlow = MutableStateFlow(AnalyticsData.defaultPopUp)

    private var analyticsScreenFlow = combine(
        analyticsUseCase.screenNamesFlow,
        popUpFlow
    ) { screenNames, popUp ->
        AnalyticsData.AnalyticsScreenState(screenNames, popUp)
    }

    val screenState
        @Composable
        get() = analyticsScreenFlow.collectAsStateWithLifecycle(AnalyticsData.defaultAnalyticsState)

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

    @Requirement(
        "A_20187#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Enable analytics"
    )
    @Requirement(
        "O.Purp_5#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Enable/disable usage analytics."
    )
    fun setAnalyticsPreference(allow: Boolean) {
        when (allow) {
            true -> allowAnalytics()
            else -> disallowAnalytics()
        }
    }
    private fun allowAnalytics() {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(true)
        }
        Contentsquare.optIn(context)
        Napier.i("Analytics allowed")
    }

    @Requirement(
        "A_20187#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Disable analytics"
    )
    @Requirement(
        "O.Purp_5#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Disable usage analytics."
    )
    private fun disallowAnalytics() {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(false)
        }
        Contentsquare.optOut(context)
        Napier.i("Analytics disallowed")
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
}

@Suppress("ComposableNaming")
@Composable
fun trackNavigationChangesAsync(
    navController: NavHostController,
    previousNavEntry: String,
    onNavEntryChange: (String) -> Unit
) {
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState

    LaunchedEffect(navController.currentBackStackEntry) {
        async {
            try {
                val route = Uri.parse(navController.currentBackStackEntry?.destination?.route)
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
}

fun trackScreenUsingNavEntry(
    route: String,
    analytics: Analytics,
    analyticsList: List<AnalyticsUseCaseData.AnalyticsScreenName>
) {
    try {
        val name = analyticsList.find { it.key == route }?.name ?: ""
        val trackedName = when {
            name.isNotEmpty() -> name
            else -> route
        }
        Napier.d { "Content square tracking: Key is $trackedName" }
        analytics.trackScreen(trackedName)
    } catch (expected: Exception) {
        Napier.e("Couldn't track navigation screen", expected)
    }
}

@Suppress("ComposableNaming")
@Composable
fun trackPopUps(
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

            is MainScreenBottomSheetContentState.GrantConsent -> {
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
