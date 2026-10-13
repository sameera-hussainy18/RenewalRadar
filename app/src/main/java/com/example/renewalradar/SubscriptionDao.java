package com.example.renewalradar;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO = Data Access Object. Room writes the SQL code for these methods for us.
 */
@Dao
public interface SubscriptionDao {

    @Insert
    long insert(Subscription subscription);

    @Update
    void update(Subscription subscription);

    @Delete
    void delete(Subscription subscription);

    // Only the subscriptions of the signed-in user
    @Query("SELECT * FROM subscriptions WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    List<Subscription> getAllForUser(long userId);

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    Subscription getById(long id);
}
