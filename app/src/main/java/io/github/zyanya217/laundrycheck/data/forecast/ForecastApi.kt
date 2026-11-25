package io.github.zyanya217.laundrycheck.data.forecast

import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("api/forecast")
    suspend fun forecast(
        @Query("county") county: String,
        @Query("district") district: String,
    ): ForecastResponse
}
