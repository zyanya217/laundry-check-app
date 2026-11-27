package io.github.zyanya217.laundrycheck.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.zyanya217.laundrycheck.MainActivity
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.logic.hh
import io.github.zyanya217.laundrycheck.logic.taipeiNowIdx
import io.github.zyanya217.laundrycheck.logic.wallHour
import io.github.zyanya217.laundrycheck.model.UserSettings
import io.github.zyanya217.laundrycheck.widget.WidgetData

/**
 * Whether the evening reminder is due: switched on, tonight is good for drying, the chosen hour has come,
 * and none was sent today. [nowIdx] is the Taipei hour index; [lastDay] the day number of the last reminder.
 */
fun shouldNotify(enabled: Boolean, notifyHour: Int, nowIdx: Int, level: Level?, lastDay: Int?): Boolean =
    enabled &&
        level == Level.OK &&
        wallHour(nowIdx) >= notifyHour &&
        Math.floorDiv(nowIdx, 24) != lastDay

/** Posts the "tonight is good for drying" reminder. Called from the hourly refresh worker. */
object ReminderNotifier {
    private const val CHANNEL = "drying_reminder"
    private const val NOTIFICATION_ID = 1
    private const val PREFS = "reminder"
    private const val KEY_LAST_DAY = "last_day"

    @SuppressLint("MissingPermission") // areNotificationsEnabled() covers it: it is false without the permission.
    fun notifyIfDue(context: Context, settings: UserSettings, data: WidgetData, nowIdx: Int = taipeiNowIdx()) {
        val ready = data as? WidgetData.Ready
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastDay = if (prefs.contains(KEY_LAST_DAY)) prefs.getInt(KEY_LAST_DAY, 0) else null
        if (!shouldNotify(settings.notifyEnabled, settings.notifyHour, nowIdx, ready?.level, lastDay)) return

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.notify_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.shirt_ok)
            .setContentTitle(context.getString(R.string.notify_title))
            .setContentText(context.getString(R.string.notify_text, hh(ready!!.startHour), hh(ready.collectHour)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        // Marked only after posting, so a failed attempt is tried again at the next refresh.
        prefs.edit().putInt(KEY_LAST_DAY, Math.floorDiv(nowIdx, 24)).apply()
    }
}
