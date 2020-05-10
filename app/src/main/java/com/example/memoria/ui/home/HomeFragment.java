package com.example.memoria.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.adapter.PostAdapter;
import com.example.memoria.model.Memory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private DatabaseReference mRef;
    private PostAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mRef = FirebaseDatabase.getInstance().getReference("Memories");
        mRef.keepSynced(true);

        RecyclerView memoryRecyclerView = root.findViewById(R.id.memoryRecyclerView);
        LinearLayoutManager mLayout = new LinearLayoutManager(getContext());
        List<Memory> listData = new ArrayList<>();

        memoryRecyclerView.setHasFixedSize(true);
        mLayout.setReverseLayout(true);
        mLayout.setStackFromEnd(true);

        memoryRecyclerView.setLayoutManager(mLayout);

        memoryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean reachedBottom  = !recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    Toast.makeText(getContext(),"You have viewed all the memories.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        String memoryId = data.getKey();
                        Memory memory = data.getValue(Memory.class).withId(memoryId);
                        listData.add(memory);
                        Log.i("Info: ", String.valueOf(listData.size()));
                    }
                    adapter = new PostAdapter(getContext(), listData);
                    memoryRecyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void  onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }
}
