package de.gematik.ti.erp.app.debug.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class DebugSettingsData(
    val eRezeptServiceURL: String,
    val eRezeptActive: Boolean,
    val idpUrl: String,
    val idpActive: Boolean,
    val bearerToken: String,
    val bearerTokenIsSet: Boolean,
    val fakeNFCCapabilities: Boolean,
    val cardAccessNumberIsSet: Boolean,
    val cardWallIntroIsAccepted: Boolean,
    val multiProfile: Boolean,
    val activeProfileName: String
) : Parcelable
