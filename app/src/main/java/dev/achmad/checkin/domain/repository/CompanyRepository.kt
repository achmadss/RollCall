package dev.achmad.checkin.domain.repository

import dev.achmad.checkin.domain.model.Company

interface CompanyRepository {
    suspend fun getCompanyByCode(code: String): Company
}