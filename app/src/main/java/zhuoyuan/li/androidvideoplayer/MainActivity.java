package zhuoyuan.li.androidvideoplayer;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import zhuoyuan.li.androidvideoplayer.data.VideoInfo;
import zhuoyuan.li.androidvideoplayer.view.MyVideoView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.video_view)
    MyVideoView mVideoView;

/*    @BindView(R.id.surface_view)
    SurfaceView surfaceView;
    private IjkMediaPlayer mPlayer;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        VideoInfo videoInfo = new VideoInfo(
                "http://cdn.flashgo.online/news-video/abd84aca-a439-4a94-911a-8f38df6a5fab",
                36,
                640,
                800
        );
        mVideoView.setVideo(videoInfo);
        mVideoView.setProgressBarVisible(true);
        //  surfaceView.getHolder().addCallback(callback);

    }

    /*private void createPlayer() {
        if (mPlayer == null) {
            mPlayer = new IjkMediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.prepareAsync();
        }
    }

    private void release() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        IjkMediaPlayer.native_profileEnd();
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            createPlayer();
            mPlayer.setDisplay(surfaceView.getHolder());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (surfaceView != null) {
                surfaceView.getHolder().removeCallback(callback);
                surfaceView = null;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }*/

}
