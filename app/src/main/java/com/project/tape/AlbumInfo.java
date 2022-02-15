package com.project.tape;

import static com.project.tape.SongsFragment.art;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.songsList;
import static com.project.tape.SongsFragment.uri;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumInfo extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener {

    TextView song_title_in_album, artist_name_in_album;
    ImageView album_cover_in_album;
    ImageButton backBtn, pause_button_in_album;

    RecyclerView myRecyclerView;
    String albumName;


    private int position;

    ArrayList<Song> songsInAlbum = new ArrayList<>();
    ArrayList<Song> copy = songsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        backBtn = findViewById(R.id.backBtn_albumInfo);
        backBtn.setOnClickListener(btnListener);

        song_title_in_album = findViewById(R.id.song_title_in_album);
        artist_name_in_album = findViewById(R.id.artist_name_in_album);
        album_cover_in_album = findViewById(R.id.album_cover_in_album);
        pause_button_in_album = findViewById(R.id.pause_button_in_album);

        getIntentMethod();

        song_title_in_album.setText(getIntent().getStringExtra("songTitle"));
        artist_name_in_album.setText(getIntent().getStringExtra("artistName"));
        if (art != null) {
            Glide.with(AlbumInfo.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_album);
        } else {
            Glide.with(AlbumInfo.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_album);
        }

        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
       for (int i = 0; i < copy.size(); i++) {
           if (albumName.equals(copy.get(i).getAlbum())) {
               songsInAlbum.add(j, copy.get(i));
               j++;
           }

        }

        AlbumInfoAdapter albumInfoAdapter = new AlbumInfoAdapter(this, songsInAlbum, this);

        myRecyclerView = findViewById(R.id.albumSongs_recyclerview);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(albumInfoAdapter);

    }

    private void getIntentMethod() {
        if (songsList != null) {
            if (mediaPlayer.isPlaying()) {
                pause_button_in_album .setImageResource(R.drawable.pause_song);
            } else {
                pause_button_in_album .setImageResource(R.drawable.play_song);
            }
        }
    }

    private void playMusic() {
        if (songsList != null) {
            uri = Uri.parse(songsInAlbum.get(position).getData());
        } if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
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
        this.position = position;
        playMusic();
    }
}