package de.rki.coronawarnapp.ccl.configuration.update

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CCLConfigurationUpdateScheduler @Inject constructor(
    private val foregroundState: ForegroundState,
    private val cclConfigurationUpdater: CCLConfigurationUpdater,
    private val workManager: WorkManager
    // private val dccWalletManager: DccWalletInfoCalculationManager
) {

    fun setup() {
        Timber.d("setup()")

        // perform update every time the app comes into the foreground
        foregroundState
            .isInForeground
            .onStart {
                scheduleDailyWorker()
            }
            .distinctUntilChanged()
            .filter { it } // Only when app comes to the foreground
            .onEach {
                val updated = cclConfigurationUpdater.updateIfRequired()
                Timber.d("Configuration was updated = %s", updated)
                // dccWalletManager.triggerCalculation(updated)
            }
    }

    private fun scheduleDailyWorker() {
        workManager.enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, buildWorkRequest())
    }

    private fun buildWorkRequest(): PeriodicWorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return PeriodicWorkRequestBuilder<CCLConfigurationUpdateWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setConstraints(constraints).build()
    }
}

private const val WORKER_NAME = "CCLConfigurationUpdateWorker"
