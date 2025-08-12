package dev.achmad.rollcall.ui.screens.auth

import dev.achmad.rollcall.core.preference.PreferenceStore

class AuthPreference(
    private val preferenceStore: PreferenceStore
) {
    fun companyCode() = preferenceStore.getString("company_code")
    fun token() = preferenceStore.getString("token")
}