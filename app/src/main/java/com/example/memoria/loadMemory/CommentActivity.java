package com.example.memoria.loadMemory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.model.Comment;
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
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.memoria.newMemory.NewMemoryActivity.AUDIO_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.IMAGE_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.LOCATION_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.VIDEO_CODE;

public class CommentActivity extends AppCompatActivity {

    @BindView(R.id.commentRecyclerView) RecyclerView commentRecyclerView;
    @BindView(R.id.commentEdit) EditText commentEdit;
    @BindView(R.id.postComment) Button postComment;
    @BindView(R.id.commentImage) ImageView commentImage;
    @BindView(R.id.commentPlayer) SimpleExoPlayerView commentPlayer;
    @BindView(R.id.commentProgress) ProgressBar progressBar;

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private CommentsAdapter adapter;
    int type;

    private TrackSelector trackSelector;
    SimpleExoPlayer exoPlayer;
    private MediaSource mediaSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        Uri uri = Uri.parse(getIntent().getStringExtra("Uri"));
        type = getIntent().getIntExtra("type", 0);
        String memoryId = getIntent().getStringExtra("memoryId");

        if(type == IMAGE_CODE){
            commentImage.setVisibility(View.VISIBLE);
            Glide.with(CommentActivity.this).load(uri).into(commentImage);
        }else if(type == VIDEO_CODE){
            commentPlayer.setVisibility(View.VISIBLE);
            playVideoAudio(uri, CommentActivity.this);
        }else if(type == AUDIO_CODE){
            commentPlayer.setVisibility(View.VISIBLE);
            playVideoAudio(uri, CommentActivity.this);
        }else if(type == LOCATION_CODE){
            Toast.makeText(CommentActivity.this, "Page Under Construction",Toast.LENGTH_LONG).show();
        }

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> commentMap = new HashMap<>();
                commentMap.put("comment", commentEdit.getText().toString());
                commentMap.put("username", mAuth.getCurrentUser().getUid());
                mRef.child("Memories/" + memoryId + "/Comments")
                        .child(String.valueOf(System.currentTimeMillis()))
                        .setValue(commentMap);
                commentEdit.setText("");
            }
        });

        LinearLayoutManager mLayout = new LinearLayoutManager(CommentActivity.this);
        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setLayoutManager(mLayout);

        mRef.child("Memories/" + memoryId + "/Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comment> listData = new ArrayList<>();
                if (dataSnapshot.exists()){
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        String memoryId = data.getKey();
                        Comment comment = data.getValue(Comment.class);
                        listData.add(comment);
                    }
                    adapter = new CommentsAdapter(CommentActivity.this, listData);
                    commentRecyclerView.setAdapter(adapter);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Database Error: ", databaseError.getMessage());
            }
        });
    }

    private void playVideoAudio(Uri uri, Context playContext) {
        trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(playContext, trackSelector, loadControl);
        commentPlayer.setPlayer(exoPlayer);

        String userAgent = Util.getUserAgent(playContext, "MemoryVideo");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(playContext, userAgent);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    public void releasePlayer() {
        if(type == VIDEO_CODE || type == AUDIO_CODE) {
            exoPlayer.release();
            exoPlayer = null;
            mediaSource = null;
            trackSelector = null;
        }
    }
}
