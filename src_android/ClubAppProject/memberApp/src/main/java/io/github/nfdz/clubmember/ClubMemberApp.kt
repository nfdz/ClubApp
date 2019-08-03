package io.github.nfdz.clubmember

import androidx.multidex.MultiDexApplication
import androidx.appcompat.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import YOUR.MEMBER.APP.ID.HERE.BuildConfig
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

class ClubMemberApp : MultiDexApplication() {

    private val DB_NAME = "clubmemberapp.realm"
    private val SCHEMA_VERSION_NAME = 1L

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setupLogger()
        setupRealm()
        setupCrashlytics()
    }

    private fun setupLogger() {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setupRealm() {
        Realm.init(this)
        Realm.setDefaultConfiguration(getRealmConfiguration())
    }

    private fun getRealmConfiguration(): RealmConfiguration {
        val bld = RealmConfiguration.Builder()
            .name(DB_NAME)
            .schemaVersion(SCHEMA_VERSION_NAME)
        if (!BuildConfig.DEBUG) {
            bld.deleteRealmIfMigrationNeeded()
        }
        return bld.build()
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