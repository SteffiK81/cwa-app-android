package de.rki.coronawarnapp.ccl.configuration.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

class DefaultCCLConfigurationProvider @Inject constructor(
    @AppContext private val context: Context
) {

    fun loadDefaultCCLConfigurationsRawData(): ByteArray = context.assets.open(ASSET_DEFAULT_CCL_CONFIGURATION)
        .use { it.readBytes() }
        .also { Timber.tag(TAG).d("Loaded default ccl config") }
}

private val TAG = tag<DefaultCCLConfigurationProvider>()

@VisibleForTesting const val ASSET_DEFAULT_CCL_CONFIGURATION = "ccl/ccl-configuration.bin"
