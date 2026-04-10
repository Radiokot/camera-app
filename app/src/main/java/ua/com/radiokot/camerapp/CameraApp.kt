package ua.com.radiokot.camerapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CameraApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CameraApp)
            modules(
                appModule,
            )
        }
    }
}
