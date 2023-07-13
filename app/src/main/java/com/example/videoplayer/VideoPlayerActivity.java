package com.example.videoplayer;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    ExoPlayer player;
    int position;
    String videoTitle;
    TextView title,vol_num,bright_num;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, prevButton, videoBack, lock, unlock, scaling, videoList;
    VideoFilesAdapter videoFilesAdapter;

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
    PlaybackParameters parameters;
    float speed;
    PictureInPictureParams.Builder pip;
    boolean isCrossChecked;
    
    //Swipe and zoom features
    private int device_height, device_width,brightness,media_volume;
    boolean start=false;
    boolean left,right;
    private float baseX,baseY;
    boolean swipeMove=false,success;
    private long diffX,diffY;
    public static final int MINIMUM_DISTANCE=100;
    TextView vol_text,brt_text;
    ProgressBar vol_progress,brt_progress;
    LinearLayout vol_progress_container,vol_text_container,brt_progress_container,brt_text_container;
    ImageView vol_icon,brt_icon;
    AudioManager audioManager;

    private ContentResolver contentResolver;
    private Window window;

    boolean singleTap=false;
    boolean doubleTap=false;

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
        screenOrientaion();
        initViews();
        playVideo();
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width=displayMetrics.widthPixels;
        device_height=displayMetrics.heightPixels;
        
        playerView.setOnTouchListener(new OnSwipeListener(this){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        playerView.showController();
                        start=true;
                        if(event.getX()<(device_width/2))
                        {
                            left=true;
                            right=false;
                        }else if (event.getX()>(device_width/2))
                        {
                            left=false;
                            right=true;
                        }
                        baseX=event.getX();
                        baseY=event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        swipeMove=true;
                        diffX= (long) Math.ceil(event.getX()-baseX);
                        diffY= (long) Math.ceil(event.getY()-baseY);
                        double brightnessSpeed=0.01;
                        if(Math.abs(diffY)>MINIMUM_DISTANCE){
                            start=true;
                            if(Math.abs(diffY)>Math.abs(diffX))
                            {
                                boolean value;
                                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                                    value= Settings.System.canWrite(getApplicationContext());
                                    if(value){
                                        if (left){
                                            //Toast.makeText(VideoPlayerActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                                            contentResolver=getContentResolver();
                                            window=getWindow();
                                            try{
                                                Settings.System.putInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                                brightness=Settings.System.getInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS);
                                            } catch (Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                            int new_brightness= (int) (brightness-(diffY+brightnessSpeed));
                                            if(new_brightness>250){
                                                new_brightness=250;

                                            } else if (new_brightness<1)
                                            {
                                                new_brightness=1;
                                            }
                                            double brt_percentage=Math.ceil((((double) new_brightness/(double) 250)*(double) 100));
                                            brt_progress_container.setVisibility(View.VISIBLE);
                                            brt_text_container.setVisibility(View.VISIBLE);
                                            brt_progress.setProgress((int) brt_percentage);

                                            if(brt_percentage<30){
                                                brt_icon.setImageResource(R.drawable.round_brightness_low_24);
                                            } else if (brt_percentage>30 && brt_percentage<80) {
                                                brt_icon.setImageResource(R.drawable.brt_med);

                                            }else if (brt_percentage>80)
                                            {
                                                brt_icon.setImageResource(R.drawable.high_brightness);
                                            }
                                            brt_text.setText(" "+(int) brt_percentage+"%");
                                            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS,(new_brightness));
                                            WindowManager.LayoutParams layoutParams=window.getAttributes();
                                            layoutParams.screenBrightness=brightness/(float) 255;
                                            window.setAttributes(layoutParams);
                                        }else if (right){
                                            //Toast.makeText(VideoPlayerActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                                            vol_text_container.setVisibility(View.VISIBLE);
                                            media_volume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                            int maxVol=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            double cal=(double) diffY+(((double) maxVol/(double) (device_height*2)-brightnessSpeed));
                                            int newMediaVolume=media_volume-(int) cal;
                                            if(newMediaVolume>maxVol)
                                            {
                                                newMediaVolume=maxVol;
                                            }else if (newMediaVolume<1)
                                            {
                                                newMediaVolume=0;
                                            }
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,newMediaVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            double volPer=Math.ceil((((double) newMediaVolume/(double) maxVol*(double) 100)));
                                            vol_text.setText(""+(int) volPer+"%");
                                            if(volPer<1){
                                                vol_icon.setImageResource(R.drawable.volume_off);
                                                vol_text.setVisibility(View.VISIBLE);
                                                vol_text.setText("Off");
                                            } else if (volPer>=1) {
                                                vol_icon.setImageResource(R.drawable.volume);
                                                vol_text.setVisibility(View.VISIBLE);

                                            }
                                            vol_progress_container.setVisibility(View.VISIBLE);
                                            vol_progress.setProgress((int) volPer);
                                        }
                                        success=true;
                                    }else {
                                        Toast.makeText(VideoPlayerActivity.this, "Allow write settings ofr swipe controls", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                        intent.setData(Uri.parse("package"+getPackageName()));
                                        startActivityForResult(intent,111);
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        swipeMove=false;
                        start=false;
                        vol_progress_container.setVisibility(View.GONE);
                        brt_progress_container.setVisibility(View.GONE);
                        vol_text_container.setVisibility(View.GONE);
                        brt_text_container.setVisibility(View.GONE);
                        break;
                }
                return super.onTouch(v, event);
            }

            @Override
            public void onDoubleTouch() {
                super.onDoubleTouch();

                if(doubleTap){
                    player.setPlayWhenReady(true);
                    doubleTap=false;
                }else {
                    player.setPlayWhenReady(false);
                    doubleTap=true;
                }
            }

            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                if(singleTap){
                    playerView.showController();
                    singleTap=false;
                }else {
                    playerView.hideController();
                    singleTap=true;
                }
            }
        });
        
        horizontalIconList();
        

    }

    private void horizontalIconList() {
        iconModels.add(new IconModel(R.drawable.right, ""));
        iconModels.add(new IconModel(R.drawable.baseline_nightlight_24, "Night"));
        iconModels.add(new IconModel(R.drawable.pip, "PIP"));
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
                        iconModels.add(new IconModel(R.drawable.pip, "PIP"));
                        iconModels.add(new IconModel(R.drawable.volume_off, "Mute"));
                        iconModels.add(new IconModel(R.drawable.rotation, "Rotate"));
                        playBackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModels.size() == 5) {
                            iconModels.add(new IconModel(R.drawable.volume, "Volume"));
                            iconModels.add(new IconModel(R.drawable.high_brightness, "Brightness"));
                            iconModels.add(new IconModel(R.drawable.equalizer, "Equalizer"));
                            iconModels.add(new IconModel(R.drawable.speed, "Speed"));

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
                if (position == 3) {
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
                if (position == 4) {
                    if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playBackIconsAdapter.notifyDataSetChanged();
                    }else if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playBackIconsAdapter.notifyDataSetChanged();
                    }
                }
                if (position==5){
                    //Volume
                    VolumeDialog volumeDialog=new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconsAdapter.notifyDataSetChanged();

                }
                if (position==6)
                {
                    //brightness
                    BrightnessDialog brightnessDialog=new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconsAdapter.notifyDataSetChanged();
                }
                if(position==7)
                {
                    Intent intent=new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if(intent.resolveActivity(getPackageManager())!=null)
                    {
                        startActivityForResult(intent,123);
                    }else {
                        Toast.makeText(VideoPlayerActivity.this, "No equalizer found", Toast.LENGTH_SHORT).show();
                    }
                    playBackIconsAdapter.notifyDataSetChanged();
                }
                if (position==8)
                {
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback Speed").setPositiveButton("Ok",null);
                    String[] items={"0.5x","1.0x","1.25x","1.5x","2.0x"};
                    int checkedItem =-1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    speed=0.5f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed=1f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);

                                    break;
                                case 2:
                                    speed=1.25f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed=1.5f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4: speed=2f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;

                            }
                        }
                    });
                    AlertDialog alertDialog1=alertDialog.create();
                    alertDialog1.show();
                }
                if(position==2)
                {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    {
                        Rational aspectRatio=new Rational(16,9);
                        pip.setAspectRatio(aspectRatio);
                        enterPictureInPictureMode(pip.build());
                    }else {
                        android.util.Log.wtf("Below Oreo","Yes");
                    }
                }


            }
        });
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        nextButton = findViewById(R.id.exo_next);
        prevButton = findViewById(R.id.exo_prev);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock_unlock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        nightMode=findViewById(R.id.night_mode);
        videoList=findViewById(R.id.video_list);
        recyclerViewicons = findViewById(R.id.recycler_view_icons);
        vol_text=findViewById(R.id.vol_text);
        brt_text=findViewById(R.id.brt_text);
        vol_progress=findViewById(R.id.volume_progress);
        brt_progress=findViewById(R.id.brt_progress);
        vol_progress_container=findViewById(R.id.volume_progress_container);
        brt_progress_container=findViewById(R.id.brt_progress_container);
        vol_text_container=findViewById(R.id.vol_text_container);
        brt_text_container=findViewById(R.id.brt_text_container);
        vol_icon=findViewById(R.id.vol_icon);
        brt_icon=findViewById(R.id.brt_icon);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        vol_num=findViewById(R.id.vol_num);
        bright_num=findViewById(R.id.brightness_num);
        title.setText(videoTitle);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            pip=new PictureInPictureParams.Builder();
        }
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

        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }



    private void screenOrientaion(){
        try {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            Bitmap bitmap;
            String path=mVideoFiles.get(position).getPath();
            Uri uri=Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap=retriever.getFrameAtTime();

            int videoWidth=bitmap.getWidth();
            int videoHeight=bitmap.getHeight();
            if(videoWidth>videoHeight){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch (Exception e)
        {
            Log.e("MediaMetaDataRetriever","ScreenOrientation: ");
        }
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
        if(isInPictureInPictureMode()){
            player.setPlayWhenReady(true);
        }else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }


    }

    @Override
    protected void onStop() {

        super.onStop();
        if(isCrossChecked){
            player.release();
            finish();
        }

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
        if (v.getId()==R.id.video_list)
        {
            PlayListDialog playListDialog=new PlayListDialog(mVideoFiles,videoFilesAdapter);
            playListDialog.show(getSupportFragmentManager(),playListDialog.getTag());
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
                title.setText(mVideoFiles.get(position).getDisplayName());
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
                title.setText(mVideoFiles.get(position).getDisplayName());
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

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isCrossChecked=isInPictureInPictureMode;
        if(isInPictureInPictureMode){
            playerView.hideController();
        }
        else{
            playerView.showController();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==111)
        {
            boolean value;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                value=Settings.System.canWrite(getApplicationContext());
                if(value){
                    success=true;
                }
                else {
                    Toast.makeText(this, "Not Granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}