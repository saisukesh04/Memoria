package com.example.memoria.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.loadMemory.PostAdapter;
import com.example.memoria.model.Memory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.memoria.MainActivity.userId;

public class GalleryFragment extends Fragment {

    private DatabaseReference mRef;
    private List<Memory> listData;
    private ProgressBar progressBar;
    private RecyclerView myRecyclerView;
    private PostAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        mRef = FirebaseDatabase.getInstance().getReference("Memories");
        mRef.keepSynced(true);

        Toast.makeText(getActivity(), userId, Toast.LENGTH_LONG).show();
        progressBar = root.findViewById(R.id.progressBar2);
        myRecyclerView = root.findViewById(R.id.myRecyclerView);
        LinearLayoutManager mLayout = new LinearLayoutManager(getContext());

        myRecyclerView.setHasFixedSize(true);
        mLayout.setReverseLayout(true);
        mLayout.setStackFromEnd(true);
        myRecyclerView.setLayoutManager(mLayout);
        loadMemories();

        return root;
    }

    private void loadMemories() {
        listData = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        String memoryId = data.getKey();
                        Memory memory = data.getValue(Memory.class).withId(memoryId);
                        if(userId.equals(memory.getUsername()))
                            listData.add(memory);
                    }
                    adapter = new PostAdapter(getContext(), listData);
                    myRecyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void  onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
