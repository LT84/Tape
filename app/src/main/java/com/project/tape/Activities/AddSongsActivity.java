package com.project.tape.Activities;

import static com.project.tape.Fragments.FragmentGeneral.songsList;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.Adapters.AddSongsAdapter;
import com.project.tape.R;

import java.io.IOException;

public class AddSongsActivity extends AppCompatActivity implements AddSongsAdapter.OnAddSongListener {

    AddSongsAdapter addSongsAdapter;
    RecyclerView addSongsRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_songs_activity);

        addSongsAdapter = new AddSongsAdapter(AddSongsActivity.this, songsList, this);
        addSongsAdapter.updateAddSongsListList(songsList);
        addSongsRecyclerView = findViewById(R.id.add_songs_recyclerView);
        addSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addSongsRecyclerView.setAdapter(addSongsAdapter);
    }


    @Override
    public void onAddSongClick(int position) throws IOException {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}

