package de.rki.coronawarnapp.ccl.configuration.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class CCLConfigurationUpdater @Inject constructor(
    private val cclSettings: CCLSettings,
    private val boosterRulesRepository: BoosterRulesRepository
) {

    suspend fun updateIfRequired(): Boolean {
        Timber.d("update()")

        if (!isUpdateRequired()) {
            Timber.d("No CCLConfig update required!")
            return false
        }

        // TODO
        // Also note that this mechanism shall cause the app to update its configuration when its opened for the
        // first time after an update to a release that supports CCL.

        cclSettings.setExecutionTime()
        return updateConfiguration()
    }

    /**
     * updates the configuration irrespectively of whether it was recently updated
     */
    suspend fun forceUpdate() {
        updateConfiguration()
    }

    private suspend fun updateConfiguration(): Boolean {
        return coroutineScope {
            val newBoosterRulesDownloaded = async { boosterRulesRepository.update() }
            // val newCclConfigDownloaded = async { cclConfigRepository.updateCCLConfig() }

            return@coroutineScope listOf(
                newBoosterRulesDownloaded,
                // newCclConfigDownloaded
            ).awaitAll().any { true }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun isUpdateRequired(now: Instant = Instant.now()): Boolean {
        val lastExecution = cclSettings.getLastExecutionTime() ?: return true

        // update is needed if the last update was on a different day
        return lastExecution.toLocalDateUtc() != now.toLocalDateUtc()
    }
}
