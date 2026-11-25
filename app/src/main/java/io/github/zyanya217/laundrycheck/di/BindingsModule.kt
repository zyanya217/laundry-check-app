package io.github.zyanya217.laundrycheck.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.zyanya217.laundrycheck.data.district.AssetDistrictRepository
import io.github.zyanya217.laundrycheck.data.district.DistrictRepository
import io.github.zyanya217.laundrycheck.data.forecast.ForecastRepository
import io.github.zyanya217.laundrycheck.data.forecast.NetworkForecastRepository
import io.github.zyanya217.laundrycheck.data.location.LocationProvider
import io.github.zyanya217.laundrycheck.data.location.PlatformLocationProvider
import io.github.zyanya217.laundrycheck.data.settings.DataStoreSettingsRepository
import io.github.zyanya217.laundrycheck.data.settings.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds @Singleton
    abstract fun forecastRepository(impl: NetworkForecastRepository): ForecastRepository

    @Binds @Singleton
    abstract fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds @Singleton
    abstract fun districtRepository(impl: AssetDistrictRepository): DistrictRepository

    @Binds @Singleton
    abstract fun locationProvider(impl: PlatformLocationProvider): LocationProvider
}
