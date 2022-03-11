package com.project.tape;

import static com.project.tape.FragmentGeneral.coverLoaded;
import static com.project.tape.FragmentGeneral.position;
import static com.project.tape.FragmentGeneral.songsList;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songSearchWasOpened;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.MainActivity.songsFromSearch;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.albumName;
import static com.project.tape.SongsFragment.art;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.previousAlbumName;
import static com.project.tape.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.SongsFragment.staticPreviousSongsInAlbum;
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
import java.util.ArrayList;
import java.util.Random;


public class AlbumInfo extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener, MediaPlayer.OnCompletionListener {

    TextView song_title_in_album, artist_name_in_album, song_title_main, artist_name_main, album_title_albumInfo;
    ImageView album_cover_in_album;
    ImageButton backBtn, playPauseBtn, playPauseBtnInTab;
    Button openFullInfoTab;
    RecyclerView myRecyclerView;

    public static int positionInOpenedAlbum;


    ArrayList<Song> currentSongsInAlbum = new ArrayList<>();
    ArrayList<Song> previousSongsInAlbum = new ArrayList<>();

    public static boolean fromAlbumInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);
        getSupportActionBar().hide();

        positionInOpenedAlbum = this.getSharedPreferences("positionInOpenedAlbum", Context.MODE_PRIVATE)
                .getInt("positionInOpenedAlbum", positionInOpenedAlbum);

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


        AlbumInfo.this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", false).commit();

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


        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < songsList.size(); i++) {
            if (albumName.equals(songsList.get(i).getAlbum())) {
                currentSongsInAlbum.add(j, songsList.get(i));
                j++;
            }
        }

        staticPreviousSongsInAlbum.clear();
        int a = 0;
        for (int i = 0; i < songsList.size(); i++) {
            if (previousAlbumName.equals(songsList.get(i).getAlbum())) {
                previousSongsInAlbum.add(a, songsList.get(i));
                a++;
            }
        }

        AlbumInfoAdapter albumInfoAdapter = new AlbumInfoAdapter(this, currentSongsInAlbum, this);
        myRecyclerView = findViewById(R.id.albumSongs_recyclerview);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(albumInfoAdapter);
        mediaPlayer.setOnCompletionListener(this);
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
        if (currentSongsInAlbum != null) {
            uri = Uri.parse(currentSongsInAlbum.get(positionInOpenedAlbum).getData());
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
                    intent.putExtra("positionInOpenedAlbum", positionInOpenedAlbum);
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
    protected void onPause() {
        Intent intent = new Intent();
        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        intent.putExtra("previousAlbumName", previousAlbumName);
        staticPreviousSongsInAlbum = previousSongsInAlbum;
        super.onPause();
    }

    @Override
    public void onAlbumClick(int position) throws IOException {
        this.positionInOpenedAlbum = position;

        fromAlbumInfo = true;
        coverLoaded = false;

        staticCurrentSongsInAlbum = currentSongsInAlbum;
        previousAlbumName = albumName;
        staticCurrentSongsInAlbum = currentSongsInAlbum;
        this.getSharedPreferences("previousAlbumName", Context.MODE_PRIVATE).edit()
                .putString("previousAlbumName", previousAlbumName).commit();

        songNameStr = currentSongsInAlbum.get(positionInOpenedAlbum).getTitle();
        artistNameStr = currentSongsInAlbum.get(positionInOpenedAlbum).getArtist();

        playMusic();

        AlbumInfo.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", " ").commit();
        AlbumInfo.this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", true).commit();
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
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                if (fromAlbumInfo) {
                    uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInOpenedAlbum).getData());
                } else if (songSearchWasOpened) {
                    uri = Uri.parse(songsFromSearch.get(position).getData());
                } else {
                    uri = Uri.parse(songsList.get(position).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = positionInOpenedAlbum + 1 == staticPreviousSongsInAlbum.size()
                            ? (0) : (positionInOpenedAlbum + 1);
                } else if (songSearchWasOpened) {
                    position = position + 1 == songsFromSearch.size() ? (0)
                            : (position + 1);
                } else {
                    position = position + 1 == songsList.size() ? (0)
                            : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                if (fromAlbumInfo) {
                    uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInOpenedAlbum).getData());
                } else if (songSearchWasOpened) {
                    uri = Uri.parse(songsFromSearch.get(position).getData());
                } else {
                    uri = Uri.parse(songsList.get(position).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = positionInOpenedAlbum + 1 == staticPreviousSongsInAlbum.size()
                            ? (0) : (positionInOpenedAlbum + 1);
                } else if (songSearchWasOpened) {
                    position = position + 1 == songsFromSearch.size() ? (0)
                            : (position + 1);
                } else {
                    position = position + 1 == songsList.size() ? (0)
                            : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInOpenedAlbum = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }
        }

        //Sets song and artist strings
        if (fromAlbumInfo) {
            //Sets song uri, name and album title if it's from albumInfo
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInOpenedAlbum).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInOpenedAlbum).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInOpenedAlbum).getArtist();
        } else if (songSearchWasOpened) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }

        metaDataInAlbumInfo(uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer = MediaPlayer.create(AlbumInfo.this, uri);

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        AlbumInfo.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("progress", uri.toString()).commit();
        AlbumInfo.this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        AlbumInfo.this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();

        staticPreviousSongsInAlbum = previousSongsInAlbum;

        mediaPlayer.start();
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
        mediaPlayer.setOnCompletionListener(this);
        super.onResume();
    }

    public void metaDataInAlbumInfo(Uri uri) {
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        AlbumInfo.this.getSharedPreferences("preferences_name", Context.MODE_PRIVATE)
                .edit().putInt("progress", positionInOpenedAlbum).commit();
        switchSong();
        mediaPlayer.setOnCompletionListener(this);
    }


}