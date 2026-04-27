package ua.com.radiokot.camerapp

import android.app.Application
import android.os.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.com.radiokot.camerapp.cut.cutModule
import ua.com.radiokot.camerapp.io.ioModule
import ua.com.radiokot.camerapp.stamps.domain.EnsurePrimaryStampCollectionUseCase
import ua.com.radiokot.camerapp.stamps.stampsModule
import ua.com.radiokot.camerapp.util.KoinSlf4jLogger
import java.io.File
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class CameraApp : Application() {

    private val log by lazy {
        KotlinLogging.logger("App")
    }
    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("App"))

    override fun onCreate() {
        super.onCreate()

        initLogging()

        startKoin {
            logger(KoinSlf4jLogger)
            androidContext(this@CameraApp)
            modules(
                ioModule,
                cutModule,
                stampsModule,
            )
        }

        ensurePrimaryCollection()
    }

    private fun initLogging() {
        // The Logback configuration is in the app/src/main/assets/logback.xml

        @Suppress("KotlinConstantConditions", "RedundantSuppression")
        System.setProperty(
            "LOG_LEVEL",
            if (BuildConfig.DEBUG)
                "TRACE"
            else
                "INFO"
        )

        try {
            val logFolder =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "CameraApp"
                )
                    .also(File::mkdirs)

            System.setProperty(
                "LOG_FILE_DIRECTORY",
                logFolder.path
            )
        } catch (e: Exception) {
            log.error(e) {
                "initLogging(): failed log file folder initialization"
            }
        }

        val defaultUncaughtExceptionHandler: UncaughtExceptionHandler? =
            Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            log.error(exception) { "Fatal exception\n" }

            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(thread, exception)
            } else {
                exitProcess(10)
            }
        }

        log.trace {
            "initLogging(): trace logger enabled"
        }
        log.debug {
            "initLogging(): debug logger enabled"
        }
        log.info {
            "initLogging(): info logger enabled"
        }
    }

    private fun ensurePrimaryCollection() {
        coroutineScope.launch {
            get<EnsurePrimaryStampCollectionUseCase>()
                .invoke()
        }
    }
}
