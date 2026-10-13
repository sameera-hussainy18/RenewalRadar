package com.example.renewalradar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Calendar;
import java.util.List;

/** Checks the month-end rule: Jan 31 -> Feb 28 -> Mar 31 -> Apr 30. */
public class RecurrenceEngineTest {

    private static long date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month - 1, day);
        return c.getTimeInMillis();
    }

    @Test
    public void monthlyFromJan31_clampsToMonthEnd() {
        List<Long> d = RecurrenceEngine.nextDueDates(date(2025, 1, 31),
                Subscription.CYCLE_MONTH, 0, date(2025, 1, 1), 4);
        assertEquals(date(2025, 1, 31), (long) d.get(0));
        assertEquals(date(2025, 2, 28), (long) d.get(1));
        assertEquals(date(2025, 3, 31), (long) d.get(2));
        assertEquals(date(2025, 4, 30), (long) d.get(3));
    }

    @Test
    public void monthlyInLeapYear_usesFeb29() {
        List<Long> d = RecurrenceEngine.nextDueDates(date(2024, 1, 31),
                Subscription.CYCLE_MONTH, 0, date(2024, 2, 1), 1);
        assertEquals(date(2024, 2, 29), (long) d.get(0));
    }

    @Test
    public void weeklyAndCustom() {
        assertEquals(date(2025, 1, 15), RecurrenceEngine.dateAt(date(2025, 1, 1),
                Subscription.CYCLE_WEEK, 0, 2));
        assertEquals(date(2025, 1, 21), RecurrenceEngine.dateAt(date(2025, 1, 1),
                Subscription.CYCLE_CUSTOM_DAYS, 10, 2));
    }

    @Test
    public void yearlyFromFeb29() {
        assertEquals(date(2025, 2, 28), RecurrenceEngine.dateAt(date(2024, 2, 29),
                Subscription.CYCLE_YEAR, 0, 1));
    }

    @Test
    public void skipsPastDates() {
        List<Long> d = RecurrenceEngine.nextDueDates(date(2025, 1, 10),
                Subscription.CYCLE_MONTH, 0, date(2025, 6, 11), 1);
        assertEquals(date(2025, 7, 10), (long) d.get(0));
    }
}
