package com.project.tape.Activities;

import static com.project.tape.Activities.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.Activities.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.Activities.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.Adapters.AlbumsAdapter.mAlbums;
import static com.project.tape.Fragments.AlbumsFragment.albumsAdapter;
import static com.project.tape.Fragments.FragmentGeneral.audioFocusRequest;
import static com.project.tape.Fragments.FragmentGeneral.audioManager;
import static com.project.tape.Fragments.FragmentGeneral.focusRequest;
import static com.project.tape.Fragments.FragmentGeneral.isPlaying;
import static com.project.tape.Fragments.FragmentGeneral.position;
import static com.project.tape.Fragments.FragmentGeneral.songsList;
import static com.project.tape.Fragments.PlaylistsFragment.playListsList;
import static com.project.tape.Fragments.SongsFragment.artistList;
import static com.project.tape.Fragments.SongsFragment.mediaPlayer;
import static com.project.tape.Fragments.SongsFragment.staticCurrentArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousArtistSongs;
import static com.project.tape.Fragments.SongsFragment.staticPreviousSongsInAlbum;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.project.tape.Adapters.FragmentsAdapter;
import com.project.tape.Fragments.AlbumsFragment;
import com.project.tape.Fragments.ArtistsFragment;
import com.project.tape.Fragments.PlaylistsFragment;
import com.project.tape.Fragments.SongsFragment;
import com.project.tape.Interfaces.Playable;
import com.project.tape.ItemClasses.Album;
import com.project.tape.ItemClasses.Playlist;
import com.project.tape.ItemClasses.Song;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.CreateNotification;
import com.project.tape.SecondaryClasses.MusicLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements androidx.appcompat.widget.SearchView.OnQueryTextListener, Playable, LoaderManager.LoaderCallbacks<List<Album>> {

    ImageButton playPauseBtn;
    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentsAdapter adapter;
    Button fullInformationTab;
    SearchView searchView;
    MenuItem menuItem, sortBtn;
    public static String songNameStr;
    public static String artistNameStr;
    public static String SORT_PREF = "SortOrder";

    public static ArrayList<Song> songsFromSearch = new ArrayList<>();
    public static boolean songSearchWasOpened;

    public static boolean searchOpenedInAlbumFragments, searchOpenedInArtistsFragments,
            searchSongsFragmentSelected, fromSearch;

    boolean albumsFragmentSelected, artistsFragmentSelected, songsFragmentSelected, playlistFragmentSelected;

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

        //Read external storage permission
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

        if (Locale.getDefault().getLanguage().equals("en")) {
            tabLayout.addTab(tabLayout.newTab().setText("Songs"));
            tabLayout.addTab(tabLayout.newTab().setText("Albums"));
            tabLayout.addTab(tabLayout.newTab().setText("Artists"));
            tabLayout.addTab(tabLayout.newTab().setText("Playlists"));
        } else {
            tabLayout.addTab(tabLayout.newTab().setText("Композиции"));
            tabLayout.addTab(tabLayout.newTab().setText("Альбомы"));
            tabLayout.addTab(tabLayout.newTab().setText("Исполнители"));
            tabLayout.addTab(tabLayout.newTab().setText("Плейлисты"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (searchView != null) {
                    if (tab.getPosition() == 0) {
                        if (Locale.getDefault().getLanguage().equals("en")) {
                            searchView.setQueryHint("Find your song");
                        } else {
                            searchView.setQueryHint("Найти композицию");
                        }
                        songsFragmentSelected = true;
                        SongsFragment.songsFragmentOpened = true;
                        artistsFragmentSelected = false;
                        albumsFragmentSelected = false;
                        playlistFragmentSelected = false;
                        sortBtn.setEnabled(true);
                        menuItem.setVisible(true);
                    } else if (tab.getPosition() == 1) {
                        if (Locale.getDefault().getLanguage().equals("en")) {
                            searchView.setQueryHint("Find your album");
                        } else {
                            searchView.setQueryHint("Найти альбом");
                        }
                        songsFragmentSelected = false;
                        albumsFragmentSelected = true;
                        artistsFragmentSelected = false;
                        sortBtn.setEnabled(false);
                        menuItem.setVisible(true);
                        playlistFragmentSelected = false;
                    } else if (tab.getPosition() == 2) {
                        if (Locale.getDefault().getLanguage().equals("en")) {
                            searchView.setQueryHint("Find your artist");
                        } else {
                            searchView.setQueryHint("Найти исполнителя");
                        }
                        songsFragmentSelected = false;
                        artistsFragmentSelected = true;
                        albumsFragmentSelected = false;
                        playlistFragmentSelected = false;
                        sortBtn.setEnabled(false);
                        menuItem.setVisible(true);
                    } else if (tab.getPosition() == 3) {
                        if (Locale.getDefault().getLanguage().equals("en")) {
                            searchView.setQueryHint("Find your playlist");
                        } else {
                            searchView.setQueryHint("Найти плейлист");
                        }
                        songsFragmentSelected = false;
                        artistsFragmentSelected = false;
                        albumsFragmentSelected = false;
                        playlistFragmentSelected = true;
                        sortBtn.setEnabled(false);
                        menuItem.setVisible(true);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(1);
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

    //Read external storage permission
    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                    , REQUEST_CODE);
        }
    }

    //Read external storage request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do what you want permission related
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
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
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                    startActivity(intent, bundle);
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
        sortBtn = menu.findItem(R.id.sort_option);
        menuItem = menu.findItem(R.id.search_option);
        sortBtn.getActionView();
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager2.setUserInputEnabled(false);
                sortBtn.setVisible(false);
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
                sortBtn.setVisible(true);

                //Loads albums list again, after search closed
                final LoaderManager supportLoaderManager = MainActivity.this.getSupportLoaderManager();
                supportLoaderManager.initLoader(1, null, MainActivity.this);

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
        songsFromSearch.clear();
        String userInput = newText.toLowerCase();
        ArrayList<Song> mySearch = new ArrayList<>();
        ArrayList<Album> myAlbumSearch = new ArrayList<>();
        ArrayList<Playlist> myPlaylistSearch = new ArrayList<>();
        if (albumsFragmentSelected) {
            searchOpenedInAlbumFragments = true;
            songSearchWasOpened = true;
            userInput = newText.toLowerCase();
            for (Album album : mAlbums) {
                if (album.getAlbumName().toLowerCase().contains(userInput)) {
                    myAlbumSearch.add(album);
                }
            }
            AlbumsFragment.albumsAdapter.updateAlbumList(myAlbumSearch);
        }
        if (artistsFragmentSelected) {
            searchOpenedInArtistsFragments = true;
            songSearchWasOpened = true;
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
            fromSearch = true;
            userInput = newText.toLowerCase();
            for (Song song : songsList) {
                if (song.getTitle().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                    songsFromSearch.add(song);
                }
            }
            SongsFragment.songsAdapter.updateSongList(mySearch);
        } else {
            songSearchWasOpened = true;
            userInput = newText.toLowerCase();
            for (Playlist playlist : playListsList) {
                if (playlist.getPlaylistName().toLowerCase().contains(userInput)) {
                    myPlaylistSearch.add(playlist);
                }
            }
            PlaylistsFragment.playlistsAdapter.updatePlaylistList(myPlaylistSearch);
        }
        return true;
    }

    //Select sort order
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(SORT_PREF, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.sort_option:
                Intent intent = new Intent(MainActivity.this, SortChoice.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                startActivity(intent, bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Create NotificationChannel
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
    public void onTrackPrevious() {
    }

    @Override
    public void onTrackNext() {
    }

    @Override
    public void onTrackPlay() {
        isPlaying = true;
        mediaPlayer.start();
        if (fromSearch) {
            CreateNotification.createNotification(this, songsFromSearch.get(position),
                    R.drawable.ic_pause_song, position, songsFromSearch.size() - 1);
        } else if (fromAlbumInfo) {
            CreateNotification.createNotification(this, staticPreviousSongsInAlbum.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticPreviousSongsInAlbum.size() - 1);
        } else if (fromArtistInfo) {
            CreateNotification.createNotification(this, staticCurrentArtistSongs.get(positionInInfoAboutItem),
                    R.drawable.ic_pause_song, positionInInfoAboutItem, staticCurrentArtistSongs.size() - 1);
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_pause_song, position, songsList.size() - 1);
        }
        audioFocusRequest = audioManager.requestAudioFocus(focusRequest);
        playPauseBtn.setImageResource(R.drawable.ic_pause_song);
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
        } else {
            CreateNotification.createNotification(this, songsList.get(position),
                    R.drawable.ic_play_song, position, songsList.size() - 1);
        }
        playPauseBtn.setImageResource(R.drawable.ic_play_song);
    }

    //Music loader methods
    @NonNull
    @Override
    public Loader<List<Album>> onCreateLoader(int id, @Nullable Bundle args) {
        return new MusicLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Album>> loader, List<Album> data) {
        if (!songsFragmentSelected) {
            albumsAdapter.addItems(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Album>> loader) {
        if (!songsFragmentSelected) {
            albumsAdapter.clearItem();
        }
    }

    @Override
    public void onBackPressed() {
    }
}