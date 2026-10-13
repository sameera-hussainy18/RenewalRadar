package com.example.renewalradar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Works out when a subscription charges next.
 *
 * The trick for month-end dates: every due date is calculated from the ORIGINAL
 * anchor date (anchor + 1 month, anchor + 2 months, ...), never from the previous
 * due date. Java's Calendar clamps to the last day of a short month, so
 * Jan 31 -> Feb 28 (or 29) -> Mar 31 -> Apr 30, and the date never "drifts" to the 28th.
 */
public final class RecurrenceEngine {

    private RecurrenceEngine() {
    }

    /** The k-th charge date (k = 0 is the anchor date itself). */
    public static long dateAt(long anchorMillis, String cycle, int customDays, int k) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anchorMillis);
        switch (cycle) {
            case Subscription.CYCLE_WEEK:
                c.add(Calendar.DAY_OF_MONTH, 7 * k);
                break;
            case Subscription.CYCLE_YEAR:
                c.add(Calendar.YEAR, k);
                break;
            case Subscription.CYCLE_CUSTOM_DAYS:
                c.add(Calendar.DAY_OF_MONTH, Math.max(1, customDays) * k);
                break;
            default: // MONTH
                c.add(Calendar.MONTH, k);
                break;
        }
        return c.getTimeInMillis();
    }

    /** The next N charge dates that fall on or after fromMillis. */
    public static List<Long> nextDueDates(long anchorMillis, String cycle, int customDays,
                                          long fromMillis, int n) {
        List<Long> result = new ArrayList<>();
        int k = 0;
        // skip the dates that are already in the past (safety limit: 100 years of weeks)
        while (k < 5200 && dateAt(anchorMillis, cycle, customDays, k) < fromMillis) {
            k++;
        }
        while (result.size() < n) {
            result.add(dateAt(anchorMillis, cycle, customDays, k));
            k++;
        }
        return result;
    }

    public static long nextDueDate(Subscription s, long fromMillis) {
        return nextDueDates(s.anchorDate, s.cycle, s.customDays, fromMillis, 1).get(0);
    }

    /** How many times this subscription charges between two dates (both inclusive). */
    public static int countChargesBetween(Subscription s, long fromMillis, long toMillis) {
        int count = 0;
        int k = 0;
        long d = dateAt(s.anchorDate, s.cycle, s.customDays, k);
        while (d <= toMillis && k < 5200) {
            if (d >= fromMillis) {
                count++;
            }
            k++;
            d = dateAt(s.anchorDate, s.cycle, s.customDays, k);
        }
        return count;
    }

    /** Total (in cents) that is charged during the current calendar month. */
    public static long totalThisMonthCents(List<Subscription> list) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        setToMidnight(start);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        long endMillis = end.getTimeInMillis() - 1;

        long total = 0;
        for (Subscription s : list) {
            if (Subscription.STATUS_PAUSED.equals(s.status)) {
                continue; // paused subscriptions do not cost anything
            }
            total += s.amountCents * countChargesBetween(s, start.getTimeInMillis(), endMillis);
        }
        return total;
    }

    /** Whole days from today until the given date (0 = today, 1 = tomorrow). */
    public static int daysUntil(long dateMillis) {
        Calendar today = Calendar.getInstance();
        setToMidnight(today);
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(dateMillis);
        setToMidnight(target);
        long diff = target.getTimeInMillis() - today.getTimeInMillis();
        return (int) Math.round(diff / (24.0 * 60 * 60 * 1000));
    }

    public static long todayMillis() {
        Calendar c = Calendar.getInstance();
        setToMidnight(c);
        return c.getTimeInMillis();
    }

    public static void setToMidnight(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    // ---------- small formatting helpers ----------

    public static String formatMoney(long cents) {
        return NumberFormat.getCurrencyInstance().format(cents / 100.0);
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(millis);
    }

    public static String cycleLabel(Subscription s) {
        switch (s.cycle) {
            case Subscription.CYCLE_WEEK:
                return "Weekly";
            case Subscription.CYCLE_YEAR:
                return "Yearly";
            case Subscription.CYCLE_CUSTOM_DAYS:
                return "Every " + s.customDays + " days";
            default:
                return "Monthly";
        }
    }
}
