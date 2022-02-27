package com.project.tape;

import static com.project.tape.FragmentGeneral.coverLoaded;
import static com.project.tape.FragmentGeneral.songsList;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.art;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.uri;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class AlbumInfo extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener,  Serializable {

    TextView song_title_in_album, artist_name_in_album, song_title_main, artist_name_main, album_title_albumInfo;
    ImageView album_cover_in_album;
    ImageButton backBtn, playPauseBtn, playPauseBtnInTab;
    Button openFullInfoTab;

    RecyclerView myRecyclerView;
    String albumName;

    public static int positionInOpenedAlbum;

    ArrayList<Song> songsInAlbum = new ArrayList<>();
    static ArrayList<Song> copyOfSongsInAlbum = new ArrayList<>();

    ArrayList<Song> copyOfSongsList = songsList;

    public static boolean fromAlbumInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        backBtn = findViewById(R.id.backBtn_albumInfo);
        backBtn.setOnClickListener(btnListener);
        openFullInfoTab = findViewById(R.id.open_information_tab_in_album);
        openFullInfoTab.setOnClickListener(btnListener);
        playPauseBtnInTab = findViewById(R.id.pause_button_in_album);

        album_title_albumInfo = findViewById(R.id.album_title_albumInfo);
        song_title_in_album = findViewById(R.id.song_title_in_album);
        artist_name_in_album = findViewById(R.id.artist_name_in_album);
        album_cover_in_album = findViewById(R.id.album_cover_in_album);
        playPauseBtn = findViewById(R.id.pause_button_in_album);
        playPauseBtn.setOnClickListener(btnListener);

        song_title_main = (TextView) findViewById(R.id.song_title_main);
        artist_name_main = (TextView) findViewById(R.id.artist_name_main);

        fromAlbumInfo = true;

        getIntentMethod();

        album_title_albumInfo.setText(getIntent().getStringExtra("albumName"));
        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);
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

        copyOfSongsInAlbum.clear();
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < copyOfSongsList.size(); i++) {
            if (albumName.equals(copyOfSongsList.get(i).getAlbum())) {
                songsInAlbum.add(j, copyOfSongsList.get(i));
                copyOfSongsInAlbum.add(j, copyOfSongsList.get(i));
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
                playPauseBtn.setImageResource(R.drawable.pause_song);
            } else {
                playPauseBtn.setImageResource(R.drawable.play_song);
            }
        }
    }

    private void playMusic() {
        if (songsInAlbum!= null) {
            uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(AlbumInfo.this, uri);
        mediaPlayer.start();
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn_albumInfo:
                    finish();
                    break;
                case R.id.pause_button_in_album:
                    playPauseBtnClicked();
                    break;
                case R.id.open_information_tab_in_album:
                    Intent intent = new Intent(AlbumInfo.this, SongInfoTab.class);
                    intent.putExtra("positionInAlbum", positionInOpenedAlbum);
                    startActivity(intent);
            }
        }
    };

    //Sets play button image
    public void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.play_song);
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            playPauseBtn.setImageResource(R.drawable.pause_song);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onAlbumClick(int position) throws IOException {
        this.positionInOpenedAlbum = position;

        fromAlbumInfo = true;
        coverLoaded = false;

        songNameStr = songsInAlbum.get(positionInOpenedAlbum).getTitle();
        artistNameStr = songsInAlbum.get(positionInOpenedAlbum).getArtist();

        playMusic();

        AlbumInfo.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", " ").commit();
        AlbumInfo.this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        AlbumInfo.this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        AlbumInfo.this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        AlbumInfo.this.getSharedPreferences("positionInOpenedAlbum", Context.MODE_PRIVATE).edit()
                .putInt("positionInOpenedAlbum", positionInOpenedAlbum).commit();

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        playPauseBtn.setImageResource(R.drawable.pause_song);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        art = retriever.getEmbeddedPicture();

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
    }

    public void switchSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                positionInOpenedAlbum = getRandom(songsInAlbum.size() -1);
            }
            else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            }

            else if (!shuffleBtnClicked && !repeatBtnClicked) {
                positionInOpenedAlbum = (positionInOpenedAlbum + 1 % songsInAlbum.size());
                uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            }
            else if (shuffleBtnClicked && repeatBtnClicked) {
                positionInOpenedAlbum = getRandom(songsInAlbum.size() -1);
                repeatBtnClicked = false;
            }

            uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            mediaPlayer = MediaPlayer.create(AlbumInfo.this, uri);

            songNameStr = songsInAlbum.get(positionInOpenedAlbum).getTitle();
            artistNameStr = songsInAlbum.get(positionInOpenedAlbum).getArtist();
            song_title_in_album.setText(songNameStr);
            artist_name_in_album.setText(artistNameStr);


            AlbumInfo.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                    .putString("progress", uri.toString()).commit();
            AlbumInfo.this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                    .putString("songNameStr", songNameStr).commit();
            AlbumInfo.this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                    .putString("artistNameStr", artistNameStr).commit();


            mediaPlayer.start();

        } else  {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                positionInOpenedAlbum = getRandom(songsInAlbum.size() -1);
            }
            else if (!shuffleBtnClicked && repeatBtnClicked) {
                uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            }

            else if (!shuffleBtnClicked && !repeatBtnClicked) {
                positionInOpenedAlbum = (positionInOpenedAlbum + 1 % songsInAlbum.size());
                uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            }
            else if (shuffleBtnClicked && repeatBtnClicked) {
                positionInOpenedAlbum = getRandom(songsInAlbum.size() -1);
                repeatBtnClicked = false;
            }

            uri = Uri.parse(songsInAlbum.get(positionInOpenedAlbum).getData());
            mediaPlayer = MediaPlayer.create(AlbumInfo.this, uri);

            songNameStr = songsInAlbum.get(positionInOpenedAlbum).getTitle();
            artistNameStr = songsInAlbum.get(positionInOpenedAlbum).getArtist();
            song_title_in_album.setText(songNameStr);
            artist_name_in_album.setText(artistNameStr);

            AlbumInfo.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                    .putString("progress", uri.toString()).commit();
            AlbumInfo.this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                    .putString("songNameStr", songNameStr).commit();
            AlbumInfo.this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                    .putString("artistNameStr", artistNameStr).commit();
        }
    }

    //Gets random number
    public int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }


    @Override
    protected void onResume() {
        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        if (mediaPlayer.isPlaying()) {
            playPauseBtnInTab.setImageResource(R.drawable.pause_song);
        } else  {
            playPauseBtnInTab.setImageResource(R.drawable.play_song);
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switchSong();
                AlbumInfo.this.getSharedPreferences("preferences_name", Context.MODE_PRIVATE).edit().putInt("progress", positionInOpenedAlbum).commit();
                mediaPlayer = MediaPlayer.create(AlbumInfo.this, uri);
                mediaPlayer.start();
            }
        });

        super.onResume();
    }


}