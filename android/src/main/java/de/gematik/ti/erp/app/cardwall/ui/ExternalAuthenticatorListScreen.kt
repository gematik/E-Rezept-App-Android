package de.gematik.ti.erp.app.cardwall.ui

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.cardwall.ui.model.ExternalAuthenticatorListViewModel
import de.gematik.ti.erp.app.idp.api.models.AuthenticationID
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExternalAuthenticatorListScreen(
    mainNavController: NavController,
    viewModel: ExternalAuthenticatorListViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val redirectScope = rememberCoroutineScope()
    val externalAuthenticatorList by produceState(
        initialValue = emptyList<AuthenticationID>(),
        producer = { value = viewModel.externalAuthenticatorIDList() }
    )

    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                all = PaddingDefaults.Medium
            )
    ) {
        items(externalAuthenticatorList) {
            Button(
                modifier = Modifier.padding(
                    all = PaddingDefaults.Medium
                ),
                onClick = {
                    redirectScope.launch {
                        val redirectUri =
                            viewModel.startAuthorizationWithExternal(it.authenticationID)
                        context.startActivity(Intent(Intent.ACTION_VIEW, redirectUri))
                    }
                }
            ) {
                Text(text = it.name)
            }
        }
    }
}
