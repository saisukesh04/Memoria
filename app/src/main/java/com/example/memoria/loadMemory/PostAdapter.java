package com.example.memoria.loadMemory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.model.Memory;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.memoria.newMemory.NewMemoryActivity.AUDIO_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.IMAGE_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.LOCATION_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.VIDEO_CODE;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private List<Memory> listData;
    private Context context;

    private TrackSelector trackSelector;
    SimpleExoPlayer exoPlayer;
    private MediaSource mediaSource;

    public PostAdapter(Context context, List<Memory> listData) {
        this.context = context;
        this.listData = listData;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        Memory memory = listData.get(position);
        int type = Integer.parseInt(memory.getType());
        String currentUserId = mAuth.getCurrentUser().getUid();
        String memoryId = memory.MemoryId;
        String userName = memory.getUsername();
        Uri uri = Uri.parse(memory.getLink());

        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> retrieveMap = (Map<String, String>) dataSnapshot.child(userName).getValue();
                holder.username.setText(String.valueOf(retrieveMap.get("Name")));
                Glide.with(context).load(Uri.parse(retrieveMap.get("Image"))).into(holder.userProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.uploadDate.setText(memory.getTimeStamp());
        holder.memoryDescription.setText(memory.getDescription());

        if(type == IMAGE_CODE){
            holder.memoryImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(uri).into(holder.memoryImage);
        }else if(type == VIDEO_CODE){
            holder.memoryVideo.setVisibility(View.VISIBLE);
            playVideoAudio(uri, holder.memoryVideo, context);
        }else if(type == AUDIO_CODE){
            holder.memoryVideo.setVisibility(View.VISIBLE);
            playVideoAudio(uri, holder.memoryVideo, context);
        }else if(type == LOCATION_CODE){
            Toast.makeText(context, "Page Under Construction",Toast.LENGTH_LONG).show();
        }

        mRef.child("Memories/" + memoryId + "/Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    holder.likeCount.setText(dataSnapshot.getChildrenCount() + " Likes");
                else
                    holder.likeCount.setText("0 Likes");

                if(dataSnapshot.hasChild(currentUserId)){
                    holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_red));
                }else{
                    holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_grey));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Database Error: ", databaseError.getMessage());
            }
        });

        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("Memories/" + memoryId + "/Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(currentUserId)){
                            mRef.child("Memories/" + memoryId + "/Likes").child(currentUserId).removeValue();
                            holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_grey));
                        }else{
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp",System.currentTimeMillis());
                            mRef.child("Memories/" + memoryId + "/Likes").child(currentUserId).setValue(likesMap);
                            holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_red));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Database Error: ", databaseError.getMessage());
                    }
                });
            }
        });

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("Uri", uri.toString());
                commentIntent.putExtra("type", type);
                commentIntent.putExtra("memoryId", memoryId);
                releasePlayer(type);
                context.startActivity(commentIntent);
            }
        });

        mRef.child("Memories/" + memoryId + "/Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    holder.commentsCount.setText(dataSnapshot.getChildrenCount() + " Comments");
                else
                    holder.commentsCount.setText("0 Comments");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Database Error: ", databaseError.getMessage());
            }
        });
    }

    private void playVideoAudio(Uri uri, SimpleExoPlayerView memoryVideo, Context playContext) {
        trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(playContext, trackSelector, loadControl);
        memoryVideo.setPlayer(exoPlayer);

        String userAgent = Util.getUserAgent(playContext, "MemoryVideo");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(playContext, userAgent);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(false);
    }

    private void releasePlayer(int type){
        if(type == VIDEO_CODE || type == AUDIO_CODE) {
            exoPlayer.release();
            exoPlayer = null;
            mediaSource = null;
            trackSelector = null;
        }
    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() :0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userProfileImage;
        TextView username, uploadDate, memoryDescription, likeCount, commentsCount;
        ImageView memoryImage ,likeIcon;
        SimpleExoPlayerView memoryVideo;
        LinearLayout commentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            uploadDate = itemView.findViewById(R.id.uploadDate);
            memoryDescription = itemView.findViewById(R.id.memoryDescription);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            memoryImage = itemView.findViewById(R.id.memoryImage);
            memoryVideo = itemView.findViewById(R.id.memoryVideo);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            commentsCount = itemView.findViewById(R.id.commentsCount);

        }
    }
}
