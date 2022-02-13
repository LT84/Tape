package com.project.tape;

import static com.project.tape.SongsFragment.songsList;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumInfo extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener {

    RecyclerView myRecyclerView;
    String albumNameString;
    String albumName;
    ImageButton backBtn;

    ArrayList<Song> songsInAlbum = new ArrayList<>();
    ArrayList<Song> copy = songsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        backBtn = (ImageButton) findViewById(R.id.backBtn_albumInfo);
        backBtn.setOnClickListener(btnListener);

        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
       for (int i = 0; i < copy.size(); i++) {
           if (albumName.equals(copy.get(i).getAlbum())) {
               songsInAlbum.add(j, copy.get(i));
               j++;
           }

        }

        AlbumInfoAdapter albumInfoAdapter = new AlbumInfoAdapter(this, songsInAlbum, this);

        myRecyclerView = (RecyclerView) findViewById(R.id.albumSongs_recyclerview);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(albumInfoAdapter);

    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn_albumInfo:
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onAlbumClick(int position) throws IOException {

    }
}