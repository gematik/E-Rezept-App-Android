package de.gematik.ti.erp.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.gematik.ti.erp.app.BuildConfig
import okhttp3.Interceptor

@InstallIn(SingletonComponent::class)
@Module
object ReleaseHeadersModule {

    @DevelopReleaseHeaderInterceptor
    @Provides
    fun providesHeaderInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .build()
        )
    }
}
