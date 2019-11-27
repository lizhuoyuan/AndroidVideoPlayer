package zhuoyuan.li.androidvideoplayer.data;

import android.text.TextUtils;


public final class VideoInfo {
    private final String mUrl;
    private final long mDuration;
    private final int mWidth;
    private final int mHeight;

    public VideoInfo(
            String url,
            long duration,
            int width,
            int height) {
        if ((url == null) || TextUtils.isEmpty(url.trim())) {
            throw new IllegalArgumentException("url is invalid!");
        }
        mUrl = url;

        mDuration = (duration < 1 ? 1 : duration);

        mWidth = width;

        mHeight = height;
    }

    public String getUrl() {
        return mUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
