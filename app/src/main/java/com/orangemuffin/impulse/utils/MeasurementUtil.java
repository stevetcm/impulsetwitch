package com.orangemuffin.impulse.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/* Created by OrangeMuffin on 2018-03-20 */
public class MeasurementUtil {
    public static int getRealWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
            return metrics.widthPixels;
        } else {
            try {
                return (Integer) Display.class.getMethod("getRawWidth").invoke(display);
            } catch (Exception e) {
                display.getMetrics(metrics);
                return metrics.widthPixels;
            }
        }
    }

    public static int getRealHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
            return metrics.heightPixels;
        } else {
            try {
                return (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                display.getMetrics(metrics);
                return metrics.heightPixels;
            }
        }
    }

    public static float getAspectRatio(Activity activity) {
        float width = getRealWidth(activity);
        float height = getRealHeight(activity);

        if (width > height) {
            return height/width;
        } else {
            return width/height;
        }
    }


    public static int dpToPixel(int dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static String convertTime(long time) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    public static String convertTimeClip(long time) {
        return String.format("%01d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    public static String convertDay(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(date);

            return DateUtils.getRelativeTimeSpanString(d.getTime(), Calendar.getInstance().getTimeInMillis(),
                    DateUtils.MINUTE_IN_MILLIS).toString();
        } catch (Exception e) {
            return "undefined";
        }
    }

    public static String convertDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);

        return strDate;
    }

    public static Date convertStringToDate(String strDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(strDate);
            return date;
        } catch (Exception e) { }

        return null;
    }

    public static long calculateDaysDifference(Date date1, Date date2) {
        long diff = date2.getTime() - date1.getTime();
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        return days;
    }

    public static long calculateHoursDifference(Date date1, Date date2) {
        long diff = date2.getTime() - date1.getTime();
        long diffHours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);

        return diffHours;
    }

    public static String calculateTimeRemaining(long timeInMillis) {
        String timeLeft;
        if (timeInMillis >= 3600000) {
            timeLeft = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % TimeUnit.MINUTES.toSeconds(1));
        } else {
            timeLeft = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
                    TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % TimeUnit.MINUTES.toSeconds(1));
        }

        return timeLeft;
    }
}
