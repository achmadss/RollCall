package dev.achmad.checkin.presentation.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.checkin.core.di.util.inject
import dev.achmad.checkin.data.remote.model.base.NullDataResponseException
import dev.achmad.checkin.domain.repository.CompanyRepository
import dev.achmad.checkin.presentation.util.ToastHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompanyCodeScreenState(
    val initialLoading: Boolean = true,
    val loading: Boolean = false,
    val shouldNavigate: Boolean = false,
)

class CompanyCodeScreenModel(
    private val toastHelper: ToastHelper = inject(),
    private val companyRepository: CompanyRepository = inject(),
): ScreenModel {

    private val _state = MutableStateFlow(CompanyCodeScreenState())
    val state = _state.asStateFlow()

    fun validateCompanyCode(value: String) {
        if (value.isEmpty()) {
            _state.update { it.copy(initialLoading = false) }
            return
        }
        screenModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }
                val company = companyRepository.getCompanyByCode(value)
                _state.update { it.copy(loading = false) }
                if (company.active) {
                    _state.update { it.copy(shouldNavigate = true) }
                    return@launch
                }
            } catch (e: NullDataResponseException) {
                e.printStackTrace()
                toastHelper.show("Invalid company code.")
            } catch (e: Exception) {
                e.printStackTrace()
                toastHelper.show("Something wrong happened. Please try again.")
            } finally {
                _state.update { it.copy(initialLoading = false) }
            }
        }
    }

}