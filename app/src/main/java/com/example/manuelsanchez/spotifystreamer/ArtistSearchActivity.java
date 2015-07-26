package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.*;


public class ArtistSearchActivity extends Activity
        implements ArtistSearchFragment.OnArtistSelectedListener, ArtistTopTracksFragment.OnTrackSelectedListener {

    private ArtistSearchFragment mSearchActivity;
    private ArtistTopTracksFragment mTopTrackActivity;
    private MusicPlayerFragment mMusicPlayerFragment;
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

        mSearchActivity.setOnArtistSelectedListener(this);

        if (savedInstanceState != null) {
            mMusicPlayerFragment = (MusicPlayerFragment) getFragmentManager().getFragment(savedInstanceState, "fragment_key");
        }

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
            if (mMusicPlayerFragment != null) {
                mMusicPlayerFragment.show(getFragmentManager(), "Now playing");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIsTwoPane) {
            getFragmentManager().putFragment(outState, "mContent", mMusicPlayerFragment);
        }
    }

    @Override
    public void onArtistSelected(String artistId) {
        if (mIsTwoPane) {
            mTopTrackActivity.displayArtistTracks(artistId);
        } else {
            /**
             * fragment transaction for tracks
             */
            if (mTopTrackActivity == null) {
                mTopTrackActivity = new ArtistTopTracksFragment();
            }
            Bundle args = new Bundle();
            args.putString(SELECTED_ARTIST_ID, artistId);
            mTopTrackActivity.setArguments(args);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.artist_search_container, mTopTrackActivity);
            fragmentTransaction.setBreadCrumbTitle("Main title");
            fragmentTransaction.setBreadCrumbShortTitle("Subtitle");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            /**
             * new activity for tracks
             *
             Intent intent = new Intent(getApplicationContext(), ArtistTopTracksActivity.class);
             intent.putExtra(SELECTED_ARTIST_ID, artistId);
             startActivity(intent);
             */

        }

    }

    @Override
    public void onTrackSelected(ArrayList<ArtistTopTrackItem> tracks, int trackIndex) {
        FragmentManager fragmentManager = getFragmentManager();
        if (mMusicPlayerFragment == null) {
            mMusicPlayerFragment = new MusicPlayerFragment();
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(TRACK_ITEMS, tracks);
        bundle.putInt(TRACK_INDEX, trackIndex);

        mMusicPlayerFragment.setArguments(bundle);
        if (mIsTwoPane) {
            mMusicPlayerFragment.show(fragmentManager, "dialog");
        } else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.artist_search_container, mMusicPlayerFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }

    public void setShareActionProvider(ShareActionProvider mShareActionProvider) {
        this.mShareActionProvider = mShareActionProvider;
    }

    public void shareTrack(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
    }

}
