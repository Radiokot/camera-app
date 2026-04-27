package ua.com.radiokot.camerapp.io

import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.fetcher.AndroidFetchers
import org.koin.dsl.bind
import org.koin.dsl.module

val ioModule = module {

    single<Landscapist> {
        val config = LandscapistConfig(
            diskCacheSize = 0L,
        )

        Landscapist.Builder()
            .config(config)
            .fetcher(AndroidFetchers.createDefault(config.networkConfig))
            .build()
    } bind Landscapist::class
}
