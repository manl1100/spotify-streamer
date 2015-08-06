package com.example.manuelsanchez.spotifystreamer.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;

public class MusicPlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        ArrayList<ArtistTopTrackItem> artistTracks = getIntent().getParcelableArrayListExtra(TRACK_ITEMS);
        int trackIndex = getIntent().getIntExtra(TRACK_INDEX, 0);

        MusicPlayerFragment musicPlayerFragment = MusicPlayerFragment.newInstance(artistTracks, trackIndex);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.music_player_container, musicPlayerFragment, "music_fragment");
        fragmentTransaction.commit();
    }
}
