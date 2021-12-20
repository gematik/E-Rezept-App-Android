package de.gematik.ti.erp.app.profiles.usecase.model

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import java.time.Instant

object ProfilesUseCaseData {

    @Immutable
    data class Profile(
        val id: Int,
        val name: String,
        val active: Boolean,
        val color: ProfileColors,
        val lastAuthenticated: Instant? = null,
        val ssoToken: SingleSignOnToken? = null,
        val accessToken: String? = null
    ) {
        fun ssoTokenValid(now: Instant = Instant.now()) = ssoToken?.isValid(now) ?: false
    }
}
