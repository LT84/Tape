package com.project.tape.Activities;

import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.position;
import static com.project.tape.Fragments.FragmentGeneral.songsList;
import static com.project.tape.Fragments.SongsFragment.albumList;
import static com.project.tape.Fragments.SongsFragment.artistList;
import static com.project.tape.Fragments.SongsFragment.mediaPlayer;
import static com.project.tape.Fragments.SongsFragment.staticCurrentArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticCurrentSongsInAlbum;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.project.tape.Adapters.FragmentsAdapter;
import com.project.tape.Interfaces.Playable;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.Fragments.AlbumsFragment;
import com.project.tape.Fragments.ArtistsFragment;
import com.project.tape.Fragments.SongsFragment;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.Song;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements androidx.appcompat.widget.SearchView.OnQueryTextListener, Playable {

    ImageButton playPauseBtn;
    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentsAdapter adapter;
    Button fullInformationTab;
    SearchView searchView;
    MenuItem menuItem;
    public static String songNameStr;
    public static String artistNameStr;

    public static ArrayList<Song> songsFromSearch = new ArrayList<>();
    public static boolean songSearchWasOpened;

    public static boolean searchOpenedInAlbumFragments, searchOpenedInArtistsFragments,
            searchSongsFragmentSelected;

    boolean albumsFragmentSelected, artistsFragmentSelected, songsFragmentSelected;

    public static final int REQUEST_CODE = 1;

    NotificationManager notificationManager;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.darkGrey));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);
        songsFragmentSelected = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        permission();

        playPauseBtn = findViewById(R.id.pause_button);
        fullInformationTab = (Button) findViewById(R.id.open_information_tab);

        playPauseBtn.setOnClickListener(btnListener);
        fullInformationTab.setOnClickListener(btnListener);

        //Tab Layout
        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.viewpager2);

        FragmentManager fm = getSupportFragmentManager();
        adapter = new FragmentsAdapter(fm, getLifecycle());
        pager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Songs"));
        tabLayout.addTab(tabLayout.newTab().setText("Albums"));
        tabLayout.addTab(tabLayout.newTab().setText("Artists"));
        tabLayout.addTab(tabLayout.newTab().setText("Playlists"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (searchView != null) {
                    if (tab.getPosition() == 1) {
                        searchView.setQueryHint("Find your album");
                        songsFragmentSelected = false;
                        albumsFragmentSelected = true;
                        artistsFragmentSelected = false;
                    } else if (tab.getPosition() == 2) {
                        searchView.setQueryHint("Find your artist");
                        songsFragmentSelected = false;
                        artistsFragmentSelected = true;
                        albumsFragmentSelected = false;
                    } else if (tab.getPosition() == 0) {
                        searchView.setQueryHint("Find your song");
                        songsFragmentSelected = true;
                        artistsFragmentSelected = false;
                        albumsFragmentSelected = false;
                    }
                }
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    //Sets play button image in main
    public void playPauseBtnClicked() {
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        if (audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (isPlaying) {
                onTrackPause();
            } else {
                onTrackPlay();
            }
        }
    }

    //Permission to read data from phone
    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do what you want permission related
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE);
            }
        }
    }

    //OnClick Listener
    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_information_tab:
                    Intent intent = new Intent(MainActivity.this, SongInfoTab.class);
                    startActivity(intent);
                    break;
                case R.id.pause_button:
                    playPauseBtnClicked();
                    break;
                default:
                    break;
            }
        }
    };


    //Functions for search
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        menuItem = menu.findItem(R.id.search_option);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Find your song");
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager2.setUserInputEnabled(false);
                LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
                for (int i = 0; i < tabStrip.getChildCount(); i++) {
                    tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                }
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                pager2.setUserInputEnabled(true);
                songSearchWasOpened = false;
                LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
                for (int i = 0; i < tabStrip.getChildCount(); i++) {
                    tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<Song> mySearch = new ArrayList<>();
        if (albumsFragmentSelected) {
            searchOpenedInAlbumFragments = true;
            songSearchWasOpened = true;
            userInput = newText.toLowerCase();
            for (Song song : albumList) {
                if (song.getAlbum().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                }
            }
            AlbumsFragment.albumsAdapter.updateAlbumList(mySearch);
        }
        if (artistsFragmentSelected) {
            searchOpenedInArtistsFragments = true;
            songSearchWasOpened = false;
            userInput = newText.toLowerCase();
            for (Song song : artistList) {
                if (song.getArtist().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                }
            }
            ArtistsFragment.artistsAdapter.updateArtistsList(mySearch);
        } else if (songsFragmentSelected) {
            searchSongsFragmentSelected = true;
            songSearchWasOpened = true;
            userInput = newText.toLowerCase();
            for (Song song : songsList) {
                if (song.getTitle().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                    songsFromSearch.add(song);
                }
            }
            SongsFragment.songsAdapter.updateSongList(mySearch);
        }
        return true;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Tape", NotificationManager.IMPORTANCE_HIGH);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancel(1);
        }
    }

    @Override
    public void onTrackPrevious() {

    }

    @Override
    public void onTrackNext() {

    }

    @Override
    public void onTrackPlay() {
        isPlaying = true;
        mediaPlayer.start();
        if (songSearchWasOpened) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticCurrentArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.pause_song, positionInInfoAboutItem, staticCurrentArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtn.setImageResource(R.drawable.pause_song);
    }

    @Override
    public void onTrackPause() {
        isPlaying = false;
        mediaPlayer.pause();
        if (songSearchWasOpened) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.play_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticCurrentSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.play_song, positionInInfoAboutItem, staticCurrentSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticCurrentArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.play_song, positionInInfoAboutItem, staticCurrentArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.play_song, position, songsList.size() - 1);
        }
        playPauseBtn.setImageResource(R.drawable.play_song);
    }


}