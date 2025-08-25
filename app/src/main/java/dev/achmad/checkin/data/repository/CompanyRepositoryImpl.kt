package dev.achmad.checkin.data.repository

import dev.achmad.checkin.core.network.GET
import dev.achmad.checkin.core.network.NetworkHelper
import dev.achmad.checkin.core.network.awaitBaseResponse
import dev.achmad.checkin.data.remote.model.base.NullDataResponseException
import dev.achmad.checkin.domain.API_URL_V1
import dev.achmad.checkin.domain.model.Company
import dev.achmad.checkin.domain.repository.CompanyRepository

class CompanyRepositoryImpl(
    private val networkHelper: NetworkHelper,
): CompanyRepository {

    override suspend fun getCompanyByCode(code: String): Company {
        return networkHelper.client.newCall(
            GET("$API_URL_V1/companies/$code")
        ).awaitBaseResponse<Company>().data
            ?: throw NullDataResponseException()
    }

}