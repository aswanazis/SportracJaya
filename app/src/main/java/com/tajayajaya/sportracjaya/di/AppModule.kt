package com.tajayajaya.sportracjaya.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.tajayajaya.sportracjaya.db.RunningDatabase
import com.tajayajaya.sportracjaya.extras.Constants.KEY_FIRST_TIME_TOGGLE
import com.tajayajaya.sportracjaya.extras.Constants.KEY_NAME
import com.tajayajaya.sportracjaya.extras.Constants.KEY_WEIGHT
import com.tajayajaya.sportracjaya.extras.Constants.RUNNING_DATABASE_NAME
import com.tajayajaya.sportracjaya.extras.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun providesSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)


    @Singleton
    @Provides
    //some time this sharePref behave unlikely if take the string value as null for that we are using null check
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) = sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true
    )

}

/**
 * In the inside dagger we had to create our own ApplicationComponent but in new Dagger we don't to create it
 */