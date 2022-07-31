package task.gg.locationtracker.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import task.gg.locationtracker.data.db.LocationDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        LocationDatabase::class.java,
        LocationDatabase.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideYourDao(db: LocationDatabase) = db.locationDao()
}