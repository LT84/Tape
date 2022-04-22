package com.project.tape.Activities;

import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.SongInfoTab.songInfoTabOpened;
import static com.project.tape.Fragments.AlbumsFragment.albumsFragmentOpened;
import static com.project.tape.Fragments.ArtistsFragment.artistsFragmentOpened;
import static com.project.tape.Fragments.FragmentGeneral.art;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.FragmentGeneral.mediaPlayer;
import static com.project.tape.Fragments.FragmentGeneral.songsList;
import static com.project.tape.Fragments.SongsFragment.songsFragmentOpened;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
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
import com.project.tape.Adapters.AboutPlaylistAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.Song;

import java.io.IOException;
import java.util.ArrayList;


public class AboutPlaylist extends AppCompatActivity implements AboutPlaylistAdapter.OnPlaylistListener {

    TextView song_title_in_playlist, artist_name_in_playlist, song_title_main, artist_name_main, album_title_playlist;
    ImageView album_cover_in_playlist;
    ImageButton backBtn, playPauseBtnInPlaylist, addSongsToPlaylist;
    Button openFullInfoTab;
    private RecyclerView myRecyclerView;

    public static ArrayList<Song> currentSongsInPlaylist = new ArrayList<>();

    NotificationManager notificationManager;

    public static boolean fromAlbumInfo, fromArtistInfo, aboutFragmentItemOpened;

    public static int positionInPlaylist;

    private boolean fromBackground = false;

    public static com.project.tape.Adapters.AboutPlaylistAdapter aboutPlaylistAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.about_playlist);
        this.getSupportActionBar().hide();
        //Booleans
        songsFragmentOpened = false;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        songInfoTabOpened = false;
        aboutFragmentItemOpened = true;

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

        // Why null
        aboutPlaylistAdapter = new AboutPlaylistAdapter(AboutPlaylist.this, currentSongsInPlaylist, this);
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

//    @Override
//    public void onCompletion(MediaPlayer mediaPlayer) {
//        onTrackNext();
//        mediaPlayer.setOnCompletionListener(this);
//    }


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
        aboutPlaylistAdapter.notifyDataSetChanged();
    }


//    //Notification
//    BroadcastReceiver broadcastReceiverAboutFragmentInfo = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getExtras().getString("actionName");
//            switch (action) {
//                case CreateNotification.ACTION_PREVIOUS:
//                    onTrackPrevious();
//                    break;
//                case CreateNotification.ACTION_PLAY:
//                    if (isPlaying) {
//                        onTrackPause();
//                    } else {
//                        onTrackPlay();
//                    }
//                    break;
//                case CreateNotification.ACTION_NEXT:
//                    onTrackNext();
//                    break;
//            }
//        }
//    };

//    private void createChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
//                    "Tape", NotificationManager.IMPORTANCE_HIGH);
//
//            notificationManager = getSystemService(NotificationManager.class);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(channel);
//            }
//            this.registerReceiver(broadcastReceiverAboutFragmentInfo, new IntentFilter("SONGS_SONGS"));
//            this.startService(new Intent(this, OnClearFromRecentService.class));
//        }
//    }


//    @Override
//    public void onTrackPrevious() {
//        isPlaying = true;
//        switchToPreviousSong();
//        if (fromSearch) {
//            CreateNotification.createNotification(this, songsFromSearch.get(position),
//                    R.drawable.pause_song, position, songsFromSearch.size() - 1);
//        } else if (fromAlbumInfo) {
//            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
//                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
//            aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
//        } else if (fromArtistInfo) {
//            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
//                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
//            aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
//        } else {
//            CreateNotification.createNotification(this, songsList.get(position),
//                    R.drawable.pause_song, position, songsList.size() - 1);
//        }
//        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
//    }

//    @Override
//    public void onTrackNext() {
//        isPlaying = true;
//        switchToNextSong();
//        if (fromSearch) {
//            CreateNotification.createNotification(this, songsFromSearch.get(position),
//                    R.drawable.pause_song, position, songsFromSearch.size() - 1);
//        } else if (fromAlbumInfo) {
//            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
//                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
//            aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
//        } else if (fromArtistInfo) {
//            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
//                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
//           aboutFragmentItemAdapter.updateColorAfterSongSwitch(positionInInfoAboutItem);
//        } else {
//            CreateNotification.createNotification(this, songsList.get(position),
//                    R.drawable.pause_song, position, songsList.size() - 1);
//        }
//        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
//    }

//    @Override
//    public void onTrackPlay() {
//        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
//        isPlaying = true;
//        mediaPlayer.start();
//        if (fromSearch) {
//            CreateNotification.createNotification(this, songsFromSearch.get(position),
//                    R.drawable.pause_song, position, songsFromSearch.size() - 1);
//        } else if (fromAlbumInfo) {
//            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInPlaylist),
//                    R.drawable.pause_song, positionInPlaylist, staticPreviousSongsInAlbum.size() - 1);
//        } else if (fromArtistInfo) {
//            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInPlaylist),
//                    R.drawable.pause_song, positionInPlaylist, staticPreviousArtistSongs.size() - 1);
//        } else {
//            CreateNotification.createNotification(this, songsList.get(position),
//                    R.drawable.pause_song, position, songsList.size() - 1);
//        }
//        playPauseBtnInPlaylist.setImageResource(R.drawable.pause_song);
//    }
//
//    @Override
//    public void onTrackPause() {
//        isPlaying = false;
//        mediaPlayer.pause();
//        if (fromSearch) {
//            CreateNotification.createNotification(this, songsFromSearch.get(position),
//                    R.drawable.play_song, position, songsFromSearch.size() - 1);
//        } else if (fromAlbumInfo) {
//            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInPlaylist),
//                    R.drawable.play_song, positionInPlaylist, staticPreviousSongsInAlbum.size() - 1);
//        } else if (fromArtistInfo) {
//            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInPlaylist),
//                    R.drawable.play_song, positionInPlaylist, staticPreviousArtistSongs.size() - 1);
//        } else {
//            CreateNotification.createNotification(this, songsList.get(position),
//                    R.drawable.play_song, position, songsList.size() - 1);
//        }
//        playPauseBtnInPlaylist.setImageResource(R.drawable.play_song);
//    }



}
