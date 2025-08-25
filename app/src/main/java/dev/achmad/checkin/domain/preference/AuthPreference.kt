package dev.achmad.checkin.domain.preference

import dev.achmad.checkin.core.preference.PreferenceStore

class AuthPreference(
    private val preferenceStore: PreferenceStore
) {
    fun token() = preferenceStore.getString("token")
    fun companyCode() = preferenceStore.getString("company_code")
}