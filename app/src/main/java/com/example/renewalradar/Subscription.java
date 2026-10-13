package com.example.renewalradar;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One row in the "subscriptions" table.
 * Every field below becomes a column in the Room database.
 */
@Entity(tableName = "subscriptions")
public class Subscription {

    // Billing cycles (chosen with the ChipGroup)
    public static final String CYCLE_WEEK = "WEEK";
    public static final String CYCLE_MONTH = "MONTH";
    public static final String CYCLE_YEAR = "YEAR";
    public static final String CYCLE_CUSTOM_DAYS = "CUSTOM_DAYS";

    // Status values
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_TRIAL = "TRIAL";
    public static final String STATUS_PAUSED = "PAUSED";

    @PrimaryKey(autoGenerate = true)
    public long id;

    // Which account this subscription belongs to (users.id)
    @ColumnInfo(name = "user_id")
    public long userId;

    @NonNull
    public String name = "";

    @NonNull
    public String category = "Other";

    // Money is stored in cents (1499 = 14.99) so there are no rounding errors
    @ColumnInfo(name = "amount_cents")
    public long amountCents;

    @NonNull
    public String cycle = CYCLE_MONTH;

    // Only used when cycle = CUSTOM_DAYS (for example "every 10 days")
    @ColumnInfo(name = "custom_days")
    public int customDays = 30;

    // First charge date, stored as milliseconds
    @ColumnInfo(name = "anchor_date")
    public long anchorDate;

    @NonNull
    public String status = STATUS_ACTIVE;

    public String notes;
}
