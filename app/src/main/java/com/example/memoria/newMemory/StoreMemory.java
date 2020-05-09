package com.example.memoria.newMemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.memoria.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.security.AccessController.getContext;

public class StoreMemory extends AppCompatActivity {

    @BindView(R.id.exoPlayerView) PlayerView exoPlayerView;
    Uri uriFinal;
    SimpleExoPlayer exoPlayer;
    MediaSource mediaSource;
    TrackSelector trackSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_memory);

        ButterKnife.bind(this);

        Intent testIntent = getIntent();
        String uriStr = testIntent.getStringExtra("URI-vid");
        uriFinal = Uri.parse(uriStr);

        float size = Float.parseFloat(getSizeFromUri(getApplicationContext(), uriFinal))/(1024*1024);

        if(size > 10) {
            Toast.makeText(this, String.valueOf(size), Toast.LENGTH_LONG).show();
            finish();
        }else{
            trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            exoPlayer = ExoPlayerFactory.newSimpleInstance(StoreMemory.this, trackSelector, loadControl);
            exoPlayerView.setPlayer(exoPlayer);

            String userAgent = Util.getUserAgent(StoreMemory.this, "RecipeVideo");
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(StoreMemory.this, userAgent);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            mediaSource = new ExtractorMediaSource(uriFinal, dataSourceFactory, extractorsFactory, null, null);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
        }
    }

    private String getSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Audio.Media.SIZE };
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
//        if(!videoURL.isEmpty()) {
            exoPlayer.release();
            exoPlayer = null;
            mediaSource = null;
            trackSelector = null;
//        }
    }
}
