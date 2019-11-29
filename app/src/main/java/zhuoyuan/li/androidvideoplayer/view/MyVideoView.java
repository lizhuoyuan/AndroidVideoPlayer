package zhuoyuan.li.androidvideoplayer.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zhuoyuan.li.androidvideoplayer.R;
import zhuoyuan.li.androidvideoplayer.data.VideoInfo;
import zhuoyuan.li.androidvideoplayer.util.DensityUtils;
import zhuoyuan.li.androidvideoplayer.util.ScreenUtils;
import zhuoyuan.li.androidvideoplayer.util.TimeUtil;

public class MyVideoView extends ConstraintLayout {

    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);
    }

    private static final int UPDATE_PROGRESS = 1;

    @BindView(R.id.my_video_view)
    VideoView videoView;
    @BindView(R.id.play_btn)
    ImageButton videoPlayBtn;
    @BindView(R.id.seek_bar_progress)
    SeekBar seekBarProgress;
    @BindView(R.id.controller_layout)
    ViewGroup controllerLayout;
    @BindView(R.id.already_play_text)
    TextView alreadyTextView;
    @BindView(R.id.total_play_text)
    TextView totalPlayTextView;
    @BindView(R.id.video_thumb)
    ImageView videoThumb;
    @BindView(R.id.error_layout)
    ViewGroup mErrorLayout;
    @BindView(R.id.error_btn)
    Button mErrorBtn;

    private View videoLayout;

    private VideoState mVideoState = VideoState.unKnow;
    private int mDuration;
    private Context mContext;

    private OnProgressChangedListener mOnProgressChangedListener = null;

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mOnProgressChangedListener = listener;
    }

    private Handler mHandler;

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            alreadyTextView.setText(TimeUtil.formatTimeWhichExist(progress));
            if (mOnProgressChangedListener != null) {
                mOnProgressChangedListener.onProgressChanged(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 暂停刷新
            mHandler.removeMessages(UPDATE_PROGRESS);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (videoView != null) {
                if (progress + 1000 < mDuration) {
                    // 设置当前播放的位置
                    videoView.seekTo(progress);
                } else {
                    mVideoState = VideoState.playEnd;
                    videoView.seekTo(0);
                    start();
                }
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        }
    };

    public enum VideoState {
        unKnow,
        loadFinish,
        playing,
        playEnd,
        error,
        pause
    }

    public MyVideoView(Context context) {
        this(context, null);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        videoLayout = LayoutInflater.from(context).inflate(R.layout.video_layout, this, true);
        ButterKnife.bind(this, videoLayout);
        initView();
    }

    private void initView() {
        videoView.setOnPreparedListener(mp -> {
            mVideoState = VideoState.loadFinish;
            totalPlayTextView.setText(TimeUtil.formatTimeWhichExist(mDuration));
            videoThumb.setVisibility(GONE);
            start();
            if (mHandler != null) {
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });

        videoView.setOnCompletionListener(mp -> {
            mVideoState = VideoState.playEnd;
            mp.start();
            mp.setLooping(true);
            if (mOnProgressChangedListener != null) {
                mOnProgressChangedListener.onProgressChanged(mDuration);
            }
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            //异常回调
            mVideoState = VideoState.error;
            mErrorLayout.setVisibility(VISIBLE);
            setProgressBarVisible(false);
            return true;
        });

        seekBarProgress.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    public void start() {
        if (mVideoState == VideoState.playEnd || mVideoState == VideoState.error) {
            videoView.resume();
            seekBarProgress.setProgress(0);
        } else {
            videoView.start();
        }
        mErrorLayout.setVisibility(GONE);
        mVideoState = VideoState.playing;
        changePlayIcon();
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    public void pause() {
        videoView.pause();
        mVideoState = VideoState.pause;

        changePlayIcon();
    }

    public void stop() {
        videoView.stopPlayback();
        mHandler.removeMessages(UPDATE_PROGRESS);
    }

    public void setProgressBarVisible(boolean visible) {
        controllerLayout.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    public VideoState getState() {
        if (videoView.isPlaying()) {
            mVideoState = VideoState.playing;
        }
        return mVideoState;
    }

    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    public void setVideo(VideoInfo video) {
        try {
            videoView.setVideoURI(Uri.parse(video.getUrl()));
            mDuration = (int) video.getDuration() * 1000;
            alreadyTextView.setText(TimeUtil.formatTimeWhichExist(mDuration));
            seekBarProgress.setMax(mDuration);
            //  CacheImage.load(videoThumb, video.getCoverUrl(), 0);

            if (mHandler == null) {
                mHandler = new MyHandler(
                        videoLayout,
                        mDuration
                );
            }
            //宽高比
            int width = video.getWidth();
            int height = video.getHeight();

            float aspectRatio = (float) width / height;

            LayoutParams layoutParamsVideo = (LayoutParams) videoView.getLayoutParams();
            LayoutParams layoutParamsThumb = (LayoutParams) videoThumb.getLayoutParams();
            setLayoutParam(layoutParamsVideo, aspectRatio);
            setLayoutParam(layoutParamsThumb, aspectRatio);
            videoView.setLayoutParams(layoutParamsVideo);
            videoThumb.setLayoutParams(layoutParamsThumb);

            start();
        } catch (Throwable ignore) {
        }
    }

    private void changePlayIcon() {
        videoPlayBtn.setBackground(ContextCompat.getDrawable(
                mContext,
                videoView.isPlaying() ?
                        R.drawable.icon_video_pause :
                        R.drawable.icon_video_play));
    }

    private void setLayoutParam(LayoutParams layoutParams, float aspectRatio) {
        if (aspectRatio == 1) {
            layoutParams.topMargin = DensityUtils.dp2px(mContext, 105);
        } else if (aspectRatio > 1) {
            layoutParams.topMargin = DensityUtils.dp2px(mContext, 25);
        } else {
            layoutParams.topMargin = 0;
        }
        layoutParams.height = (int) (ScreenUtils.getScreenWidth(mContext) / aspectRatio);
    }

    @OnClick({R.id.play_btn, R.id.error_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.play_btn:
                if (videoView.isPlaying()) {
                    pause();
                } else {
                    start();
                }
                break;
            case R.id.error_btn:
                start();
                break;
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<View> mVideoViewWeakReference;
        private int duration;

        MyHandler(View rootView, int duration) {
            this.mVideoViewWeakReference = new WeakReference<>(rootView);
            this.duration = duration;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            View rootView = mVideoViewWeakReference.get();
            TextView alreadyTextView = rootView.findViewById(R.id.already_play_text);
            SeekBar seekBarProgress = rootView.findViewById(R.id.seek_bar_progress);
            VideoView videoView = rootView.findViewById(R.id.my_video_view);

            if (videoView == null) {
                return;
            }
            if (msg.what == UPDATE_PROGRESS) {
                int currentTime = videoView.getCurrentPosition();
                if (currentTime >= duration) {
                    videoView.seekTo(0);
                    seekBarProgress.setProgress(0);
                    alreadyTextView.setText("00:00");
                } else {
                    seekBarProgress.setProgress(currentTime);
                    sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                    alreadyTextView.setText(TimeUtil.formatTimeWhichExist(currentTime));
                }
            }
        }
    }
}