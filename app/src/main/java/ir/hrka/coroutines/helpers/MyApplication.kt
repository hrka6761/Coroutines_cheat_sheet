package ir.hrka.coroutines.helpers

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        System.setProperty("kotlinx.coroutines.debug", "on")
    }
}