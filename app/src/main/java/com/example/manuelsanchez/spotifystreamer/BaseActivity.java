package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.squareup.okhttp.Call;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;

import java.util.ArrayList;

/**
 * Created by Manuel Sanchez on 7/26/15
 */
public abstract class BaseActivity extends Activity {

    private ShareActionProvider mShareActionProvider;
    protected ArrayList<ArtistTopTrackItem> mTracks;
    protected int mCurrentIndex;


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
            if (mTracks != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(TRACK_ITEMS, mTracks);
                bundle.putInt(TRACK_INDEX, mCurrentIndex);

                MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
                musicPlayerFragment.setArguments(bundle);
                musicPlayerFragment.show(getFragmentManager(), "dialog");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACK_ITEMS, mTracks);
        outState.putInt(TRACK_INDEX, mCurrentIndex);
    }

    public void setShareActionProvider(ShareActionProvider mShareActionProvider) {
        this.mShareActionProvider = mShareActionProvider;
    }

    public void shareTrack(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
    }
}
