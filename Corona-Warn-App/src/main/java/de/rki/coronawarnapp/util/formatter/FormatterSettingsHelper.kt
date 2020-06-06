@file:JvmName("FormatterSettingsHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

/*Texter*/

/**
 * Formats the text display of settings item status depending on flag provided
 *
 * @param value
 * @return String
 */
fun formatStatus(value: Boolean): String = formatText(
    value,
    R.string.settings_on,
    R.string.settings_off
)

/**
 * Formats the text display of settings notification status depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return
 */
fun formatNotificationsStatusText(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): String =
    formatStatus((notifications && (notificationsRisk || notificationsTest)))

/**
 * Formats the settings notifications title display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsTitle(notifications: Boolean): String = formatText(
    notifications,
    R.string.settings_notifications_headline_active,
    R.string.settings_notifications_headline_inactive
)

/**
 * Formats the settings notifications description text display depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationsDescription(notifications: Boolean): String = formatText(
    notifications,
    R.string.settings_notifications_body_active,
    R.string.settings_notifications_body_inactive
)

/**
 * Formats the settings notifications details illustration description depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationIllustrationText(notifications: Boolean): String =
    formatText(
        notifications,
        R.string.settings_notifications_illustration_description_active,
        R.string.settings_notifications_illustration_description_inactive
    )

/**
 * Change the tracing text in the row based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return String
 */
fun formatTracingStatusText(tracing: Boolean, bluetooth: Boolean, connection: Boolean): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.CONNECTION, TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_status_restricted)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_status_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_status_inactive)
        else -> ""
    }
}

/**
 * Format the settings tracing description text display depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return String
 */
fun formatTracingDescription(tracing: Boolean, bluetooth: Boolean, connection: Boolean): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.CONNECTION ->
            appContext.getString(R.string.settings_tracing_body_connection_inactive)
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_body_bluetooth_inactive)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_body_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_body_inactive)
        else -> ""
    }
}

/**
 * Formats the tracing body depending on the tracing status and the days since last exposure.
 *
 * @param activeTracingDaysInRetentionPeriod
 * @return String
 */
// TODO add generic plural formatter helper
fun formatTracingStatusBody(activeTracingDaysInRetentionPeriod: Long): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val resources = appContext.resources
    val days = activeTracingDaysInRetentionPeriod.toInt()
    return resources.getQuantityString(R.plurals.settings_tracing_status_body_active, days, days)
}

/**
 * Format the settings tracing content description for the header illustration
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return String
 */
fun formatTracingIllustrationText(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.CONNECTION ->
            appContext.getString(R.string.settings_tracing_connection_illustration_description_inactive)
        TracingStatusHelper.BLUETOOTH ->
            appContext.getString(R.string.settings_tracing_bluetooth_illustration_description_inactive)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getString(R.string.settings_tracing_illustration_description_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getString(R.string.settings_tracing_illustration_description_inactive)
        else -> ""
    }
}

/*Styler*/

/**
 * Formats the settings icon color for notifications depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return Int
 */
fun formatNotificationIconColor(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): Int =
    formatColor(
        (notifications && (notificationsRisk || notificationsTest)),
        R.color.tracingIconActive,
        R.color.tracingIconInactive
    )

/**
 * Formats settings icon color for notifications depending on notification values
 *
 * @param notifications
 * @param notificationsRisk
 * @param notificationsTest
 * @return
 */
fun formatNotificationIcon(
    notifications: Boolean,
    notificationsRisk: Boolean,
    notificationsTest: Boolean
): Drawable? =
    formatDrawable(
        (notifications && (notificationsRisk || notificationsTest)),
        R.drawable.ic_settings_notification_active,
        R.drawable.ic_settings_notification_inactive
    )

/**
 * Formats the settings notifications details illustration depending on notifications status
 *
 * @param notifications
 * @return
 */
fun formatNotificationImage(notifications: Boolean): Drawable? =
    formatDrawable(
        notifications,
        R.drawable.ic_settings_illustration_notification_on,
        R.drawable.ic_settings_illustration_notification_off
    )

/**
 * Formats the settings icon color for tracing depending on tracing values
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return
 */
fun formatSettingsTracingIconColor(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.CONNECTION, TracingStatusHelper.BLUETOOTH ->
            appContext.getColor(R.color.settingsIconInactive)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getColor(R.color.tracingIconActive)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getColor(R.color.tracingIconInactive)
        else -> appContext.getColor(R.color.tracingIconInactive)
    }
}

/**
 * Formats the settings icon for tracing depending on tracing values
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return
 */
fun formatSettingsTracingIcon(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean
): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.CONNECTION,
        TracingStatusHelper.BLUETOOTH,
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_active)
        TracingStatusHelper.TRACING_INACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_inactive)
        else -> appContext.getDrawable(R.drawable.ic_settings_tracing_inactive)
    }
}

/**
 * Formats the tracing switch status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Boolean
 */
fun formatTracingSwitch(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.TRACING_ACTIVE -> true
        else -> false
    }
}

/**
 * Formats the tracing switch enabled status based on the tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Boolean
 */
fun formatTracingSwitchEnabled(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Boolean {
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.TRACING_ACTIVE, TracingStatusHelper.TRACING_INACTIVE -> true
        else -> false
    }
}

/**
 * Formats the main tracing icon depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Drawable
 */
fun formatTracingIcon(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_bluetooth_inactive)
        TracingStatusHelper.CONNECTION ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_connection_inactive)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_active)
        else ->
            appContext.getDrawable(R.drawable.ic_settings_tracing_inactive)
    }
}

/**
 * Formats the main tracing icon color depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Int
 */
fun formatTracingIconColor(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getColor(R.color.tracingIconActive)
        else ->
            appContext.getColor(R.color.tracingIconInactive)
    }
}

/**
 * Formats the settings tracing details illustration depending on tracing status
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Drawable
 */
fun formatTracingStatusImage(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (tracingStatusHelper(tracing, bluetooth, connection)) {
        TracingStatusHelper.BLUETOOTH ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off)
        TracingStatusHelper.CONNECTION ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_connection_off)
        TracingStatusHelper.TRACING_ACTIVE ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_tracing_on)
        else ->
            appContext.getDrawable(R.drawable.ic_settings_illustration_tracing_off)
    }
}

/**
 * Change the visibility of the connection card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Int
 */
fun formatTracingStatusConnection(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            connection
        ) == TracingStatusHelper.CONNECTION
    )

/**
 * Change the visibility of the bluetooth card based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Int
 */
fun formatTracingStatusVisibilityBluetooth(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean
): Int =
    formatVisibility(
        tracingStatusHelper(
            tracing,
            bluetooth,
            connection
        ) == TracingStatusHelper.BLUETOOTH
    )

/**
 * Change the visibility of the tracing text based on the tracing status.
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Int
 */
fun formatTracingStatusVisibilityTracing(
    tracing: Boolean,
    bluetooth: Boolean,
    connection: Boolean
): Int {
    val tracingStatus = tracingStatusHelper(tracing, bluetooth, connection)
    return formatVisibility(
        tracingStatus == TracingStatusHelper.TRACING_ACTIVE ||
                tracingStatus == TracingStatusHelper.TRACING_INACTIVE
    )
}
