package dev.achmad.rollcall.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.ScreenTransition
import dev.achmad.rollcall.core.di.util.injectLazy
import dev.achmad.rollcall.ui.screens.auth.CompanyCodeScreen
import dev.achmad.rollcall.domain.preference.AuthPreference
import dev.achmad.rollcall.ui.screens.auth.SignInScreen
import dev.achmad.rollcall.ui.screens.checkin.CheckInScreen
import dev.achmad.rollcall.ui.theme.RollCallTheme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

class MainActivity : ComponentActivity() {

    private var isReady = false
    private var initialScreen: Screen = CompanyCodeScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        handlePreDraw()
        enableEdgeToEdge()
        setContent {
            RollCallTheme {
                val slideDistance = rememberSlideDistance()
                Navigator(
                    screen = initialScreen,
                    disposeBehavior = NavigatorDisposeBehavior(
                        disposeNestedNavigators = false,
                        disposeSteps = true,
                    )
                ) { navigator ->
                    ScreenTransition(
                        modifier = Modifier.fillMaxSize(),
                        navigator = navigator,
                        transition = {
                            materialSharedAxisX(
                                forward = navigator.lastEvent != StackEvent.Pop,
                                slideDistance = slideDistance,
                            )
                        },
                    )
                    HandleNewIntent(this@MainActivity, navigator)
                }
            }
        }
    }

    private fun handlePreDraw() {
        val authPreference by injectLazy<AuthPreference>()
        val companyCode = authPreference.companyCode().get()
        val token = authPreference.token().get()
        if (companyCode.isNotEmpty()) initialScreen = SignInScreen
        if (token.isNotEmpty()) initialScreen = CheckInScreen

        isReady = true
    }

    @Composable
    private fun HandleNewIntent(context: Context, navigator: Navigator) {
        LaunchedEffect(Unit) {
            callbackFlow {
                val componentActivity = context as ComponentActivity
                val consumer = Consumer<Intent> { trySend(it) }
                componentActivity.addOnNewIntentListener(consumer)
                awaitClose { componentActivity.removeOnNewIntentListener(consumer) }
            }.collectLatest { handleIntentAction(it, navigator) }
        }
    }

    private fun handleIntentAction(intent: Intent, navigator: Navigator) {
        // TODO handle intent
    }
}