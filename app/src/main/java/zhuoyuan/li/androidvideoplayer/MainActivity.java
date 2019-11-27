package zhuoyuan.li.androidvideoplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import zhuoyuan.li.androidvideoplayer.data.VideoInfo;
import zhuoyuan.li.androidvideoplayer.view.MyVideoView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.video_view)
    MyVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        VideoInfo videoInfo = new VideoInfo(
                "http://cdn.flashgo.online/news-video/abd84aca-a439-4a94-911a-8f38df6a5fab",
                36000,
                640,
                800
        );
        mVideoView.setVideo(videoInfo);
        mVideoView.setVideoVisible();
    }
}
