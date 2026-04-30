package com.bunnyvpn.repository

import com.bunnyvpn.model.IpResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface IpApiService {
    @GET("?format=json")
    suspend fun getPublicIp(): IpResponse
}

object IpRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ipify.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: IpApiService = retrofit.create(IpApiService::class.java)

    suspend fun fetchPublicIp(): String {
        return try {
            service.getPublicIp().ip
        } catch (e: Exception) {
            "Unable to fetch"
        }
    }
}
