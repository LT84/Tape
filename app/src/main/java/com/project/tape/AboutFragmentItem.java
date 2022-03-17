package com.project.tape;

import static com.project.tape.AlbumsFragment.fromAlbumsFragment;
import static com.project.tape.ArtistsFragment.fromArtistsFragment;
import static com.project.tape.FragmentGeneral.coverLoaded;
import static com.project.tape.FragmentGeneral.position;
import static com.project.tape.FragmentGeneral.songsList;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.MainActivity.songSearchWasOpened;
import static com.project.tape.MainActivity.songsFromSearch;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.SongsFragment.albumName;
import static com.project.tape.SongsFragment.art;
import static com.project.tape.SongsFragment.artistName;
import static com.project.tape.SongsFragment.mediaPlayer;
import static com.project.tape.SongsFragment.previousAlbumName;
import static com.project.tape.SongsFragment.previousArtistName;
import static com.project.tape.SongsFragment.staticArtistSongs;
import static com.project.tape.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.SongsFragment.staticPreviousArtistSongs;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class AboutFragmentItem extends AppCompatActivity implements AlbumInfoAdapter.OnAlbumListener, MediaPlayer.OnCompletionListener {

    TextView song_title_in_album, artist_name_in_album, song_title_main, artist_name_main, album_title_albumInfo;
    ImageView album_cover_in_itemInfo;
    ImageButton backBtn, playPauseBtn, playPauseBtnInTab;
    Button openFullInfoTab;
    RecyclerView myRecyclerView;

    public static int positionInInfoAboutItem;

    ArrayList<Song> currentSongsInAlbum = new ArrayList<>();
    ArrayList<Song> previousSongsInAlbum = new ArrayList<>();
    ArrayList<Song> artistSongs = new ArrayList<>();
    ArrayList<Song> previousArtistSongs = new ArrayList<>();

    public static boolean fromAlbumInfo, fromArtistInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_fragment_item);
        getSupportActionBar().hide();

        backBtn = findViewById(R.id.backBtn_fragmentItemInfo);
        backBtn.setOnClickListener(btnListener);
        openFullInfoTab = findViewById(R.id.open_information_tab_in_itemInfo);
        openFullInfoTab.setOnClickListener(btnListener);
        playPauseBtnInTab = findViewById(R.id.pause_button_in_itemInfo);

        album_title_albumInfo = findViewById(R.id.item_title_fragmentItemInfo);
        song_title_in_album = findViewById(R.id.song_title_in_itemInfo);
        artist_name_in_album = findViewById(R.id.artist_name_in_album);
        album_cover_in_itemInfo = findViewById(R.id.album_cover_in_itemInfo);
        playPauseBtn = findViewById(R.id.pause_button_in_itemInfo);
        playPauseBtn.setOnClickListener(btnListener);

        song_title_main = (TextView) findViewById(R.id.song_title_main);
        artist_name_main = (TextView) findViewById(R.id.artist_name_main);



        if (fromAlbumInfo) {
            AboutFragmentItem.this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                    .putBoolean("fromAlbumInfo", false).commit();
        }

        getIntentMethod();

        //Getting albumName to fill the list
        if (fromAlbumsFragment) {
            album_title_albumInfo.setText(getIntent().getStringExtra("albumName"));
        } else {
            album_title_albumInfo.setText(getIntent().getStringExtra("artistName"));
        }

        //Sets information
        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);
        if (art != null) {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_itemInfo);
        } else {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_itemInfo);
        }
        mediaPlayer.setOnCompletionListener(this);

        //Filling up arrayLists depending where its from
        if (fromAlbumsFragment) {
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
            myRecyclerView = findViewById(R.id.itemSongs_recyclerview);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            myRecyclerView.setAdapter(albumInfoAdapter);
        } else {
            artistName = getIntent().getStringExtra("artistName");
            int j = 0;
            for (int i = 0; i < songsList.size(); i++) {
                if (artistName.equals(songsList.get(i).getArtist())) {
                    artistSongs.add(j, songsList.get(i));
                    j++;
                }
            }

            staticPreviousArtistSongs.clear();
            int a = 0;
            for (int i = 0; i < songsList.size(); i++) {
                if (previousArtistName.equals(songsList.get(i).getArtist())) {
                    previousArtistSongs.add(a, songsList.get(i));
                    a++;
                }
            }

            AlbumInfoAdapter albumInfoAdapter = new AlbumInfoAdapter(this, artistSongs, this);
            myRecyclerView = findViewById(R.id.itemSongs_recyclerview);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            myRecyclerView.setAdapter(albumInfoAdapter);
        }
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
        if (currentSongsInAlbum != null && fromAlbumsFragment) {
            uri = Uri.parse(currentSongsInAlbum.get(positionInInfoAboutItem).getData());
        } else {
            uri = Uri.parse(artistSongs.get(positionInInfoAboutItem).getData());
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(AboutFragmentItem.this, uri);
        mediaPlayer.start();
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn_fragmentItemInfo:
                    finish();
                    break;
                case R.id.pause_button_in_itemInfo:
                    playPauseBtnClicked();
                    break;
                case R.id.open_information_tab_in_itemInfo:
                    Intent intent = new Intent(AboutFragmentItem.this, SongInfoTab.class);
                    intent.putExtra("positionInInfoAboutItem", positionInInfoAboutItem);
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
    protected void onPause() {
        Intent intent = new Intent();
        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        intent.putExtra("previousAlbumName", previousAlbumName);
        intent.putExtra("previousArtistName", previousArtistName);
        if (fromAlbumsFragment) {
            staticPreviousSongsInAlbum = previousSongsInAlbum;
        } else {
            staticPreviousArtistSongs = previousArtistSongs;
        }
        super.onPause();
    }

    @Override
    public void onAlbumClick(int position) throws IOException {
        this.positionInInfoAboutItem = position;

        if (fromAlbumsFragment) {
            fromAlbumInfo = true;
            coverLoaded = false;
        } else if (fromArtistsFragment) {
            fromArtistInfo = true;
            coverLoaded = false;
        }

        if (fromAlbumsFragment) {
            staticCurrentSongsInAlbum = currentSongsInAlbum;
            previousAlbumName = albumName;
            this.getSharedPreferences("previousAlbumName", Context.MODE_PRIVATE).edit()
                    .putString("previousAlbumName", previousAlbumName).commit();
            songNameStr = currentSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = currentSongsInAlbum.get(positionInInfoAboutItem).getArtist();
        } else if (fromArtistsFragment) {
            staticArtistSongs = artistSongs;
            previousArtistName = artistName;
            this.getSharedPreferences("previousArtistName", Context.MODE_PRIVATE).edit()
                    .putString("previousArtistName", previousArtistName).commit();
            Toast.makeText(AboutFragmentItem.this, previousArtistName, Toast.LENGTH_SHORT).show();
            songNameStr = artistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = artistSongs.get(positionInInfoAboutItem).getArtist();
        }

        playMusic();

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", " ").commit();
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", true).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        playPauseBtn.setImageResource(R.drawable.pause_song);

        metaDataInAboutFragmentItem(uri);
    }

    public void switchSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                if (fromAlbumInfo) {
                    uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
                } else if (songSearchWasOpened) {
                    uri = Uri.parse(songsFromSearch.get(position).getData());
                } else {
                    uri = Uri.parse(songsList.get(position).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                            ? (0) : (positionInInfoAboutItem + 1);
                } else if (songSearchWasOpened) {
                    position = position + 1 == songsFromSearch.size() ? (0)
                            : (position + 1);
                } else if (fromArtistInfo) {
                    positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                            ? (0) : (positionInInfoAboutItem + 1);
                } else {
                    position = position + 1 == songsList.size() ? (0)
                            : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBtnClicked && !repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
            } else if (!shuffleBtnClicked && repeatBtnClicked) {
                if (fromAlbumInfo) {
                    uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
                } else if (songSearchWasOpened) {
                    uri = Uri.parse(songsFromSearch.get(position).getData());
                } else {
                    uri = Uri.parse(songsList.get(position).getData());
                }
            } else if (!shuffleBtnClicked && !repeatBtnClicked) {
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                            ? (0) : (positionInInfoAboutItem + 1);
                } else if (songSearchWasOpened) {
                    position = position + 1 == songsFromSearch.size() ? (0)
                            : (position + 1);
                } else if (fromArtistInfo) {
                    positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                            ? (0) : (positionInInfoAboutItem + 1);
                } else {
                    position = position + 1 == songsList.size() ? (0)
                            : (position + 1);
                }
            } else if (shuffleBtnClicked && repeatBtnClicked) {
                position = getRandom(songsList.size() - 1);
                if (fromAlbumInfo) {
                    positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
                }
                repeatBtnClicked = false;
            }
        }

        //Sets song, artist string and uri depending where its from
        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
        } else if (songSearchWasOpened) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
        }

        metaDataInAboutFragmentItem(uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer = MediaPlayer.create(AboutFragmentItem.this, uri);

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        AboutFragmentItem.this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("progress", uri.toString()).commit();
        AboutFragmentItem.this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        AboutFragmentItem.this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        AboutFragmentItem.this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        AboutFragmentItem.this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();

        if (fromAlbumsFragment) {
            staticPreviousSongsInAlbum = previousSongsInAlbum;
        } else {
            staticPreviousArtistSongs = artistSongs;
        }

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
        if (art != null) {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_itemInfo);
        } else {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_itemInfo);
        }
        if (mediaPlayer.isPlaying()) {
            playPauseBtnInTab.setImageResource(R.drawable.pause_song);
        } else  {
            playPauseBtnInTab.setImageResource(R.drawable.play_song);
        }
        mediaPlayer.setOnCompletionListener(this);
        super.onResume();
    }

    public void metaDataInAboutFragmentItem(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_itemInfo);
        } else {
            Glide.with(AboutFragmentItem.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_itemInfo);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switchSong();
        mediaPlayer.setOnCompletionListener(this);
    }


}