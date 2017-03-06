package com.i2max.i2smartwork.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FormatUtil {

    private static final TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
    private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat DATETIME_FORMATTER1 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static final SimpleDateFormat DATETIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static boolean isThisDateValid(String dateToValidate, String dateFromat){
        if (TextUtils.isEmpty(dateToValidate)) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
        sdf.setLenient(false);

        try {

            //if not valid, it will throw ParseException
            Date date = sdf.parse(dateToValidate);
            System.out.println(date);

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getSendableFormatNow() {
        Date today = new Date();
        DATE_FORMATTER1.setTimeZone(timeZone);
        String a = DATETIME_FORMATTER2.format(today);
        return a;
    }

    public static String getStringDate3toDateTime(String date) {
        if (TextUtils.isEmpty(date)) return "";

        Date d = null;
        try {
            DATE_FORMATTER3.setTimeZone(timeZone);
            d = DATE_FORMATTER3.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return DATETIME_FORMATTER.format(d);
    }


    public static String getFormattedDateTime(String date) {
        if (TextUtils.isEmpty(date))
            return "";

        Date d = null;
        try {
            DATETIME_FORMATTER.setTimeZone(timeZone);
            d = DATETIME_FORMATTER.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return DATETIME_FORMATTER1.format(d);
    }

    public static String getFormattedDateTime2(String date) {
        if (TextUtils.isEmpty(date))
            return "";

        Date d = null;
        try {
            DATETIME_FORMATTER.setTimeZone(timeZone);
            d = DATETIME_FORMATTER.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return DATE_FORMATTER4.format(d);
    }

    public static String getFormattedDateTime3(String date) {
        if (TextUtils.isEmpty(date))
            return "";

        Date d = null;
        try {
            DATETIME_FORMATTER.setTimeZone(timeZone);
            d = DATETIME_FORMATTER.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return DATE_FORMATTER3.format(d);
    }

    private static final SimpleDateFormat TIME_FORMATTER1 = new SimpleDateFormat("HHmmss");
    private static final SimpleDateFormat TIME_FORMATTER2 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat TIME_FORMATTER3 = new SimpleDateFormat("HHmmssSS");
    private static final SimpleDateFormat TIME_FORMATTER4 = new SimpleDateFormat("HH:mm");
    /**
     * 151215와 같은 형식으로 입력된 문자열을 15:12:15와 같은 문자열로 포매팅해서 내보낸다.
     *
     * @param unformatted "151215"와 같이 포매팅되지 않은 날짜를 나타내는 문자열
     * @return "15:12:15"와 같이 포맷팅된 문자열. 과정에 오류가 발생하면 포맷되지 않은 값이라도 표현하도록 입력 값을 그대로 돌려보낸다.
     */
    public static String getFormattedTime(String unformatted) {
        if (TextUtils.isEmpty(unformatted)) return "";

        if (unformatted.length() == 8)
            unformatted = unformatted.substring(0, 6);

        Date d = null;
        try {
            TIME_FORMATTER1.setTimeZone(timeZone);
            d = TIME_FORMATTER1.parse(unformatted);
        } catch (ParseException e) {
            return unformatted;
        }

        return TIME_FORMATTER2.format(d);
    }

    public static String getFormattedCfrcTime(String unformatted) {
        if (TextUtils.isEmpty(unformatted)) return "";
        if (unformatted.length() != 4) return "";

        return unformatted.substring(0,2) + ":" + unformatted.substring(2);
    }

    private static final SimpleDateFormat DATE_FORMATTER1 = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat DATE_FORMATTER2 = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat DATE_FORMATTER3 = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_FORMATTER4 = new SimpleDateFormat("yyyy.MM.dd");
    private static final SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("yy.MM.dd");
    private static final SimpleDateFormat SHORT_DATE_FORMATTER2 = new SimpleDateFormat("MM/dd");

    public static String getSendableFormatToday() {
        Date today = new Date();
        DATE_FORMATTER1.setTimeZone(timeZone);
        String a = DATE_FORMATTER1.format(today);
        return a;
    }

    public static String getSendableFormat3Today() {
        Date today = new Date();
        DATE_FORMATTER1.setTimeZone(timeZone);
        String a = DATE_FORMATTER3.format(today);
        return a;
    }

    public static String getFormattedDate(String date) {
        if (TextUtils.isEmpty(date)) return "";

        Date d = null;
        try {
            DATE_FORMATTER1.setTimeZone(timeZone);
            d = DATE_FORMATTER1.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return DATE_FORMATTER2.format(d);
    }

    public static String getFormattedDate2(Date date) {
        DATE_FORMATTER2.setTimeZone(timeZone);
        return DATE_FORMATTER2.format(date);
    }

    public static String getFormattedDate3(String date) {
        if (TextUtils.isEmpty(date)) return "";
        Log.e("", "getFormattedDate3 date = " + date);
        Date d = null;
        try {
            d = DATE_FORMATTER1.parse(date);
        } catch (ParseException e) {
            return date;
        }
        return getFormattedDate3(date);
    }

    public static String getFormattedDate3(Date date) {
        return DATE_FORMATTER3.format(date);
    }

    public static String getFormattedDate3(Calendar date) {
        return DATE_FORMATTER3.format(date);
    }

    public static String getFormattedDate4(String date) {
        if (TextUtils.isEmpty(date)) return "";
        if(date.length() != 8) return "";

        return date.substring(0, 4)+"."+date.substring(4,6)+"."+date.substring(6);
    }

    public static String getFormattedDate5(String date) {
        if (TextUtils.isEmpty(date)) return "";
        if(date.length() != 8) return "";

        return date.substring(0, 4)+"-"+date.substring(4,6)+"-"+date.substring(6);
    }

    /**
     * 15121511와 같은 형식으로 입력된 문자열을 15:12:15와 같은 문자열로 포매팅해서 내보낸다.
     *
     * @param unformatted "151215"와 같이 포매팅되지 않은 날짜를 나타내는 문자열
     * @return "15:12:15"와 같이 포맷팅된 문자열. 과정에 오류가 발생하면 포맷되지 않은 값이라도 표현하도록 입력 값을 그대로 돌려보낸다.
     */
    public static String getFormattedTimeMilliCut(String unformatted) {
        if (TextUtils.isEmpty(unformatted)) return "";
        Date d = null;
        try {
            DATE_FORMATTER1.setTimeZone(timeZone);
            d = DATE_FORMATTER1.parse(unformatted);
        } catch (ParseException e) {
            return unformatted;
        }

        return TIME_FORMATTER2.format(d);
    }

    /**
     * 20090102형식의 문자열을 09.01.02 형식을 바꿔준다.
     *
     * @param date 20090102 형식의 날짜를 나타내는 문자열
     * @return 09.01.02 형식으로 변환된 날짜 문자열
     */
    public static String getShortDateFormat(String date) {
        if (TextUtils.isEmpty(date)) return "";

        Date d = null;
        try {
            DATE_FORMATTER1.setTimeZone(timeZone);
            d = DATE_FORMATTER1.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return SHORT_DATE_FORMATTER.format(d);
    }

    /**
     * 20090102형식의 문자열을 01.02 형식을 바꿔준다.
     *
     * @param date 20090102 형식의 날짜를 나타내는 문자열
     * @return 09.01.02 형식으로 변환된 날짜 문자열
     */
    public static String getShortDateFormat2(String date) {
        if (TextUtils.isEmpty(date)) return "";

        Date d = null;
        try {
            DATE_FORMATTER1.setTimeZone(timeZone);
            d = DATE_FORMATTER1.parse(date);
        } catch (ParseException e) {
            return date;
        }

        return SHORT_DATE_FORMATTER2.format(d);
    }
    /**
     * 현재날짜에서 원하는 날짜만큼 더하거나 빼서 20100608 형식으로 반환해준다.
     *
     * @param field Calendar클래스의 DATE, MONTH, YEAR와 같은 상수
     * @param value 변경하고 싶은 날짜값
     * @return 20100608 형식으로 변환된 날짜 문자열
     */
    public static String getBeforeDateFormat(int field, int value){
        Calendar cal = Calendar.getInstance();
        cal.add(field, value);

        return String.format("%04d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static String addComma(String value) {
        if (TextUtils.isEmpty(value))
            return "";

        String formatted;
        NumberFormat formatter = new DecimalFormat("#,###");
        try {
            Number n = NumberFormat.getInstance().parse(value);
            formatted = formatter.format(n);
        } catch (ParseException e) {
            formatted = value;
        }

        return formatted;
    }
    public static String formatDecimalDot2(String value) {
        String formatted;
        NumberFormat formatter = new DecimalFormat("###,###,##0.00");
        try {
            Number n = NumberFormat.getInstance().parse(value);
            formatted = formatter.format(n);
        } catch (ParseException e) {
            formatted = value;
        }

        return formatted;
    }

    public static String addDot2(String value) {
        String formatted;
        NumberFormat formatter = new DecimalFormat("0.00");
        try {
            Number n = NumberFormat.getInstance().parse(value);
            formatted = formatter.format(n);
        } catch (ParseException e) {
            formatted = value;
        }

        return formatted;
    }

    //Percentage 형태로 변환
    public static String getFormatedPercentage(String value) {
        if (value == null || value.equals(""))
            return "";
        String rtnVal;

        String dot = FormatUtil.addDot2(value);

        if (dot.equals("0.00")) {
            rtnVal = "0%";
        } else {
            if (dot.startsWith("-")) {
                rtnVal = dot + "%";
            } else {
                rtnVal = dot + "%";
            }
        }

        return rtnVal;
    }

    public static String addDot3(String value) {
        String formatted;
        NumberFormat formatter = new DecimalFormat("0.000");
        try {
            Number n = NumberFormat.getInstance().parse(value);
            formatted = formatter.format(n);
        } catch (ParseException e) {
            formatted = value;
        }

        return formatted;
    }

    public static String removeComma(String value) {
        if (value == null)
            return "";
        return value.replaceAll(",", "");
    }

    // 특정 문자 제거
    public static String removeStr(String str1, String str2) {
        if (str1 == null || str1.equals(""))
            return "";

        String formatted;
        formatted = str1.replaceAll(str2, "");

        return formatted;
    }
    /**
     * 123,456,789와 같이 콤마가 포함된 숫자를 길이에 따라서 123,456천이나 123백만으로 치환해서 반환한다.
     *
     * 참고: 하이닉스 상장주 수 5,239,972,000 > 5,239백만 (우선 반올림 제외)
     *
     * @param input 콤마가 포함된 숫자를 나타내는 문자열
     * @return 줄어든 포맷의 숫자를 나타내는 문자열. 예: 123,456천
     */
    public static String getShortenCountFormat(String input){
        if (input.length() > 11) {
            return input.substring(0, input.length() - 8) + "백만";
        } else if (input.length() > 7) {
            return input.substring(0, input.length() - 4) + "천";
        } else {
            return input;
        }
    }

    public static String getShortenMoneyFormat(String input) {
        if (input == null || input.equals(""))
            return "";

        NumberFormat format = NumberFormat.getInstance();
        String result;
        try {
            double n = format.parse(input).doubleValue();
            if (n >= 1000000000d) {
                long l = Math.round(n/100000000d);
                result = format.format(l) + "억";
            } else if (n >= 10000000) {
                long l = Math.round(n/1000d);
                result = format.format(l) + "천";
            } else {
                result = format.format(n);
            }
        } catch (ParseException e) {
            return input;
        }

        return result;
    }

    /**
     * 백만원 to 억원
     * @param million 백만원단위 금액
     * @return 억원단위 금액
     */
    public static String millionToHundredMillion(String million) {
        Number m = getNumberFormat(million);
        long h = Math.round(m.doubleValue() / 100d);
        return Long.toString(h);
    }

    /**
     * 천원 to 억원
     *
     * @param thousand 천원단위 금액
     * @return 억원단위 금액
     */
    public static String thousandToHundredMillion(String thousand) {
        Number t = getNumberFormat(thousand);
        long h = Math.round(t.doubleValue() / 100000d);
        return Long.toString(h);
    }
    /**
     * 일원 to 백만원
     *
     * @param won 일원단위 금액
     * @return 백만단위 금액
     */
    public static String ToMillion(String won) {
        Number t = getNumberFormat(won);
        long h = Math.round(t.doubleValue() / 1000000d);
        return Long.toString(h);
    }
    /**
     * 지정한 길이가 될 때까지 입력 값의 오른쪽에 선택한 문자를 채운다.
     *
     * @param input 입력 문자열
     * @param length 돌려 받으려는 문자열의 길이
     * @param fill 채우려는 문자
     * @return fill로 오른쪽 여백을 채운 문자열
     */
    public static String fillRight(String input, int length, char fill) {
        String s = padRight(input, length);
        return s.replaceAll("\\s", Character.toString(fill));
    }

    /**
     * 지정한 길이가 될 때까지 공백으로 채운다.
     *
     * @param input 입력 문자열
     * @param length 원하는 길이
     * @return 원하는 길이까지 공백으로 오른쪽을 채운 문자열
     */
    public static String padRight(String input, int length) {
        String output = String.format("%1$-" + length + "s", input);

        // 문자수로 오른쪽으로 채우는 것이 아니라 바이트로 채워야한다.
        // 1 바이트 문자가 아닌 경우 바이트로 계산해서 잘라낸다.
        try {
            if (output.length() != output.getBytes("euc-kr").length
                    && output.getBytes("euc-kr").length > length) {
                byte[] b = new byte[length];
                System.arraycopy(output.getBytes("euc-kr"), 0, b, 0, length);
                try {
                    output = new String(b, "euc-kr");
                } catch (UnsupportedEncodingException e) {
                    output = new String(b);
                }
            }
        } catch (UnsupportedEncodingException e) {
            //
        }


        return output;
    }

    public static String padLeft(int input, int length) {
        String output = String.format("%0"+length + "d", input);

        return output;
    }

    /**
     * 콤마가 포함된 값을 나타내는 문자열에서 콤마를 제거하고 숫자로 변환한다.
     *
     * @param moneyFormat 콤마를 포함한 값. 반드시 Valid한 값을 전달해야 한다.
     * @return 콤마를 제거한 Number 값. 파싱에 실패하면 Null
     */
    public static Number getNumberFormat(String moneyFormat) {
        // null일 때 NumberFormatException이 발생하지 않고 그냥 죽어버리는 현상있음.
        if (moneyFormat == null)
            return null;

        NumberFormat format = NumberFormat.getInstance();
        Number num;
        try {
            num = format.parse(moneyFormat);
        } catch (ParseException e) {
            return null;
        }
        return num;
    }

    /**
     * 콤마가 붙은 두 개의 숫자를 좌변-우변으로 계산해서 다시 콤마를 넣어서 반환한다.
     * */
    public static String subtract(String left, String right) {
        if (left == null || right == null || "".equals(left) || "".equals(right))
            return "";

        int a = FormatUtil.getNumberFormat(left).intValue();
        int b = FormatUtil.getNumberFormat(right).intValue();

        return addComma(Integer.toString(a - b));
    }

    public static String toEUC(String inStr) {
        Charset charset = Charset.forName("euc-kr");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();

        String result = null;
        try {
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inStr));
            CharBuffer cbuf = decoder.decode(bbuf);
            result = cbuf.toString();
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 유니코드 공백을 포함한 문자열 Trimming
     */
    public static String trimUnknown(String inStr){
        if(inStr == null)
            return "";

        return inStr.trim().replaceAll(Character.toString((char)12288), "");
    }

    public static String getDimenString(String input, int maxLength) {
        String result = input;

        if( input.length() > maxLength ) {
            result = input.substring(0, maxLength - 1) + "...";
        }

        return result;
    }

    public static String removeChar(String str, char c) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == c) continue;

            sb.append(str.charAt(i));
        }

        return sb.toString();
    }


    public static String getStringValidate(Object value) {
        String result = null;
        if(value == null || TextUtils.isEmpty(value.toString()) || "null".equals(value.toString()))
            result = "";
        else result = value.toString();
        return result;
    }

    public static String checkNullString(Object objStr) {
        if (objStr == null) return "";

        return objStr.toString();
    }
}
