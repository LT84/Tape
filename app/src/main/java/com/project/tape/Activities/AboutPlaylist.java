package com.project.tape.Activities;

import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Fragments.FragmentGeneral.art;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.FragmentGeneral.mediaPlayer;
import static com.project.tape.Fragments.FragmentGeneral.songsList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.project.tape.Adapters.AboutPlaylistAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.JsonDataMap;
import com.project.tape.SecondaryClasses.JsonDataSongs;
import com.project.tape.SecondaryClasses.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AboutPlaylist extends AppCompatActivity implements AboutPlaylistAdapter.OnPlaylistListener {

    TextView song_title_in_playlist, artist_name_in_playlist, song_title_main, artist_name_main, album_title_playlist;
    ImageButton backBtn, playPauseBtnInPlaylist, addSongsToPlaylist;
    ImageView album_cover_in_playlist;
    Button openFullInfoTab;
    private RecyclerView myRecyclerView;

    public static int positionInPlaylist;

    AboutPlaylistAdapter aboutPlaylistAdapter;

    public static String jsonMap;
    String json;
    public static JsonDataMap jsonDataMap = new JsonDataMap();
    JsonDataSongs jsonDataSongs = new JsonDataSongs();
    Gson gson = new Gson();

    public static ArrayList<Song> currentSongsInPlaylist = new ArrayList<>();
    public static Map<String, String> getSongsInPlaylistMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.about_playlist);
        this.getSupportActionBar().hide();

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

        aboutPlaylistAdapter = new AboutPlaylistAdapter(AboutPlaylist.this, currentSongsInPlaylist, this);
        aboutPlaylistAdapter.updatePlaylistList(currentSongsInPlaylist);
        myRecyclerView = findViewById(R.id.playlist_songs_recyclerView);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(aboutPlaylistAdapter);
    }

    private void getIntentMethod() {
        if (songsList != null) {
            if (mediaPlayer.isPlaying()) {
                playPauseBtnInPlaylist.setImageResource(R.drawable.pause_song);
            } else {
                playPauseBtnInPlaylist.setImageResource(R.drawable.play_song);
            }
        }
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.backBtn_playlist:
                    finish();
                    break;
                case R.id.pause_button_in_playlist:
                    playPauseBtnClicked();
                    break;
                case R.id.open_information_tab_in_playlist:
                    intent = new Intent(AboutPlaylist.this, SongInfoTab.class);
                    intent.putExtra("positionInInfoAboutItem", positionInPlaylist);
                    startActivity(intent);
                    break;
                case R.id.add_songs_to_playlist:
                    intent = new Intent(AboutPlaylist.this, AddSongsActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    //Sets play button image
    public void playPauseBtnClicked() {
        if (isPlaying) {
            // onTrackPause();
        } else {
            audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
            // onTrackPlay();
        }
    }

    //Calls when audio source changed
    BroadcastReceiver audioSourceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                // onTrackPause();
            }
        }
    };

    //To register audioSourceChangedReceiver
    public void trackAudioSource() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(audioSourceChangedReceiver, intentFilter);
    }

    @Override
    public void onPlaylistClick(int position) throws IOException {
    }

    @Override
    protected void onResume() {
        super.onResume();
        aboutPlaylistAdapter.updatePlaylistList(currentSongsInPlaylist);
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


            Log.i("jsonMap", this.getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE)
                    .getString("sharedJsonStringMap", ""));
            Log.i("jsonMap", incomingName);
            Log.i("jsonString", json);
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


}
