package zhuoyuan.li.androidvideoplayer.util;

import android.annotation.SuppressLint;

import static java.lang.String.format;

public class TimeUtil {
    @SuppressLint("DefaultLocale")
    public static String formatTimeWhichExist(long duration) {
        duration = duration / 1000;
        long hour = duration / (60 * 60);
        long min = (duration - hour * 60 * 60) / 60;
        long sec = duration - hour * 60 * 60 - min * 60;
        String time;
        if (hour > 0) {
            time = format("%02d : %02d : %02d", hour, min, sec);
        } else {
            time = format("%02d : %02d", min, sec);
        }
        return time;
    }
}
