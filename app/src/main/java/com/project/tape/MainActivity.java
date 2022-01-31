package com.project.tape;

import static com.project.tape.SongInfoTab.songSwitched;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {


    static ImageView album_cover_main;
    static TextView song_title_main, artist_name_main;

    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;
    Button fullInformationTabB;
    public static final int REQUEST_CODE = 1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        album_cover_main = findViewById(R.id.album_cover_main);
        song_title_main = findViewById(R.id.song_name_main);
        artist_name_main = findViewById(R.id.artist_name_main);

        permission();
        metaDataInMain(SongsFragment.uri);

        //OnClick Listener
        fullInformationTabB = (Button) findViewById(R.id.open_information_tab);
        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.open_information_tab:
                            Intent intent = new Intent(MainActivity.this, SongInfoTab.class);
                            startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        };
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

   //Sets album cover in main
    private void metaDataInMain(Uri uri) {
        if (songSwitched == true) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                Glide.with(this)
                        .asBitmap()
                        .load(art)
                        .into(album_cover_main);
            } else {
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.bebra)
                        .into(album_cover_main);
            }
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

}




