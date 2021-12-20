package de.gematik.ti.erp.app.cardwall.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallSwitchNavigation
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium

@Preview
@Composable
fun SwitchPreview() {
    AppTheme {
        var navSelection: CardWallSwitchNavigation = CardWallSwitchNavigation.NO_ROUTE

        CardWallAuthenticationChooser(
            navSelection = navSelection,
            onSelected = { onSelection -> navSelection = onSelection },
            hasNfc = true,
        )
    }
}

@Composable
fun CardWallAuthenticationChooser(
    navSelection: CardWallSwitchNavigation,
    onSelected: (CardWallSwitchNavigation) -> Unit,
    hasNfc: Boolean
) {

    Column {
        Column(Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                text = stringResource(id = R.string.cdw_register_title),
                style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onBackground,
                modifier = Modifier.padding(bottom = PaddingDefaults.Small)
            )
            Text(
                text = stringResource(id = R.string.cdw_register_body),
                style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onBackground
            )
        }
        SelectableCard(
            image = { CardImagePainter(R.drawable.man_register, stringResource(R.string.cdw_man_register_accessibility)) },
            header = stringResource(id = R.string.cdw_register_with_healthy_card),
            info = stringResource(id = R.string.cdw_register_healty_card_info),
            selected = when (navSelection) { CardWallSwitchNavigation.INTRO -> true; else -> false },
            onCardSelected = { onSelected(CardWallSwitchNavigation.INTRO) },
            enabled = hasNfc,
        )
        if (!hasNfc) Text(
            text = stringResource(id = R.string.cdw_no_nfc),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Large),
            color = Color.Red,
            fontSize = 10.sp,
        )
        SpacerMedium()
        // todo: Change img, header and enable card when fasttrack is live
        SelectableCard(
            image = { CardImagePainter(R.drawable.ic_construction_android, stringResource(R.string.cdw_woman_register_accessibility)) },
            header = stringResource(id = R.string.cdw_register_with_health_insurance),
            info = stringResource(id = R.string.cdw_register_health_insurance_info),
            selected = when (navSelection) { CardWallSwitchNavigation.INSURANCE_APP -> true; else -> false },
            onCardSelected = { onSelected(CardWallSwitchNavigation.INSURANCE_APP) },
            enabled = false
        )
    }
}

@Composable
fun CardImagePainter(@DrawableRes drawableId: Int, description: String) {
    val painter = painterResource(id = drawableId)
    Image(painter = painter, contentDescription = description, contentScale = ContentScale.Fit, modifier = Modifier.size(80.dp))
}
