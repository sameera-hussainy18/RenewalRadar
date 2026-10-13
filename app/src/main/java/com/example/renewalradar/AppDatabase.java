package com.example.renewalradar;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room database. There is only ever one copy of it (a "singleton").
 */
@Database(entities = {Subscription.class, User.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SubscriptionDao subscriptionDao();

    public abstract UserDao userDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "renewal_radar.db")
                    // Keeps the code simple for a small app. A bigger app would
                    // run database work on a background thread instead.
                    .allowMainThreadQueries()
                    // If the tables change (new version number), start with a fresh database
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
