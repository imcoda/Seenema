package ink.c0d4.seenema

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import ink.c0d4.seenema.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SeenemaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("SeenemaApp", "Providing WorkManager Configuration")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d("SeenemaApp", "onCreate")

        // Explicitly initialize WorkManager to ensure HiltWorkerFactory is used.
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            Log.d("SeenemaApp", "WorkManager initialized explicitly")
        } catch (e: Exception) {
            Log.e("SeenemaApp", "WorkManager already initialized: ${e.message}")
        }
        
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}
