package de.gematik.ti.erp.app.prescription.detail.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class AppLinkPrescription(
    val patient: String?,
    val prescriber: String?,
    val description: String?,
    val prescribedOn: String?,
    val taskUrl: String,
    val emoji: String = AppLinkAllowedPrescriptionEmojis.random()
)

private val AppLinkAllowedPrescriptionEmojis = listOf(
    "\uD83D\uDE23",    // 😣
    "\uD83D\uDE35",    // 😵
    "\uD83D\uDE35\u200D\uD83D\uDCAB",    // 😵‍💫
    "\uD83E\uDD22",    // 🤢
    "\uD83E\uDD2E",    // 🤮
    "\uD83E\uDD27",    // 🤧
    "\uD83D\uDE37",    // 😷
    "\uD83E\uDD12",    // 🙂
    "\uD83E\uDD15",    // 🙅
    "\uD83E\uDE7A",    // 🥺
    "\uD83D\uDC89",    // 💉
    "\uD83D\uDC8A",    // 💊
    "\uD83E\uDDA0",    // 🦠
    "\uD83C\uDF21",    // 🌡️
    "\uD83E\uDDEA",    // 🧪
    "\uD83E\uDDEB"     // 🧫
)
