package io.github.nfdz.clubadmin

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import YOUR.ADMIN.APP.ID.HERE.BuildConfig
import timber.log.Timber

class ClubAdminApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setupLogger()
        setupCrashlytics()
    }

    private fun setupLogger() {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setupCrashlytics() {
        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()
        // Initialize Fabric with the debug-disabled crashlytics
        Fabric.with(this, crashlyticsKit)

        // Debug
        //        Crashlytics crashlyticsKit = new Crashlytics.Builder()
        //                .core(new CrashlyticsCore.Builder().build())
        //                .build();
        //        final Fabric fabric = new Fabric.Builder(this)
        //                .kits(crashlyticsKit)
        //                .debuggable(true)
        //                .build();
        //        Fabric.with(fabric);
    }

}

fun reportException(ex: Exception) {
    if (BuildConfig.DEBUG) {
        Timber.w(ex, "CrashlyticsReportDebug")
    } else {
        Crashlytics.logException(ex)
    }
}