package zhuoyuan.li.androidvideoplayer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import static java.lang.String.format;

public final class Util {
    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(outMetrics);
        }
        return outMetrics.widthPixels;
    }

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
