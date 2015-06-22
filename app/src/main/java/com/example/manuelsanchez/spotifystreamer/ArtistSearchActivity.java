package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ArtistSearchActivity extends Activity implements ArtistSearchFragment.OnArtistSelectedListener {

    private ArtistSearchFragment mSearchActivity;
    private ArtistTopTracksFragment mTopTrackActivity;
    private boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        mSearchActivity = (ArtistSearchFragment) getFragmentManager().findFragmentById(R.id.fragment_search);
        mTopTrackActivity = (ArtistTopTracksFragment) getFragmentManager().findFragmentById(R.id.fragment_top_tracks);

        View topTrackView = findViewById(R.id.fragment_top_tracks);
        mIsTwoPane = topTrackView != null && topTrackView.getVisibility() == View.VISIBLE;

        mSearchActivity.setOnArtistSelectedListener(this);

    }

    @Override
    public void onArtistSelected(String artistId) {
        if (mIsTwoPane) {
            mTopTrackActivity.displayArtistTracks(artistId);
        } else {
            Intent intent = new Intent(getApplicationContext(), ArtistTopTracksActivity.class);
            intent.putExtra(ArtistSearchFragment.SELECTED_ARTIST_ID, artistId);
            startActivity(intent);
        }

    }

}
