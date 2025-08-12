package dev.achmad.rollcall.ui.screens.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.rollcall.ui.util.collectAsState

object SignInScreen : Screen {
    private fun readResolve(): Any = SignInScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SignInScreenModel() }

    }

}

@Composable
private fun SignInScreen(
    initialLoading: Boolean = true,
    loading: Boolean = false,
    onSuccess: () -> Unit = {},
    onFailed: () -> Unit = {},
) {

}