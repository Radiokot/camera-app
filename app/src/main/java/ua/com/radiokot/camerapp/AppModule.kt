package ua.com.radiokot.camerapp

import android.os.Environment
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.fetcher.AndroidFetchers
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

const val DIRECTORY_STAMPS = "stamps-dir"

val appModule = module {

    single(named(DIRECTORY_STAMPS)) {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Stamps"
        ).also { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    single {
        FsStampRepository(
            stampDirectory = get(named(DIRECTORY_STAMPS)),
        )
    } bind StampRepository::class

    single {
        FsStampCollectionRepository(
            stampDirectory = get(named(DIRECTORY_STAMPS)),
        )
    } bind StampCollectionRepository::class

    single<Landscapist> {
        val config = LandscapistConfig(
            diskCacheSize = 0L,
        )

        Landscapist.Builder()
            .config(config)
            .fetcher(AndroidFetchers.createDefault(config.networkConfig))
            .build()
    } bind Landscapist::class

    viewModel {
        CaptureAndSaveViewModel(
            stampRepository = get(),
            imageAdjustmentsControllerViewModel = get(),
            application = androidApplication(),
        )
    }

    viewModel {
        ImageAdjustmentsControllerViewModel()
    }

    viewModel {
        StampsScreenViewModel(
            stampRepository = get(),
            parameters = getOrNull()
                ?: error("No StampsScreenViewModel.Parameters provided"),
        )
    }

    viewModel {
        StampScreenViewModel(
            parameters = getOrNull()
                ?: error("No StampScreenViewModel.Parameters provided"),
            stampRepository = get(),
        )
    }

    viewModel {
        CollectionsScreenViewModel(
            collectionRepository = get(),
            getStampCollectionsWithSamplesUseCase = get(),
        )
    }

    factory {
        GetStampCollectionsWithSamplesUseCase(
            collectionRepository = get(),
            stampRepository = get(),
        )
    }
}
