package zhuoyuan.li.androidvideoplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zhuoyuan.li.androidvideoplayer.R;
import zhuoyuan.li.androidvideoplayer.data.VideoInfo;
import zhuoyuan.li.androidvideoplayer.util.Util;

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

    private VideoState mVideoState = VideoState.unKnow;
    private int mDuration = 0;
    private Context mContext;

    private OnProgressChangedListener mOnProgressChangedListener = null;

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mOnProgressChangedListener = listener;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_PROGRESS) {
                if (videoView.isPlaying()) {
                    int currentTime = videoView.getCurrentPosition();
                    if (currentTime >= mDuration) {
                        videoView.seekTo(0);
                        seekBarProgress.setProgress(0);
                        alreadyTextView.setText("00:00");
                        mHandler.removeMessages(UPDATE_PROGRESS);
                    } else {
                        seekBarProgress.setProgress(currentTime);
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                        alreadyTextView.setText(Util.formatTimeWhichExist(currentTime));
                    }
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            alreadyTextView.setText(Util.formatTimeWhichExist(progress));
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
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                } else {
                    mVideoState = VideoState.playEnd;
                    start();
                }
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
        final View videoLayout = LayoutInflater.from(context).inflate(R.layout.video_layout, this, true);
        ButterKnife.bind(this, videoLayout);
        initView();
    }

    private void initView() {
        videoView.setOnInfoListener((mp, what, extra) -> false);

        videoView.setOnPreparedListener(mp -> {
            mVideoState = VideoState.loadFinish;
            totalPlayTextView.setText(Util.formatTimeWhichExist(mDuration));
            videoThumb.setVisibility(GONE);
            start();
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        });

        videoView.setOnCompletionListener(mp -> {
            mHandler.removeMessages(UPDATE_PROGRESS);
            mVideoState = VideoState.playEnd;
            changePlayIcon();
            seekBarProgress.setProgress(0);
            alreadyTextView.setText("00:00");
            videoThumb.setVisibility(VISIBLE);
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            //异常回调
            mVideoState = VideoState.error;
            return false;
        });

        seekBarProgress.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    public void start() {
        if (mVideoState == VideoState.playEnd) {
            videoView.resume();
        } else {
            videoView.start();
        }
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

    public void setVideoVisible() {
        controllerLayout.setVisibility(VISIBLE);
    }

    public void setVideoGone() {
        controllerLayout.setVisibility(GONE);
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
            mDuration = (int) video.getDuration();
            alreadyTextView.setText(Util.formatTimeWhichExist(mDuration));

            seekBarProgress.setMax(mDuration);
            start();

            //宽高比
            int width = video.getWidth();
            int height = video.getHeight();

            float aspectRatio = (float) width / height;
            ConstraintLayout.LayoutParams layoutParamsThumb = (ConstraintLayout.LayoutParams) videoThumb.getLayoutParams(); //取控件textView当前的布局参数
            setLayoutParam(layoutParamsThumb, aspectRatio);
            videoThumb.setLayoutParams(layoutParamsThumb);
        } catch (Throwable ignore) {
        }
    }

    private void changePlayIcon() {
        videoPlayBtn.setBackground(ContextCompat.getDrawable(
                mContext,
                videoView.isPlaying() ?
                        R.mipmap.icon_video_pause :
                        R.mipmap.icon_video_play));
    }

    private void setLayoutParam(ConstraintLayout.LayoutParams layoutParams, float aspectRatio) {
        if (aspectRatio == 1) {
            layoutParams.topMargin = 105;
        } else if (aspectRatio > 1) {
            layoutParams.topMargin = 25;
        } else {
            layoutParams.topMargin = 0;
        }
        layoutParams.height = (int) (Util.getScreenWidth(mContext) / aspectRatio);
    }

    @OnClick(R.id.play_btn)
    public void onViewClicked() {
        videoPlayBtn.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                pause();
            } else {
                start();
            }
        });
    }
}
