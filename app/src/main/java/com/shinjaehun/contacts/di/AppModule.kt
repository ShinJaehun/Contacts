package com.shinjaehun.contacts.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.shinjaehun.contacts.data.ContactDataRepositoryImpl
import com.shinjaehun.contacts.data.ContactDatabase
import com.shinjaehun.contacts.data.ImageStorage
import com.shinjaehun.contacts.domain.ContactDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContactDatabase(app: Application): ContactDatabase {
        return Room.databaseBuilder(
            app,
            ContactDatabase::class.java,
            "contacts.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideContactDataRepository(db: ContactDatabase, @ApplicationContext context: Context): ContactDataRepository {
        return ContactDataRepositoryImpl(
            dao = db.contactDao,
            imageStorage = ImageStorage(context = context),
        )
    }
}