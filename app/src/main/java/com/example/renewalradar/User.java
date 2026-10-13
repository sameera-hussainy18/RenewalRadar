package com.example.renewalradar;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One row in the "users" table (one per account).
 * The email must be unique, so two accounts cannot share it.
 */
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String email = "";

    // We never store the real password, only a SHA-256 "fingerprint" of it
    @NonNull
    @ColumnInfo(name = "password_hash")
    public String passwordHash = "";
}
