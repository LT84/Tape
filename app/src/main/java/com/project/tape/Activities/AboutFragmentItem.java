package com.project.tape.Activities;

import static com.project.tape.Fragments.AlbumsFragment.fromAlbumsFragment;
import static com.project.tape.Fragments.ArtistsFragment.fromArtistsFragment;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.coverLoaded;
import static com.project.tape.Fragments.FragmentGeneral.position;
import static com.project.tape.Fragments.FragmentGeneral.songsList;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.MainActivity.songSearchWasOpened;
import static com.project.tape.Activities.MainActivity.songsFromSearch;
import static com.project.tape.Activities.SongInfoTab.repeatBtnClicked;
import static com.project.tape.Activities.SongInfoTab.shuffleBtnClicked;
import static com.project.tape.Fragments.SongsFragment.albumName;
import static com.project.tape.Fragments.SongsFragment.art;
import static com.project.tape.Fragments.SongsFragment.artistName;
import static com.project.tape.Fragments.SongsFragment.mediaPlayer;
import static com.project.tape.Fragments.SongsFragment.previousAlbumName;
import static com.project.tape.Fragments.SongsFragment.previousArtistName;
import static com.project.tape.Fragments.SongsFragment.staticCurrentArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.uri;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.tape.Adapters.AboutFragmentItemAdapter;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.Interfaces.Playable;
import com.project.tape.R;
import com.project.tape.Services.OnClearFromRecentService;
import com.project.tape.SecondaryClasses.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class AboutFragmentItem extends AppCompatActivity implements AboutFragmentItemAdapter.OnItemListener
        , MediaPlayer.OnCompletionListener, Playable {

    TextView song_title_in_album, artist_name_in_album, song_title_main, artist_name_main, album_title_albumInfo;
    ImageView album_cover_in_itemInfo;
    ImageButton backBtn, playPauseBtn, playPauseBtnInTab;
    Button openFullInfoTab;
    RecyclerView myRecyclerView;

    ArrayList<Song> currentSongsInAlbum = new ArrayList<>();
    ArrayList<Song> currentArtistSongs = new ArrayList<>();

    NotificationManager notificationManager;

    boolean isPlaying = false;
    public static boolean fromAlbumInfo, fromArtistInfo, aboutFragmentItemOpened;

    public static int positionInInfoAboutItem;


    BroadcastReceiver audioSourceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                onTrackPause();
            }
        }
    };
    public void trackAudioSource() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(audioSourceChangedReceiver, intentFilter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.about_fragment_item);
        this.getSupportActionBar().hide();
        //Booleans
        aboutFragmentItemOpened = true;

        if (mediaPlayer.isPlaying()) {
            isPlaying = true;
        } else {
            isPlaying = false;
        }

        //Init views
        backBtn = findViewById(R.id.backBtn_fragmentItemInfo);
        backBtn.setOnClickListener(btnListener);
        openFullInfoTab = findViewById(R.id.open_information_tab_in_itemInfo);
        openFullInfoTab.setOnClickListener(btnListener);

        album_title_albumInfo = findViewById(R.id.item_title_fragmentItemInfo);
        song_title_in_album = findViewById(R.id.song_title_in_itemInfo);
        artist_name_in_album = findViewById(R.id.artist_name_in_album);
        album_cover_in_itemInfo = findViewById(R.id.album_cover_in_itemInfo);
        playPauseBtnInTab = findViewById(R.id.pause_button_in_itemInfo);
        playPauseBtnInTab.setOnClickListener(btnListener);

        song_title_main = (TextView) findViewById(R.id.song_title_main);
        artist_name_main = (TextView) findViewById(R.id.artist_name_main);

        if (fromAlbumsFragment) {
            fromAlbumInfo = true;
            fromArtistInfo = false;
        } else if (fromArtistsFragment) {
            fromArtistInfo = true;
            fromAlbumInfo = false;
        }

        getIntentMethod();

        //Getting albumName to fill the list
        if (fromAlbumsFragment) {
            album_title_albumInfo.setText(this.getIntent().getStringExtra("albumName"));
        } else if (fromArtistsFragment) {
            album_title_albumInfo.setText(this.getIntent().getStringExtra("artistName"));
        }

        //Sets information
        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);
        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(album_cover_in_itemInfo);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.default_cover)
                    .into(album_cover_in_itemInfo);
        }

        //Filling up arrayLists depending where its from
        if (fromAlbumsFragment) {
            albumName = getIntent().getStringExtra("albumName");
            int a = 0;
            for (int i = 0; i < songsList.size(); i++) {
                if (albumName.equals(songsList.get(i).getAlbum())) {
                    currentSongsInAlbum.add(a, songsList.get(i));
                    a++;
                }
            }

            AboutFragmentItemAdapter aboutFragmentItemAdapter = new AboutFragmentItemAdapter(this, currentSongsInAlbum, this);
            myRecyclerView = findViewById(R.id.itemSongs_recyclerview);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            myRecyclerView.setAdapter(aboutFragmentItemAdapter);
        } else {
            artistName = getIntent().getStringExtra("artistName");
            int b = 0;
            for (int i = 0; i < songsList.size(); i++) {
                if (artistName.equals(songsList.get(i).getArtist())) {
                    currentArtistSongs.add(b, songsList.get(i));
                    b++;
                }
            }

            AboutFragmentItemAdapter aboutFragmentItemAdapter = new AboutFragmentItemAdapter(this, currentArtistSongs, this);
            myRecyclerView = findViewById(R.id.itemSongs_recyclerview);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            myRecyclerView.setAdapter(aboutFragmentItemAdapter);
        }
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
        coverLoaded = false;
    }

    private void getIntentMethod() {
        if (songsList != null) {
            if (mediaPlayer.isPlaying()) {
                playPauseBtnInTab.setImageResource(R.drawable.pause_song);
            } else {
                playPauseBtnInTab.setImageResource(R.drawable.play_song);
            }
        }
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
            if (isPlaying) {
                onTrackPause();
            } else {
                audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
                onTrackPlay();
            }
    }

    public void onItemClick(int position) throws IOException {
        this.positionInInfoAboutItem = position;

        if (fromAlbumsFragment) {
            previousAlbumName = albumName;
            staticCurrentSongsInAlbum = currentSongsInAlbum;
            staticPreviousSongsInAlbum.clear();
            staticPreviousSongsInAlbum.addAll(currentSongsInAlbum);
            songNameStr = currentSongsInAlbum.get(position).getTitle();
            artistNameStr = currentSongsInAlbum.get(position).getArtist();
            this.getSharedPreferences("previousAlbumName", Context.MODE_PRIVATE).edit()
                    .putString("previousAlbumName", previousAlbumName).commit();
            fromAlbumInfo = true;
            fromArtistInfo = false;
            coverLoaded = false;
        } else if (fromArtistsFragment) {
            previousArtistName = artistName;
            staticCurrentArtistSongs = currentArtistSongs;
            staticPreviousArtistSongs.clear();
            staticPreviousArtistSongs.addAll(currentArtistSongs);
            songNameStr = currentArtistSongs.get(position).getTitle();
            artistNameStr = currentArtistSongs.get(position).getArtist();
            this.getSharedPreferences("previousArtistName", Context.MODE_PRIVATE).edit()
                    .putString("previousArtistName", previousArtistName).commit();
            fromArtistInfo = true;
            fromAlbumInfo = false;
            coverLoaded = false;
        }

        if (currentSongsInAlbum != null && fromAlbumsFragment) {
            uri = Uri.parse(currentSongsInAlbum.get(positionInInfoAboutItem).getData());
        } else if (currentArtistSongs != null) {
            uri = Uri.parse(currentArtistSongs.get(positionInInfoAboutItem).getData());
        }


        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(AboutFragmentItem.this, uri);
        onTrackPlay();


        metaDataInAboutFragmentItem(uri);

        if (fromAlbumInfo) {
            CreateNotification.createNotification(this, currentSongsInAlbum.get(position),
                    R.drawable.pause_song, position, currentSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, currentArtistSongs.get(position),
                    R.drawable.pause_song, position, currentArtistSongs.size() - 1);
        }

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("uri", uri.toString()).commit();
        this.getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", fromAlbumInfo).commit();
        this.getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        playPauseBtnInTab.setImageResource(R.drawable.pause_song);
    }

    public void switchToNextSong() {
        mediaPlayer.stop();
        mediaPlayer.release();

        //Checking is shuffle or repeat button clicked
        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(currentSongsInAlbum.size() - 1);
            } else if (songSearchWasOpened) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousSongsInAlbum.size()
                        ? (positionInInfoAboutItem = 0) : (positionInInfoAboutItem + 1);
            } else if (songSearchWasOpened) {
                position = position + 1 == songsFromSearch.size() ? (0)
                        : (position + 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem + 1 == staticPreviousArtistSongs.size()
                        ? (positionInInfoAboutItem = 0) : (positionInInfoAboutItem + 1);
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
        mediaPlayer = MediaPlayer.create(this, uri);

        mediaPlayer.start();

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("progress", uri.toString()).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
        this.getSharedPreferences("previousArtistName", Context.MODE_PRIVATE).edit()
                .putString("previousArtistName", previousArtistName).commit();
    }

    public void switchToPreviousSong() {
        mediaPlayer.stop();
        mediaPlayer.release();

        //Checking is shuffle or repeat button clicked
        if (shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = getRandom(currentSongsInAlbum.size() - 1);
            } else if (songSearchWasOpened) {
                position = getRandom(songsFromSearch.size() - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = getRandom(staticPreviousArtistSongs.size() - 1);
            } else {
                position = getRandom(songsList.size() - 1);
            }
        } else if (!shuffleBtnClicked && !repeatBtnClicked) {
            if (fromAlbumInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousSongsInAlbum.size() - 1)
                        : (positionInInfoAboutItem - 1);
            } else if (songSearchWasOpened) {
                position = position - 1 < 0 ? (songsFromSearch.size())
                        : (position - 1);
            } else if (fromArtistInfo) {
                positionInInfoAboutItem = positionInInfoAboutItem - 1 < 0 ? (staticPreviousArtistSongs.size() - 1)
                        : (positionInInfoAboutItem - 1);
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
        mediaPlayer = MediaPlayer.create(this, uri);

        mediaPlayer.start();

        song_title_in_album.setText(songNameStr);
        artist_name_in_album.setText(artistNameStr);

        this.getSharedPreferences("uri", Context.MODE_PRIVATE).edit()
                .putString("progress", uri.toString()).commit();
        this.getSharedPreferences("songNameStr", Context.MODE_PRIVATE).edit()
                .putString("songNameStr", songNameStr).commit();
        this.getSharedPreferences("artistNameStr", Context.MODE_PRIVATE).edit()
                .putString("artistNameStr", artistNameStr).commit();
        this.getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                .putInt("position", position).commit();
        this.getSharedPreferences("previousArtistName", Context.MODE_PRIVATE).edit()
                .putString("previousArtistName", previousArtistName).commit();
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

        trackAudioSource();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

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
        } else {
            playPauseBtnInTab.setImageResource(R.drawable.play_song);
        }
        mediaPlayer.setOnCompletionListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        intent.putExtra("previousAlbumName", previousAlbumName);
        intent.putExtra("previousArtistName", previousArtistName);
        //Checking is screen locked
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            this.unregisterReceiver(broadcastReceiverAboutFragmentInfo);
        }

        this.getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
        this.getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        switchToNextSong();
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
        if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
    }

    @Override
    public void onTrackNext() {
        isPlaying = true;
        switchToNextSong();
        if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
    }

    @Override
    public void onTrackPlay() {
        isPlaying = true;
        mediaPlayer.start();
        if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtnInTab.setImageResource(R.drawable.pause_song);
    }

    @Override
    public void onTrackPause() {
        isPlaying = false;
        mediaPlayer.pause();
        if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.play_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticPreviousArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.play_song, positionInInfoAboutItem, staticPreviousArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.play_song, position, songsList.size() - 1);
        }
        playPauseBtnInTab.setImageResource(R.drawable.play_song);
    }


}