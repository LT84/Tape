package com.project.tape;

import static android.app.Activity.RESULT_OK;
import static com.project.tape.AboutFragmentItem.fromAlbumInfo;
import static com.project.tape.AboutFragmentItem.fromArtistInfo;
import static com.project.tape.AboutFragmentItem.positionInInfoAboutItem;
import static com.project.tape.AlbumAdapter.mAlbumList;
import static com.project.tape.MainActivity.artistNameStr;
import static com.project.tape.MainActivity.searchOpenedInAlbumFragments;
import static com.project.tape.MainActivity.songNameStr;
import static com.project.tape.SongInfoTab.repeatBtnClicked;
import static com.project.tape.SongsFragment.albumList;
import static com.project.tape.SongsFragment.previousAlbumName;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class AlbumsFragment extends FragmentGeneral implements AlbumAdapter.OnAlbumListener, MediaPlayer.OnCompletionListener {

    TextView album_title_albumFragments;
    ImageView album_cover_albumFragment;
    private Parcelable listState;
    private RecyclerView myRecyclerView;
    LinearLayoutManager LLMAlbumFragment = new LinearLayoutManager(getContext());

    int positionIndex, topView;
    final int REQUEST_CODE = 1;
    private static final int VERTICAL_ITEM_SPACE = 10;

    static AlbumAdapter albumAdapter;

    static boolean fromAlbumsFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.albums_fragment, container, false);
        coverLoaded = false;

        //Init views
        album_title_albumFragments = (TextView) v.findViewById(R.id.album_title_albumFragment);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.albums_recyclerview);
        album_cover_albumFragment = v.findViewById(R.id.album_cover_albumFragment);
        song_title_main = (TextView) getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView) getActivity().findViewById(R.id.artist_name_main);
        //Setting song title and artist name in infoTab
        album_cover_main = (ImageView) getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = (ImageButton) getActivity().findViewById(R.id.pause_button);
        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);

        //Sets adapter to list and applies settings to recyclerView
        albumAdapter = new AlbumAdapter(getContext(), albumList, this);

        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(LLMAlbumFragment);
        myRecyclerView.setAdapter(albumAdapter);
        myRecyclerView.setItemViewCacheSize(300);
        myRecyclerView.setDrawingCacheEnabled(true);
        myRecyclerView.setHasFixedSize(true);

        //Opens place where recyclerView has stopped
        if (listState != null) {
            myRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            listState = savedInstanceState.getParcelable("ListState");
        }
        mediaPlayer.setOnCompletionListener(AlbumsFragment.this);
        return v;
    }

    //Saving place where recyclerView stopped
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", myRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    //ClickListener in recyclerView
    @Override
    public void onAlbumClick(int position) throws IOException {
        fromAlbumsFragment = true;

        if (searchOpenedInAlbumFragments) {
            albumList.addAll(mAlbumList);
        }

        Intent intent = new Intent(getActivity(), AboutFragmentItem.class);

        if (searchOpenedInAlbumFragments) {
            intent.putExtra("albumName", mAlbumList.get(position).getAlbum());
        } else {
            intent.putExtra("albumName", albumList.get(position).getAlbum());
        }
        getActivity().getSharedPreferences("fromAlbumInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromAlbumInfo", true).commit();
        startActivityForResult(intent, REQUEST_CODE);
        //Sorting albums in albumsFragment
        sortAlbumsList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    songNameStr = data.getStringExtra("titleToMain");
                    artistNameStr = data.getStringExtra("ArtistNameToMain");
                    previousAlbumName = data.getStringExtra("previousAlbumName");
                    getActivity().getSharedPreferences("previousAlbumName", Context.MODE_PRIVATE).edit()
                            .putString("previousAlbumName", previousAlbumName);
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        positionIndex = LLMAlbumFragment.findFirstVisibleItemPosition();
        View startView = myRecyclerView.getChildAt(0);
        topView = (startView == null) ? 0 : (startView.getTop() - myRecyclerView.getPaddingTop());
        if (repeatBtnClicked) {
            repeatBtnClicked = true;
            getContext().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", true).commit();
        } else {
            repeatBtnClicked = false;
            getContext().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", false).commit();
        }
        getContext().getSharedPreferences("fromArtistInfo", Context.MODE_PRIVATE).edit()
                .putBoolean("fromArtistInfo", fromArtistInfo).commit();
        super.onPause();
    }

    @Override
    public void onResume() {
        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
        mediaPlayer.setOnCompletionListener(AlbumsFragment.this);
        if (mediaPlayer != null) {
            if (!coverLoaded) {
                if (uri != null) {
                    metaDataInFragment(uri);
                    coverLoaded = true;
                }
            }

            if (mediaPlayer.isPlaying()) {
                mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
            } else {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }
        } else {
            song_title_main.setText(" ");
            artist_name_main.setText(" ");
        }
        super.onResume();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        switchNextSongInFragment();
        mediaPlayer.setOnCompletionListener(AlbumsFragment.this);
        if (fromAlbumInfo) {
            getContext().getSharedPreferences("positionInInfoAboutItem", Context.MODE_PRIVATE).edit()
                    .putInt("positionInInfoAboutItem", positionInInfoAboutItem).commit();
        } else {
            getActivity().getSharedPreferences("position", Context.MODE_PRIVATE).edit()
                    .putInt("position", position).commit();
        }
    }


}
