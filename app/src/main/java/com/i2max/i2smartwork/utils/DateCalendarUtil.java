package com.i2max.i2smartwork.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by berserk1147 on 15. 8. 1..
 */
public class DateCalendarUtil {

    public static Date getDateFromYYYYMMDDHHMM(String strDateTime) {
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

        try {
            date = dateFormat.parse(strDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String getStringFromYYYYMMDDHHMM(String strDateTime) {
        String dateStr = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

        try {
            Date date = dateFormat.parse(strDateTime);
            dateStr = stringFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static String getKoreanYMDEFromYYYYMMDD(String strDateTime) {
        String dateStr = "";
        SimpleDateFormat dfOrigin = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);

        try {
            Date date = dfOrigin.parse(strDateTime);
            dateStr = df.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static String getKoreanShortYMDEFromYYYYMMDD(String strDateTime) {
        String dateStr = "";
        SimpleDateFormat dfOrigin = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat df = new SimpleDateFormat("MM월 dd일(E)", Locale.KOREAN);

        try {
            Date date = dfOrigin.parse(strDateTime);
            dateStr = df.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static String getStringFromYYYYMMDDHHMMSS(String strDateTime) {
        String dateStr = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        try {
            Date date = dateFormat.parse(strDateTime);
            dateStr = stringFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static String getKoreaMDFromYYYYMMDDHHMMSS(String strDateTime) {
        String dateStr = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat stringFormat = new SimpleDateFormat("MM월 dd일");

        try {
            Date date = dateFormat.parse(strDateTime);
            dateStr = stringFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }

    public static String getYMDHSFromCalendar(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(cal.getTime());
    }

    public static String getStringFromBetweenNow(String strDateTime) {
        String termStr = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            Date oldDate = dateFormat.parse(strDateTime);

            Date currentDate = new Date();

            long diff = currentDate.getTime() - oldDate.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long years = days / 365;

            if (hours < 1 ) {
                termStr = "방금전";
            } else if (days < 2) {
                termStr = "어제";
            } else if (days < 3) {
                termStr = "그저께";
            } else if (days >= 3) {
                termStr = String.format("%d일전", days);
            } else if (days > 30) {
                termStr = String.format("%d달전", days/30);
            } else if (years > 0) {
                termStr = String.format("%d년전", years);
            }

        } catch (ParseException e) {

            e.printStackTrace();
        }

        return termStr;
    }

    public static int getMonthBetweenCalendar(Calendar c1, Calendar c2) {
        int diffYear = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);

        return diffMonth;
    }

    public static Calendar getCalenderFromYYYYMMDDHHSS(String strDateTime) {
        Calendar cal = Calendar.getInstance();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = dateFormat.parse(strDateTime);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal;
    }

    public static Calendar getCalenderFromYYYYMMDDHH(String strDateTime) {
        Calendar cal = Calendar.getInstance();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            Date date = dateFormat.parse(strDateTime);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal;
    }
}
