package de.rki.coronawarnapp.risk.changedetection

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/*
* Checks for changes in combined risk and triggers notification and pop up
*
* */
@Suppress("LongParameterList")
class CombinedRiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationHelper: GeneralNotifications,
    private val coronaTestRepository: CoronaTestRepository,
    private val tracingSettings: TracingSettings,
) {

    fun launch() {
        Timber.v("Monitoring combined risk level changes.")
        riskLevelStorage.allCombinedEwPtRiskLevelResults
            .map { results ->
                results
                    .filter { it.wasSuccessfullyCalculated }
                    .sortedBy { it.calculatedAt }
                    .takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach { results ->
                Timber.v("Checking for combined risklevel change.")
                results.checkForRiskLevelChanges()
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    private suspend fun List<CombinedEwPtRiskLevelResult>.checkForRiskLevelChanges() {
        if (isEmpty()) return

        val oldResult = minByOrNull { it.calculatedAt }
        val newResult = maxByOrNull { it.calculatedAt }

        if (oldResult == null || newResult == null) return

        val lastCheckedResult = riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp = newResult.calculatedAt

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState
        Timber.d("Last combined state was $oldRiskState and current state is $newRiskState")

        if (oldResult.riskState.hasChangedFromHighToLow(newResult.riskState)) {
            tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update { true }
            Timber.d("Risk level changed LocalData is updated. Current Risk level is ${newResult.riskState}")
        }

        // Check sending a notification when risk level changes
        val isSubmissionSuccessful = coronaTestRepository.coronaTests.first().any { it.isSubmitted }
        val riskChanged = oldRiskState.hasChangedFromLowToHigh(newRiskState) ||
            oldRiskState.hasChangedFromHighToLow(newRiskState)

        Timber.d("Risk changed=%s from=%s to=%s", riskChanged, oldRiskState, newRiskState)
        if (!isSubmissionSuccessful && riskChanged) {
            Timber.d("Notification Permission = ${notificationManagerCompat.areNotificationsEnabled()}")

            val notification = notificationHelper.newBaseBuilder()
                .setContentTitle(context.getString(R.string.notification_headline))
                .setContentTextExpandable(context.getString(R.string.notification_body))
                .build()

            notificationHelper.sendNotification(
                notificationId = NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID,
                notification = notification,
            )
            tracingSettings.showRiskLevelBadge.update { true }

            Timber.d("Risk level changed and notification/badge sent. Current Risk level is $newRiskState")
        }
    }
}
