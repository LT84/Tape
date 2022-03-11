package com.project.tape;

import static com.project.tape.FragmentGeneral.songsList;
import static com.project.tape.SongsFragment.albumList;
import static com.project.tape.SongsFragment.mediaPlayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements androidx.appcompat.widget.SearchView.OnQueryTextListener {

    ImageButton playPauseBtn;
    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;
    Button fullInformationTabB;
    static String songNameStr, artistNameStr;

    static ArrayList<Song> songsFromSearch = new ArrayList<>();
    static boolean songSearchWasOpened;

    public static final int REQUEST_CODE = 1;

    boolean albumsFragmentSelected;

    static boolean searchOpenedInAlbumFragments;

    SearchView searchView;
    MenuItem menuItem;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.darkGrey));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);

        permission();

        playPauseBtn = findViewById(R.id.pause_button);
        fullInformationTabB = (Button) findViewById(R.id.open_information_tab);

        playPauseBtn.setOnClickListener(btnListener);
        fullInformationTabB.setOnClickListener(btnListener);

        //Tab Layout
        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.viewpager2);

        FragmentManager fm = getSupportFragmentManager();
        adapter = new FragmentAdapter(fm, getLifecycle());
        pager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Songs"));
        tabLayout.addTab(tabLayout.newTab().setText("Albums"));
        tabLayout.addTab(tabLayout.newTab().setText("Artists"));
        tabLayout.addTab(tabLayout.newTab().setText("Playlists"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    if (searchView != null) {
                        searchView.setQueryHint("Find your album");
                        albumsFragmentSelected = true;
                    }
                } else if (searchView != null) {
                    searchView.setQueryHint("Find your song");
                    albumsFragmentSelected = false;
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
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.play_song);
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            playPauseBtn.setImageResource(R.drawable.pause_song);
        }
    }

    //Permission to read data from phone
    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    ,REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Do what you want permission related
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        ,REQUEST_CODE);
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
            userInput = newText.toLowerCase();
            for (Song song : albumList) {
                if (song.getAlbum().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                }
            }
            AlbumsFragment.albumAdapter.updateSongList(mySearch);
        } else {
            songSearchWasOpened = true;
            for (Song song : songsList) {
                if (song.getTitle().toLowerCase().contains(userInput)) {
                    mySearch.add(song);
                    songsFromSearch.add(song);
                }
            }
            SongsFragment.songAdapter.updateSongList(mySearch);
        }
        return true;
    }


}