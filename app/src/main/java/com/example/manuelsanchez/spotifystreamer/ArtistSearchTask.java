package com.example.manuelsanchez.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


public class ArtistSearchTask extends AsyncTask<String, Void, List<ArtistSearchItem>> {

    private static final String LOG_TAG = ArtistSearchTask.class.getSimpleName();

    private final ArtistSearchListAdapter mArtistSearchItemArrayAdapter;
    private SpotifyApi api = new SpotifyApi();


    public ArtistSearchTask(ArtistSearchListAdapter artistSearchItemArrayAdapter) {
        this.mArtistSearchItemArrayAdapter = artistSearchItemArrayAdapter;
    }

    @Override
    protected List<ArtistSearchItem> doInBackground(String... params) {
        List<ArtistSearchItem> artists = new ArrayList<ArtistSearchItem>();
        if (params.length > 0) {
            try {
                String query = params[0];
                ArtistsPager pager = api.getService().searchArtists(query);
                artists.addAll(transform(pager.artists.items));
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "RetrofitError: " + e.getMessage());
                return null;
            }
        }
        return artists;
    }

    @Override
    protected void onPostExecute(List<ArtistSearchItem> artists) {
        if (artists == null) {
            Toast.makeText(mArtistSearchItemArrayAdapter.getContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {
            mArtistSearchItemArrayAdapter.clear();
            mArtistSearchItemArrayAdapter.addAll(artists);
            mArtistSearchItemArrayAdapter.notifyDataSetChanged();
            if (artists.size() == 0) {
                Toast.makeText(mArtistSearchItemArrayAdapter.getContext(), "No artists found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<ArtistSearchItem> transform(List<Artist> artists) {
        List<ArtistSearchItem> transformedArtists = new ArrayList<ArtistSearchItem>();
        for (Artist artist : artists) {
            ArtistSearchItem searchItem = new ArtistSearchItem(getImageUrl(artist), artist.id, artist.name);
            transformedArtists.add(searchItem);
        }
        return transformedArtists;
    }

    private String getImageUrl(Artist artist) {
        List<Image> images = artist.images;
        if (images.size() > 0) {
            return images.get(0).url;
        }
        return null;
    }

}
