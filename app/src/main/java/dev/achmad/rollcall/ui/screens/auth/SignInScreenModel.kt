package dev.achmad.rollcall.ui.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.rollcall.core.di.util.inject
import dev.achmad.rollcall.domain.model.SignInOption
import dev.achmad.rollcall.domain.preference.AuthPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInScreenModel(
    private val authPreference: AuthPreference = inject()
): ScreenModel {

    private val _initialLoading = MutableStateFlow(true)
    val initialLoading = _initialLoading.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate = _shouldNavigate.asStateFlow()

    fun signIn(method: SignInOption) {
        screenModelScope.launch {
            when(method) {
                is SignInOption.Basic -> {
                    // TODO
                }
                is SignInOption.Google -> {
                    // TODO
                }
            }
        }
    }

}