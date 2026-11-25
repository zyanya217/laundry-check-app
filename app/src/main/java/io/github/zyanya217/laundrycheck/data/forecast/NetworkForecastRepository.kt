package io.github.zyanya217.laundrycheck.data.forecast

import io.github.zyanya217.laundrycheck.model.Forecast
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class NetworkForecastRepository @Inject constructor(
    private val api: ForecastApi,
    private val json: Json,
) : ForecastRepository {

    override suspend fun forecast(county: String, district: String): Forecast =
        try {
            api.forecast(county, district).toModel()
        } catch (e: HttpException) {
            throw ForecastException(errorCode(e), e)
        } catch (e: IOException) {
            throw ForecastException("network", e)
        } catch (e: SerializationException) {
            throw ForecastException("unknown", e)
        }

    /** The server's `error` value when the body has one, otherwise `http_<status>`. */
    private fun errorCode(e: HttpException): String {
        val fallback = "http_${e.code()}"
        val body = e.response()?.errorBody()?.string() ?: return fallback
        return try {
            json.decodeFromString<ErrorBody>(body).error
        } catch (_: SerializationException) {
            fallback
        }
    }
}
