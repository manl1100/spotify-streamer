package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.util.ArrayList;

public class ArtistSearchActivity extends Activity
        implements ArtistSearchFragment.OnArtistSelectedListener, ArtistTopTracksFragment.OnTrackSelectedListener {

    private ArtistSearchFragment mSearchActivity;
    private ArtistTopTracksFragment mTopTrackActivity;
    private boolean mIsTwoPane;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        mSearchActivity = (ArtistSearchFragment) getFragmentManager().findFragmentById(R.id.fragment_search);
        mTopTrackActivity = (ArtistTopTracksFragment) getFragmentManager().findFragmentById(R.id.fragment_top_tracks);

        View topTrackView = findViewById(R.id.fragment_top_tracks);
        mIsTwoPane = topTrackView != null && topTrackView.getVisibility() == View.VISIBLE;

        if (mIsTwoPane) {
            mTopTrackActivity.setOnTrackSelectedListener(this);
        }
        mSearchActivity.setOnArtistSelectedListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = new ShareActionProvider(this);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        mShareActionProvider.setShareIntent(sendIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_item_share) {
            Toast.makeText(this, "Share something", Toast.LENGTH_LONG).show();
        } else if (id == R.id.action_now_playing) {
            Toast.makeText(this, "Now playing", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onTrackSelected(ArrayList<ArtistTopTrackItem> tracks, int trackIndex) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ArtistTopTracksFragment.TRACK, tracks);
        bundle.putInt(ArtistTopTracksFragment.TRACK_INDEX, trackIndex);

        FragmentManager fragmentManager = getFragmentManager();
        MusicPlayerFragment musicPlayer = new MusicPlayerFragment();
        musicPlayer.setArguments(bundle);

        musicPlayer.show(fragmentManager, "dialog");

    }

    public void setShareActionProvider(ShareActionProvider mShareActionProvider) {
        this.mShareActionProvider = mShareActionProvider;
    }

    public void shareTrack(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
    }

}
