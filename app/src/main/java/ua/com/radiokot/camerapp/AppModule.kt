package ua.com.radiokot.camerapp

import android.os.Environment
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

    viewModel {
        CaptureAndSaveViewModel(
            stampDirectory = get(named(DIRECTORY_STAMPS)),
            application = androidApplication(),
        )
    }

    viewModel {
        StampsScreenViewModel(
            stampRepository = get(),
        )
    }

    viewModel {
        StampScreenViewModel(
            parameters = getOrNull()
                ?: error("No StampScreenViewModel.Parameters provided"),
            stampRepository = get(),
        )
    }
}
