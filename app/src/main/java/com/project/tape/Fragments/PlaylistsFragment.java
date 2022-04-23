package com.project.tape.Fragments;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.project.tape.Activities.AboutPlaylist.getSongsInPlaylistMap;
import static com.project.tape.Activities.AboutPlaylist.jsonDataMap;
import static com.project.tape.Activities.AboutPlaylist.jsonMap;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.project.tape.Activities.AboutPlaylist;
import com.project.tape.Adapters.PlaylistsAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.JsonDataMap;
import com.project.tape.SecondaryClasses.JsonDataPlaylists;
import com.project.tape.SecondaryClasses.Playlist;
import com.project.tape.SecondaryClasses.RecyclerItemClickListener;
import com.project.tape.SecondaryClasses.VerticalSpaceItemDecoration;

import java.io.IOException;
import java.util.ArrayList;


public class PlaylistsFragment extends FragmentGeneral implements PlaylistsAdapter.OnPlaylistListener {

    Button newPlaylistBtn, closeAddingNewPlaylistBtn, addNewPlaylistBtn, closeAlertPopupBtn, deletePlaylistBtn;
    EditText addNewPlaylistEditText;

    RecyclerView playlistRecyclerView;
    PlaylistsAdapter playlistsAdapter;
    private static final int VERTICAL_ITEM_SPACE = 3;

    final int REQUEST_CODE = 1;
    private int deletePosition;

    boolean fromLongClick;

    Gson gson = new Gson();
    String json;
    JsonDataPlaylists jsonDataPlaylists = new JsonDataPlaylists();

    public static ArrayList<Playlist> playlistsList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.playlists_fragment, container, false);
        newPlaylistBtn = v.findViewById(R.id.new_playlist_btn);
        newPlaylistBtn.setOnClickListener(btnL);

        //getArraylistOf playlists
        getSharedPlaylists();

        addNewPlaylistEditText = v.findViewById(R.id.new_playlist_name);

        //Sets adapter to list and applies settings to recyclerView
        playlistsAdapter = new PlaylistsAdapter(getContext(), playlistsList, this);
        playlistsAdapter.updatePlaylistList(playlistsList);
        playlistRecyclerView = v.findViewById(R.id.playlists_recyclerView);
        playlistRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistRecyclerView.setAdapter(playlistsAdapter);

        //RecyclerView listener
        playlistRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), playlistRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), AboutPlaylist.class);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();

                        intent.putExtra("playlistName", playlistsList.get(position).getPlaylistName());

//                     getActivity().unregisterReceiver(audioSourceChangedReceiver);

                        startActivityForResult(intent, REQUEST_CODE, bundle);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        fromLongClick = true;
                        deletePosition = position;
                        onButtonShowPopupWindowClick(view);
                    }
                })
        );
        return v;
    }

    public void onButtonShowPopupWindowClick(View view) {
        //Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;
        //Check which popup needed
        if (fromLongClick) {
            popupView = inflater.inflate(R.layout.popup_delete_permission, null);
        } else {
            popupView = inflater.inflate(R.layout.popup_window, null);
        }

        //Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Show the popup window
        //Which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, -230);

        //Listeners
        if (fromLongClick) {
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
                    getSongsInPlaylistMap.remove(playlistsList.get(deletePosition).getPlaylistName());

                    playlistsList.remove(deletePosition);

                    jsonDataMap = new JsonDataMap(getSongsInPlaylistMap);
                    jsonMap = gson.toJson(jsonDataMap);
                    getActivity().getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE).edit()
                            .putString("sharedJsonStringMap", jsonMap).commit();


                    playlistsAdapter.updatePlaylistList(playlistsList);
                    popupWindow.dismiss();
                }
            });
            fromLongClick = false;
        } else {
            //AddNewPlaylistBtn onClickListener
            addNewPlaylistBtn = popupView.findViewById(R.id.save_new_playlist);
            addNewPlaylistEditText = popupView.findViewById(R.id.new_playlist_name);
            addNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Playlist playlist = new Playlist();
                    playlist.setPlaylistName(addNewPlaylistEditText.getText().toString());
                    playlistsList.add(playlist);
                    popupWindow.dismiss();
                    playlistsAdapter.updatePlaylistList(playlistsList);
                }
            });

            //CloseAddingNewPlaylistBtn onClickListener
            closeAddingNewPlaylistBtn = popupView.findViewById(R.id.close_adding_new_playlist);
            closeAddingNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
        }
        //Dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.new_playlist_btn:
                    onButtonShowPopupWindowClick(v);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Save json
        jsonDataPlaylists = new JsonDataPlaylists(playlistsList);
        json = gson.toJson(jsonDataPlaylists);
        getActivity().getSharedPreferences("json", Context.MODE_PRIVATE).edit()
                .putString("json", json).commit();
    }

    //Get json
    public void getSharedPlaylists() {
        json = getActivity().getSharedPreferences("json", Context.MODE_PRIVATE)
                .getString("json", "");
        if (!json.equals("")) {
            jsonDataPlaylists = gson.fromJson(json, JsonDataPlaylists.class);
            playlistsList.addAll(jsonDataPlaylists.getArray());
        }
    }

    @Override
    public void onPlaylistClick(int position) throws IOException {
    }

    @Override
    public void onMediaButtonSingleClick() {
    }

    @Override
    public void onMediaButtonDoubleClick() {
    }


}
