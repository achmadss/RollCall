package dev.achmad.checkin.presentation.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.checkin.core.di.util.inject
import dev.achmad.checkin.domain.model.Company
import dev.achmad.checkin.domain.model.SignInOption
import dev.achmad.checkin.domain.preference.AuthPreference
import dev.achmad.checkin.domain.repository.AuthRepository
import dev.achmad.checkin.domain.repository.CompanyRepository
import dev.achmad.checkin.presentation.util.ToastHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInScreenState(
    val fetchCompanyLoading: Boolean = true,
    val signInLoading: Boolean = false,
    val shouldNavigateNext: Boolean = false,
    val company: Company? = null,
)

class SignInScreenModel(
    private val toastHelper: ToastHelper = inject(),
    private val authPreference: AuthPreference = inject(),
    private val authRepository: AuthRepository = inject(),
    private val companyRepository: CompanyRepository = inject(),
): ScreenModel {

    private val _state = MutableStateFlow(SignInScreenState())
    val state = _state.asStateFlow()

    fun signIn(option: SignInOption) {
        screenModelScope.launch {
            try {
                _state.update { it.copy(signInLoading = true) }
                authRepository.signIn(option)
                _state.update { it.copy(shouldNavigateNext = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                toastHelper.show("Sign-in failed: \n${e.message}")
            } finally {
                _state.update { it.copy(signInLoading = false,) }
            }
        }
    }

    fun fetchCompany() {
        screenModelScope.launch {
            try {
                _state.update { it.copy(fetchCompanyLoading = true) }
                val company = companyRepository.getCompanyByCode(authPreference.companyCode().get())
                _state.update { it.copy(company = company) }
            } catch (e: Exception) {
                e.printStackTrace()
                toastHelper.show("Fetch company failed: \n${e.message}")
            } finally {
                _state.update { it.copy(fetchCompanyLoading = false) }
            }
        }
    }

}