package com.project.tape;

import static com.project.tape.SongsFragment.songsList;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumInfo extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener {

    RecyclerView myRecyclerView;
    String albumNameString;
    String albumName;

    ArrayList<Song> songsInAlbum = new ArrayList<>();
    ArrayList<Song> copy = songsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);
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

    @Override
    public void onAlbumClick(int position) throws IOException {

    }
}