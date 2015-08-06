package com.example.manuelsanchez.spotifystreamer.ui;

import android.os.Bundle;

import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

public class ArtistTopTracksActivity extends BaseActivity implements ArtistTopTracksFragment.OnTrackSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top_ten);
    }

    @Override
    public void onTrackSelected(ArrayList<ArtistTopTrackItem> artistTracks, int trackIndex) {
        displayMusicPlayer(artistTracks, trackIndex);
    }
}