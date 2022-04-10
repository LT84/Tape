package com.project.tape.Fragments;

import static android.app.Activity.RESULT_OK;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.searchOpenedInArtistsFragments;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Adapters.ArtistsAdapter.mArtistsList;
import static com.project.tape.Fragments.AlbumsFragment.fromAlbumsFragment;
import static com.project.tape.Fragments.SongsFragment.artistList;
import static com.project.tape.Fragments.SongsFragment.artistName;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
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

import com.project.tape.Activities.AboutFragmentItem;
import com.project.tape.Adapters.ArtistsAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.SecondaryClasses.VerticalSpaceItemDecoration;

import java.io.IOException;


public class ArtistsFragment extends FragmentGeneral implements ArtistsAdapter.OnArtistListener, MediaPlayer.OnCompletionListener {

    TextView artist_name;
    LinearLayoutManager LLMAlbumFragment = new LinearLayoutManager(getContext());
    private RecyclerView myRecyclerView;
    private Parcelable listState;

    final int REQUEST_CODE = 1;
    private static final int VERTICAL_ITEM_SPACE = 25;

    public static ArtistsAdapter artistsAdapter;

    boolean oneTime = false;
    public static boolean fromArtistsFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.artists_fragment, container, false);
        coverLoaded = false;
        fromArtistsFragment = true;

        artist_name = v.findViewById(R.id.artist_name_artistsFragment);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.artists_recyclerview);
        song_title_main = (TextView) getActivity().findViewById(R.id.song_title_main);
        artist_name_main = (TextView) getActivity().findViewById(R.id.artist_name_main);
        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
        album_cover_main = (ImageView) getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = (ImageButton) getActivity().findViewById(R.id.pause_button);


        artistsAdapter = new ArtistsAdapter(getContext(), artistList, this);
        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(LLMAlbumFragment);
        myRecyclerView.setAdapter(artistsAdapter);
        myRecyclerView.setHasFixedSize(true);

        if (listState != null) {
            myRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            listState = savedInstanceState.getParcelable("ListState");
        }
        return v;
    }

    @Override
    public void OnArtistsClick(int position) throws IOException {
        fromArtistsFragment = true;
        fromAlbumsFragment = false;
        if (searchOpenedInArtistsFragments) {
            artistList.addAll(mArtistsList);
        }

        Intent intent = new Intent(getActivity(), AboutFragmentItem.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();

        if (searchOpenedInArtistsFragments) {
            intent.putExtra("artistName", mArtistsList.get(position).getArtist());
        } else {
            intent.putExtra("artistName", artistList.get(position).getArtist());
        }
        getActivity().getSharedPreferences("artistName", Context.MODE_PRIVATE).edit()
                .putString("artistName", artistName).commit();

        if (oneTime) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }

        startActivityForResult(intent, REQUEST_CODE, bundle);
        //Sorting artistsList
        sortArtistsList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    songNameStr = data.getStringExtra("songNameStr");
                    artistNameStr = data.getStringExtra("artistNameStr");
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createChannel();
        trackAudioSource();

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(getActivity());

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
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
        mediaPlayer.setOnCompletionListener(ArtistsFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onTrackNext();
        mediaPlayer.setOnCompletionListener(ArtistsFragment.this);
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


