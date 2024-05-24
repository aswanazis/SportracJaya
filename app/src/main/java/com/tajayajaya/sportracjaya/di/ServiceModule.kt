package com.tajayajaya.sportracjaya.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.extras.Constants
import com.tajayajaya.sportracjaya.ui.activities.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //    ServiceScoped :- for the lifetime of the service their will only be a instance for the provideFusedLocationProviderClient
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0, Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_IMMUTABLE //whenever we launch the pendingIntent and if it already exist then it will update it instead of recreating it.
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(
        app,
        Constants.NOTIFICATION_CHANNEL_ID
    )
        .setAutoCancel(false) //if user click on the notification the will that be disappear (no) -> false
        .setOngoing(true) // notification can't be swiped away
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp) //icon
        .setContentTitle("Running App")//notification title of application
        .setContentText("00:00:00:00") // content inside the notification
        .setContentIntent(pendingIntent)
}