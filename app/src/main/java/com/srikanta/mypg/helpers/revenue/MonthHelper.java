package com.srikanta.mypg.helpers.revenue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthHelper {

    private static final SimpleDateFormat KEY_FMT =
            new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat UI_FMT =
            new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    // ================= FORMAT =================

    // Calendar → yyyy-MM
    public static String getMonthKey(Calendar cal) {
        return KEY_FMT.format(cal.getTime());
    }


    // yyyy-MM → "MMMM yyyy"
    public static String getMonthText(String monthKey) {
        try {
            Calendar cal = getCalendarFromKey(monthKey);
            return UI_FMT.format(cal.getTime());
        } catch (Exception e) {
            return monthKey;
        }
    }

    // ================= CALENDAR =================

    // yyyy-MM → Calendar (SAFE & REUSABLE)
    public static Calendar getCalendarFromKey(String monthKey) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(KEY_FMT.parse(monthKey));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            resetTime(cal);
            return cal;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }

    public static boolean isFutureMonthBeyondNext(String monthKey) {
        Calendar selected = getCalendarFromKey(monthKey);
        Calendar next = getNextMonthCalendar();
        return selected.after(next);
    }

    private static Calendar getCurrentMonthCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(c);
        return c;
    }

    private static Calendar getNextMonthCalendar() {
        Calendar c = getCurrentMonthCalendar();
        c.add(Calendar.MONTH, 1);
        return c;
    }

    private static void resetTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
