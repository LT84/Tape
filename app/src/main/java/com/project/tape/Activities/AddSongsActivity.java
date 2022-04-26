package com.project.tape.Activities;

import static com.project.tape.Adapters.AddSongsAdapter.setCheckBox;
import static com.project.tape.Fragments.FragmentGeneral.songsList;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.Adapters.AddSongsAdapter;
import com.project.tape.R;

public class AddSongsActivity extends AppCompatActivity {

    AddSongsAdapter addSongsAdapter;
    RecyclerView addSongsRecyclerView;
    ImageButton backBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_songs);
        setContentView(R.layout.activity_add_songs);
        getSupportActionBar().hide();

        backBtn = findViewById(R.id.backBtn_addSongs);
        backBtn.setOnClickListener(btnL);

        addSongsAdapter = new AddSongsAdapter(AddSongsActivity.this, songsList);
        addSongsAdapter.updateAddSongsListList(songsList);
        addSongsRecyclerView = findViewById(R.id.add_songs_recyclerView);
        addSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addSongsRecyclerView.setAdapter(addSongsAdapter);
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn_addSongs:
                    finish();
                    overridePendingTransition(0, R.anim.hold);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setCheckBox = false;
    }


}

