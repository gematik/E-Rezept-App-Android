package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import java.util.Locale

@Composable
fun HealthCardInfoScreen() {
    val activity = LocalActivity.current

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                mode = NavigationBarMode.Close,
                headline = stringResource(R.string.cdw_health_card_info_title),
                onClick = { activity.onBackPressed() }
            )
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { activity.onBackPressed() },
                ) {
                    Text(text = stringResource(id = R.string.ok).uppercase(Locale.getDefault()))
                }
                SpacerSmall()
            }
        }
    ) {

        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.cdw_healthcard_info_headline),
                style = MaterialTheme.typography.h6
            )
            SpacerSmall()
            Text(stringResource(R.string.cdw_healthcard_info))
            SpacerMedium()
            SimpleCheck(
                annotatedStringResource(
                    R.string.cdw_healthcard_info_can,
                    annotatedStringBold(stringResource(R.string.cdw_healthcard_info_card)),
                ).toString()
            )
            SpacerSmall()
            SimpleCheck(
                annotatedStringResource(
                    R.string.cdw_healthcard_info_pin,
                    annotatedStringBold(stringResource(R.string.cdw_healthcard_info_pin_pin)),
                ).toString()
            )
            SpacerMedium()
            Text(
                annotatedStringResource(
                    R.string.cdw_healthcard_info_mail_description,
                    annotatedStringBold(stringResource(R.string.cdw_healthcard_info_mail)),
                )
            )
            SpacerXXLarge()
            HintCard(
                properties = HintCardDefaults.flatProperties(
                    backgroundColor = AppTheme.colors.primary100
                ),
                image = {
                    HintSmallImage(
                        painterResource(R.drawable.information),
                        innerPadding = it
                    )
                },
                title = { Text(stringResource(R.string.cdw_health_card_info_title)) },
                body = { Text(stringResource(R.string.cdw_health_card_info_hint_description)) }
            )
        }
    }
}
