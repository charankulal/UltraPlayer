package com.example.videoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ForwardingPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    ExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, prevButton, videoBack, lock, unlock, scaling;

    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    RelativeLayout root;

    //Icon models & playbackiconsadapter
    private ArrayList<IconModel> iconModels = new ArrayList<>();
    PlayBackIconsAdapter playBackIconsAdapter;
    RecyclerView recyclerViewicons;
    boolean expand = false;

    View nightMode;
    boolean dark = false;
    boolean mute=false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view);
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        nextButton = findViewById(R.id.exo_next);
        prevButton = findViewById(R.id.exo_prev);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock_unlock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        nightMode=findViewById(R.id.night_mode);
        recyclerViewicons = findViewById(R.id.recycler_view_icons);
        title.setText(videoTitle);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        iconModels.add(new IconModel(R.drawable.right, ""));
        iconModels.add(new IconModel(R.drawable.baseline_nightlight_24, "Night"));
        iconModels.add(new IconModel(R.drawable.volume_off, "Mute"));
        iconModels.add(new IconModel(R.drawable.rotation, "Rotate"));

        playBackIconsAdapter = new PlayBackIconsAdapter(iconModels, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewicons.setLayoutManager(linearLayoutManager);
        recyclerViewicons.setAdapter(playBackIconsAdapter);
        playBackIconsAdapter.notifyDataSetChanged();
        playBackIconsAdapter.setOnItemClickListener(new PlayBackIconsAdapter.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    if (expand) {
                        iconModels.clear();
                        iconModels.add(new IconModel(R.drawable.right, ""));
                        iconModels.add(new IconModel(R.drawable.baseline_nightlight_24, "Night"));
                        iconModels.add(new IconModel(R.drawable.volume_off, "Mute"));
                        iconModels.add(new IconModel(R.drawable.rotation, "Rotate"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModels.size() == 4) {
                            iconModels.add(new IconModel(R.drawable.volume, "Volume"));
                            iconModels.add(new IconModel(R.drawable.high_brightness, "Brightness"));
                            iconModels.add(new IconModel(R.drawable.equalizer, "Equalizer"));
                            iconModels.add(new IconModel(R.drawable.speed, "Speed"));
                            iconModels.add(new IconModel(R.drawable.subtitle, "Sub Title"));
                        }
                        iconModels.set(position, new IconModel(R.drawable.left_arrow, ""));
                        playBackIconsAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModels.set(position,new IconModel(R.drawable.baseline_nightlight_24,"Night"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);

                        iconModels.set(position, new IconModel(R.drawable.baseline_nightlight_24, "Day"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position == 2) {
                    if(mute){
                        player.setVolume(100);
                        iconModels.set(position,new IconModel(R.drawable.volume_off,"Mute"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        mute=false;
                    }else{
                        player.setVolume(0);
                        iconModels.set(position,new IconModel(R.drawable.volume,"Unmute"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        mute=true;
                    }
                }
                if (position == 3) {
                    Toast.makeText(VideoPlayerActivity.this, "Fourth", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playVideo();

    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new ExoPlayer.Builder(this).setSeekForwardIncrementMs(10000L)
                .setSeekBackIncrementMs(10000L).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "App"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {

        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();


    }

    @Override
    protected void onStop() {

        super.onStop();
        player.stop();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        player.pause();

    }

    @Override
    protected void onResume() {

        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.video_back) {
            if (player != null) {
                player.release();

            }
            finish();
        }
        if (v.getId() == R.id.lock_unlock) {

            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();

        }
        if (v.getId() == R.id.unlock) {
            controlsMode = ControlsMode.LOCK;
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();

        }

        if (v.getId() == R.id.exo_next) {
            try {
                player.stop();
                position++;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Next Video in PlayList", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (v.getId() == R.id.exo_prev) {
            try {
                player.stop();
                position--;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Previous Video in PlayList", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);
            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);

        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);
            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };
}