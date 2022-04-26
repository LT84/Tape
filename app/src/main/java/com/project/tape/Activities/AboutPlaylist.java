package com.project.tape.Activities;

import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemAdapter;
import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemOpened;
import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.fromSearch;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.MainActivity.songsFromSearch;
import static com.project.tape.Activities.SongInfoTab.repeatBtnClicked;
import static com.project.tape.Activities.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.Activities.SongInfoTab.songInfoTabOpened;
import static com.project.tape.Fragments.AlbumsFragment.albumsFragmentOpened;
import static com.project.tape.Fragments.ArtistsFragment.artistsFragmentOpened;
import static com.project.tape.Fragments.FragmentGeneral.art;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.coverLoaded;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.FragmentGeneral.mediaPlayer;
import static com.project.tape.Fragments.FragmentGeneral.position;
import static com.project.tape.Fragments.FragmentGeneral.songsList;
import static com.project.tape.Fragments.FragmentGeneral.uri;
import static com.project.tape.Fragments.PlaylistsFragment.playlistsFragmentOpened;
import static com.project.tape.Fragments.SongsFragment.songsAdapter;
import static com.project.tape.Fragments.SongsFragment.songsFragmentOpened;
import static com.project.tape.Fragments.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.project.tape.Adapters.AboutPlaylistAdapter;
import com.project.tape.Interfaces.Playable;
import com.project.tape.ItemClasses.Song;
import com.project.tape.JsonFilesClasses.JsonDataMap;
import com.project.tape.JsonFilesClasses.JsonDataSongs;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.SecondaryClasses.RecyclerItemClickListener;
import com.project.tape.Services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class AboutPlaylist extends AppCompatActivity implements Playable, MediaPlayer.OnCompletionListener,
        HeadsetActionButtonReceiver.Delegate {

    TextView song_title_in_playlist, artist_name_in_playlist, song_title_main, artist_name_main, album_title_playlist;
    ImageButton backBtn, playPauseBtnInPlaylist, addSongsToPlaylist;
    ImageView album_cover_in_playlist;
    Button openFullInfoTab;
    private RecyclerView myRecyclerView;

    Button closeAlertPopupBtn, deletePlaylistBtn;

    private int deletePosition;

    public static int positionInAboutPlaylist;

    public static AboutPlaylistAdapter aboutPlaylistAdapter;

    public static String jsonMap;
    String json;
    public static JsonDataMap jsonDataMap = new JsonDataMap();
    JsonDataSongs jsonDataSongs = new JsonDataSongs();
    Gson gson = new Gson();

    public static ArrayList<Song> currentSongsInPlaylist = new ArrayList<>();
    public static ArrayList<Song> previousSongsInPlaylist = new ArrayList<>();

    public static Map<String, String> getSongsInPlaylistMap = new HashMap<>();

    private boolean fromBackground = false;
    public static boolean fromPlaylist, aboutPlaylistOpened;

    NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_about_playlist);
        this.getSupportActionBar().hide();
        //Booleans
        songsFragmentOpened = false;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        songInfoTabOpened = false;
        aboutFragmentItemOpened = false;
        playlistsFragmentOpened = false;
        aboutPlaylistOpened = true;

        if (mediaPlayer.isPlaying()) {
            isPlaying = true;
        } else {
            isPlaying = false;
        }

        //Init views
        backBtn = findViewById(R.id.backBtn_playlist);
        backBtn.setOnClickListener(btnL);
        openFullInfoTab = findViewById(R.id.open_information_tab_in_playlist);
        openFullInfoTab.setOnClickListener(btnL);

        addSongsToPlaylist = findViewById(R.id.add_songs_to_playlist);
        addSongsToPlaylist.setOnClickListener(btnL);
        album_title_playlist = findViewById(R.id.item_title_playlist);
        song_title_in_playlist = findViewById(R.id.song_title_in_playlist);
        artist_name_in_playlist = findViewById(R.id.artist_name_in_playlist);
        album_cover_in_playlist = findViewById(R.id.album_cover_in_playlist);
        playPauseBtnInPlaylist = findViewById(R.id.pause_button_in_playlist);
        playPauseBtnInPlaylist.setOnClickListener(btnL);

        song_title_main = (TextView) findViewById(R.id.song_title_main);
        artist_name_main = (TextView) findViewById(R.id.artist_name_main);

        song_title_main = (TextView) findViewById(R.id.song_title_main);
        artist_name_main = (TextView) findViewById(R.id.artist_name_main);

        getIntentMethod();

        album_title_playlist.setText(this.getIntent().getStringExtra("playlistName"));

        //Sets information
        song_title_in_playlist.setText(songNameStr);
        artist_name_in_playlist.setText(artistNameStr);
        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_playlist);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_playlist);
        }

        getSongsFromJson();

        //Sets information
        song_title_in_playlist.setText(songNameStr);
        artist_name_in_playlist.setText(artistNameStr);
        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_playlist);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_playlist);
        }

        aboutPlaylistAdapter = new AboutPlaylistAdapter(AboutPlaylist.this, currentSongsInPlaylist);
        aboutPlaylistAdapter.updatePlaylistList(currentSongsInPlaylist);
        myRecyclerView = findViewById(R.id.playlist_songs_recyclerView);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(aboutPlaylistAdapter);

        //RecyclerView listener
        myRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, myRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        positionInAboutPlaylist = position;
                        aboutPlaylistAdapter.updateColorAfterSongSwitch(positionInAboutPlaylist);
                        fromSearch = false;
                        fromAlbumInfo = false;
                        fromArtistInfo = false;
                        fromPlaylist = true;

                        getSharedPreferences("fromPlaylist", Context.MODE_PRIVATE).edit()
                                .putBoolean("fromPlaylist", fromPlaylist).commit();
                        previousSongsInPlaylist.clear();
                        previousSongsInPlaylist.addAll(currentSongsInPlaylist);

                        songNameStr = currentSongsInPlaylist.get(position).getTitle();
                        artistNameStr = currentSongsInPlaylist.get(position).getArtist();

                        coverLoaded = false;

                        uri = Uri.parse(currentSongsInPlaylist.get(positionInAboutPlaylist).getData());

                        mediaPlayer.release();
                        mediaPlayer = MediaPlayer.create(AboutPlaylist.this, uri);
                        onTrackPlay();

                        metaDataInAboutPlaylist(uri);


                        CreateNotification.createNotification(AboutPlaylist.this, currentSongsInPlaylist.get(position),
                                R.drawable.ic_pause_song, position, currentSongsInPlaylist.size() - 1);


                        song_title_in_playlist.setText(songNameStr);
                        artist_name_in_playlist.setText(artistNameStr);

                        playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        deletePosition = position;
                        onButtonShowPopupWindowClick(view);
                    }
                })
        );
    }

    public void onButtonShowPopupWindowClick(View view) {
        //Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;
        //Check which popup needed

        popupView = inflater.inflate(R.layout.popup_delete_permission, null);

        //Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Show the popup window
        //Which view you pass in doesn't matter, it is only used for the window token
        popupWindow.setAnimationStyle(R.style.popupWindowAnimation);
        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, -230);

        closeAlertPopupBtn = popupView.findViewById(R.id.close_popup_alert_btn);
        deletePlaylistBtn = popupView.findViewById(R.id.popup_delete_btn);

        //CloseAlertPopupBtn listener
        closeAlertPopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        //DeletePlaylistBtn listener
        deletePlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSongsInPlaylist.remove(deletePosition);

                aboutPlaylistAdapter.updatePlaylistList(currentSongsInPlaylist);
                popupWindow.dismiss();
            }
        });
    }

    private void getIntentMethod() {
        if (songsList != null) {
            if (mediaPlayer.isPlaying()) {
                playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
            } else {
                playPauseBtnInPlaylist.setImageResource(R.drawable.ic_play_song);
            }
        }
    }

    public void metaDataInAboutPlaylist(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(AboutPlaylist.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_playlist);
        } else {
            Glide.with(AboutPlaylist.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_playlist);
        }
        coverLoaded = false;
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.backBtn_playlist:
                    finish();
                    overridePendingTransition(0, R.anim.hold);
                    break;
                case R.id.pause_button_in_playlist:
                    playPauseBtnClicked();
                    break;
                case R.id.open_information_tab_in_playlist:
                    intent = new Intent(AboutPlaylist.this, SongInfoTab.class);
                    startActivity(intent);
                    break;
                case R.id.add_songs_to_playlist:
                    intent = new Intent(AboutPlaylist.this, AddSongsActivity.class);
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(AboutPlaylist.this).toBundle();
                    startActivity(intent, bundle);
                    break;
            }
        }
    };

    //Sets play button image
    public void playPauseBtnClicked() {
        if (isPlaying) {
            onTrackPause();
        } else {
            audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
            onTrackPlay();
        }
    }

    public void switchToNextSong() {
        mediaPlayer.stop();
        mediaPlayer.release();

        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            } else if (fromSearch) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = getRandom(previousSongsInPlaylist.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                        ? (0) : (positionInInfoAboutItem + 1);
            } else if (fromSearch) {
                position = position + 1 == songsFromSearch.size() ? (0)
                        : (position + 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                        ? (0) : (positionInInfoAboutItem + 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = positionInAboutPlaylist + 1 == previousSongsInPlaylist.size()
                        ? (0) : (positionInAboutPlaylist + 1);
            } else {
                position = position + 1 == songsList.size() ? (0)
                        : (position + 1);
            }
        } else if (shuffleBtnClicked && repeatBtnClicked) {
            position = getRandom(songsList.size() - 1);
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticCurrentSongsInAlbum.size() - 1);
            }
            repeatBtnClicked = false;
        }

        coverLoaded = true;

        //Sets song and artist strings
        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
            if (aboutPlaylistAdapter != null) {
                aboutPlaylistAdapter.updateColorAfterSongSwitch(positionInAboutPlaylist);
            }
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
            if (songsAdapter != null) {
                songsAdapter.updateColorAfterSongSwitch(position);
            }
        }

        mediaPlayer = MediaPlayer.create(AboutPlaylist.this, uri);
        metaDataInAboutPlaylist(uri);

        song_title_in_playlist.setText(songNameStr);
        artist_name_in_playlist.setText(artistNameStr);
        mediaPlayer.start();

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    public void switchToPreviousSong() {
        mediaPlayer.stop();
        mediaPlayer.release();

        //Checking is shuffle or repeat button clicked
        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            } else if (fromSearch) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && repeatBtnClicked) {
            if (fromAlbumInfo) {
                uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            } else if (fromSearch) {
                uri = Uri.parse(songsFromSearch.get(position).getData());
            } else if (fromPlaylist) {
                uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            } else {
                uri = Uri.parse(songsList.get(position).getData());
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousSongsInAlbum.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (fromSearch) {
                position = position - 1 < 0 ? (songsFromSearch.size() - 1)
                        : (position - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousArtistSongs.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (fromPlaylist) {
                positionInAboutPlaylist = positionInAboutPlaylist - 1 < 0 ? (previousSongsInPlaylist.size() - 1)
                        : (positionInAboutPlaylist - 1);
            } else {
                position = position - 1 < 0 ? (songsList.size() - 1)
                        : (position - 1);
            }
        } else if (shuffleBtnClicked && repeatBtnClicked) {
            position = getRandom(songsList.size() - 1);
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousSongsInAlbum.size() - 1);
            }
            repeatBtnClicked = false;
        }

        coverLoaded = true;

        //Sets song and artist strings
        if (fromAlbumInfo) {
            uri = Uri.parse(staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousSongsInAlbum.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromSearch) {
            uri = Uri.parse(songsFromSearch.get(position).getData());
            songNameStr = songsFromSearch.get(position).getTitle();
            artistNameStr = songsFromSearch.get(position).getArtist();
        } else if (fromArtistInfo) {
            uri = Uri.parse(staticPreviousArtistSongs.get(positionInInfoAboutItem).getData());
            songNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getTitle();
            artistNameStr = staticPreviousArtistSongs.get(positionInInfoAboutItem).getArtist();
            if (aboutFragmentItemAdapter != null) {
                aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
            }
        } else if (fromPlaylist) {
            uri = Uri.parse(previousSongsInPlaylist.get(positionInAboutPlaylist).getData());
            songNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getTitle();
            artistNameStr = previousSongsInPlaylist.get(positionInAboutPlaylist).getArtist();
            if (aboutPlaylistAdapter != null) {
                aboutPlaylistAdapter.updateColorAfterSongSwitch(positionInAboutPlaylist);
            }
        } else {
            uri = Uri.parse(songsList.get(position).getData());
            songNameStr = songsList.get(position).getTitle();
            artistNameStr = songsList.get(position).getArtist();
            if (songsAdapter != null) {
                songsAdapter.updateColorAfterSongSwitch(position);
            }
        }


        mediaPlayer = MediaPlayer.create(AboutPlaylist.this, uri);
        metaDataInAboutPlaylist(uri);

        song_title_in_playlist.setText(songNameStr);
        artist_name_in_playlist.setText(artistNameStr);
        mediaPlayer.start();

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
    }

    //Gets random number
    public int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.putExtra("songsAmount", Integer.toString(currentSongsInPlaylist.size()));

        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            this.unregisterReceiver(broadcastReceiverAboutFragmentInfo);
        }

        this.getSharedPreferences("fromPlaylist", Context.MODE_PRIVATE).edit()
                .putBoolean("fromPlaylist", fromPlaylist).commit();
        this.getSharedPreferences("positionInAboutPlaylist", Context.MODE_PRIVATE).edit()
                .putInt("positionInAboutPlaylist", positionInAboutPlaylist).commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        song_title_in_playlist.setText(songNameStr);
        artist_name_in_playlist.setText(artistNameStr);

        if (fromBackground) {
            this.unregisterReceiver(broadcastReceiverAboutFragmentInfo);
            fromBackground = false;
        }

        createChannel();
        trackAudioSourceInPlaylist();

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(this);

        if (art != null) {
            Glide.with(AboutPlaylist.this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_playlist);
        } else {
            Glide.with(AboutPlaylist.this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_playlist);
        }
        if (mediaPlayer.isPlaying()) {
            playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
        } else {
            playPauseBtnInPlaylist.setImageResource(R.drawable.ic_play_song);
        }
        mediaPlayer.setOnCompletionListener(this);
        aboutPlaylistAdapter.updatePlaylistList(currentSongsInPlaylist);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (aboutPlaylistOpened) {
            createChannel();
            fromBackground = true;
        }
    }


    //Get json
    public void getSongsFromJson() {
        String incomingName = getIntent().getStringExtra("playlistName");

        jsonMap = this.getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE)
                .getString("sharedJsonStringMap", "");

        if (!jsonMap.equals("")) {
            jsonDataMap = gson.fromJson(jsonMap, JsonDataMap.class);
            getSongsInPlaylistMap.putAll(jsonDataMap.getMap());
        }

        if (getSongsInPlaylistMap.containsKey(incomingName)) {

            json = getSongsInPlaylistMap.get(incomingName);

            jsonDataSongs = gson.fromJson(json, JsonDataSongs.class);
            currentSongsInPlaylist.addAll(jsonDataSongs.getArray());
        }
    }

    public void writeNewPlaylistSongsToJson() {
        //Save json
        jsonDataSongs = new JsonDataSongs(currentSongsInPlaylist);

        json = gson.toJson(jsonDataSongs);
        getSongsInPlaylistMap.put(getIntent().getStringExtra("playlistName"), json);

        jsonDataMap = new JsonDataMap(getSongsInPlaylistMap);
        jsonMap = gson.toJson(jsonDataMap);

        this.getSharedPreferences("sharedPrefPlaylistName", Context.MODE_PRIVATE).edit()
                .putString("sharedPrefPlaylistName", getIntent().getStringExtra("playlistName")).commit();
        this.getSharedPreferences("sharedJsonString", Context.MODE_PRIVATE).edit()
                .putString("sharedJsonString", json).commit();
        this.getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE).edit()
                .putString("sharedJsonStringMap", jsonMap).commit();

        currentSongsInPlaylist.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeNewPlaylistSongsToJson();
    }

    //Calls when audio source changed
    BroadcastReceiver audioSourceChangedReceiverInPlaylist = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                onTrackPause();
            }
        }
    };

    //To register audioSourceChangedReceiver
    public void trackAudioSourceInPlaylist() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(audioSourceChangedReceiverInPlaylist, intentFilter);
    }

    //Notification
    BroadcastReceiver broadcastReceiverAboutFragmentInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Tape", NotificationManager.IMPORTANCE_HIGH);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            this.registerReceiver(broadcastReceiverAboutFragmentInfo, new IntentFilter("SONGS_SONGS"));
            this.startService(new Intent(this, OnClearFromRecentService.class));
        }
    }


    @Override
    public void onTrackPrevious() {
        isPlaying = true;
        switchToPreviousSong();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackNext() {
        isPlaying = true;
        switchToNextSong();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPlay() {
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        isPlaying = true;
        mediaPlayer.start();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_pause_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        playPauseBtnInPlaylist.setImageResource(R.drawable.ic_pause_song);
    }

    @Override
    public void onTrackPause() {
        isPlaying = false;
        mediaPlayer.pause();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_play_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_play_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else if (fromPlaylist) {
            CreateNotification.createNotification(this, previousSongsInPlaylist.get(positionInAboutPlaylist),
                    R.drawable.ic_play_song, positionInAboutPlaylist, previousSongsInPlaylist.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_play_song, position, songsList.size() - 1);
        }
        playPauseBtnInPlaylist.setImageResource(R.drawable.ic_play_song);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        onTrackNext();
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onMediaButtonSingleClick() {
        if (isPlaying) {
            onTrackPause();
        } else {
            onTrackPlay();
        }
    }

    @Override
    public void onMediaButtonDoubleClick() {

    }
}
