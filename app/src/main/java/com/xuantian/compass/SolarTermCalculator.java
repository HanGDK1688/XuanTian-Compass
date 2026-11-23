package com.xuantian.compass;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public final class SolarTermCalculator {

    private static final String FILE_NAME =
            "solar_terms_1900_2052.csv";

    private static final TimeZone BEIJING_TIME_ZONE =
            TimeZone.getTimeZone("GMT+08:00");

    private static final Map<String, Long> TERM_TIMES =
            new HashMap<>();

    private static boolean loaded = false;

    private SolarTermCalculator() {
    }

    /**
     * 读取 assets 中的节气 CSV 文件。
     */
    public static synchronized void load(Context context) {

        if (loaded) {
            return;
        }

        try {
            InputStream input = context.getAssets()
                    .open(FILE_NAME);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            input, StandardCharsets.UTF_8));

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                if (firstLine) {
                    line = line.replace("\uFEFF", "");
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length < 7) {
                    continue;
                }

                int year = Integer.parseInt(parts[0].trim());

                String termName = parts[2].trim();
                String date = parts[4].trim();
                String time = parts[5].trim();

                String dateTime = date + " " + time;

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.US);

                format.setLenient(false);
                format.setTimeZone(BEIJING_TIME_ZONE);

                long timestamp = format.parse(dateTime).getTime();

                String key = makeKey(year, termName);

                TERM_TIMES.put(key, timestamp);
            }

            reader.close();
            loaded = true;

        } catch (Exception e) {
            TERM_TIMES.clear();
            loaded = false;
            throw new IllegalStateException(
                    "节气数据读取失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取指定年份、指定节气的北京时间时间戳。
     */
    public static synchronized long getSolarTermTime(
            int year, String termName) {

        if (!loaded) {
            throw new IllegalStateException(
                    "请先调用 SolarTermCalculator.load(context)");
        }

        String key = makeKey(year, termName);

        Long result = TERM_TIMES.get(key);

        if (result == null) {
            throw new IllegalArgumentException(
                    "找不到节气数据：" + year + "年" + termName);
        }

        return result;
    }

    /**
     * 获取指定年份立春的北京时间时间戳。
     */
    public static long getLiChunTime(int year) {
        return getSolarTermTime(year, "立春");
    }

    /**
     * 判断指定时间是否已经过立春。
     */
    public static boolean isAfterLiChun(
            int year, long timestamp) {

        return timestamp >= getLiChunTime(year);
    }

    /**
     * 根据北京时间确定当前使用的节气年份。
     */
    public static int getEffectiveYear(long timestamp) {

        Calendar calendar = Calendar.getInstance(
                BEIJING_TIME_ZONE);

        calendar.setTimeInMillis(timestamp);

        int year = calendar.get(Calendar.YEAR);

        if (timestamp < getLiChunTime(year)) {
            return year - 1;
        }

        return year;
    }

    /**
     * 获取当前北京时间对应的节气年份。
     */
    public static int getCurrentEffectiveYear() {

        return getEffectiveYear(
                System.currentTimeMillis());
    }

    private static String makeKey(
            int year, String termName) {

        return year + "_" + termName;
    }
}
