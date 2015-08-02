package com.example.manuelsanchez.spotifystreamer.ui;

import android.app.FragmentTransaction;
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
        MusicPlayerFragment musicPlayerFragment = MusicPlayerFragment.newInstance(artistTracks, trackIndex);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.top_tracks_container, musicPlayerFragment, "music_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}