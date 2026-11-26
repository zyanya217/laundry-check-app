package io.github.zyanya217.laundrycheck.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Fetches a fresh forecast and redraws every placed widget. */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        // Nothing placed on a home screen: no reason to use the network.
        val manager = GlanceAppWidgetManager(context)
        val placed = listOf(SmallWidget::class.java, MediumWidget::class.java, LargeWidget::class.java)
            .any { manager.getGlanceIds(it).isNotEmpty() }
        if (!placed) return Result.success()

        val fetched = WidgetEntryPoint.from(context).dataSource().refresh()
        SmallWidget().updateAll(context)
        MediumWidget().updateAll(context)
        LargeWidget().updateAll(context)
        return if (fetched) Result.success() else Result.retry()
    }
}

object WidgetRefresh {
    private const val PERIODIC = "laundry_widget_periodic"
    private const val NOW = "laundry_widget_now"
    private const val PERIOD_HOURS = 1L

    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Keeps the widget fresh while at least one is placed. Safe to call again. */
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            .setConstraints(online)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** One refresh as soon as there is a network, e.g. after the settings changed. */
    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().setConstraints(online).build()
        WorkManager.getInstance(context).enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, request)
    }
}
