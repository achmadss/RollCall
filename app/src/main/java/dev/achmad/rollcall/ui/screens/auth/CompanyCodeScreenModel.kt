package dev.achmad.rollcall.ui.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.rollcall.core.di.util.inject
import dev.achmad.rollcall.domain.preference.AuthPreference
import dev.achmad.rollcall.ui.util.ToastHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompanyCodeScreenModel(
    private val authPreference: AuthPreference = inject(),
    private val toastHelper: ToastHelper = inject()
): ScreenModel {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate = _shouldNavigate.asStateFlow()

    val companyCode = authPreference.companyCode().stateIn(screenModelScope)

    fun validateCompanyCode(value: String) {
        screenModelScope.launch {
            _loading.update { true }
            delay(3000) // TODO remove mock
            _loading.update { false }
            val success = true
            if (success) {
                authPreference.companyCode().set(value)
                _shouldNavigate.update { true }
                return@launch
            }
            toastHelper.show("Invalid company code!")
        }
    }

}