package com.example.manuelsanchez.spotifystreamer.util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.manuelsanchez.spotifystreamer.ui.ArtistTopTracksListAdapter;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;


public class ArtistTopTracksTask extends AsyncTask<String, Void, List<ArtistTopTrackItem>> {

    private static final String LOG_TAG = ArtistTopTracksTask.class.getSimpleName();

    private final ArtistTopTracksListAdapter mArtistTopTracksListAdapter;
    private SpotifyApi api = new SpotifyApi();


    public ArtistTopTracksTask(ArtistTopTracksListAdapter artistTopTracksListAdapter) {
        this.mArtistTopTracksListAdapter = artistTopTracksListAdapter;
    }

    @Override
    protected List<ArtistTopTrackItem> doInBackground(String... params) {
        List<ArtistTopTrackItem> tracks = new ArrayList<>();
        if (params.length > 0) {
            try {
                SpotifyService service = api.getService();
                String query = params[0];
                String countryCode = params[1];
                Map<String, Object> options = new HashMap<>();
                options.put(SpotifyService.COUNTRY, countryCode);
                tracks.addAll(transform(service.getArtistTopTrack(query, options).tracks));
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "RetrofitError: " + e.getMessage());
                return null;
            }

        }
        return tracks;
    }

    @Override
    protected void onPostExecute(List<ArtistTopTrackItem> tracks) {
        if (tracks == null) {
            Toast.makeText(mArtistTopTracksListAdapter.getContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {
            mArtistTopTracksListAdapter.clear();
            mArtistTopTracksListAdapter.addAll(tracks);
            mArtistTopTracksListAdapter.notifyDataSetChanged();
            if (tracks.size() == 0) {
                Toast.makeText(mArtistTopTracksListAdapter.getContext(), "No tracks found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<ArtistTopTrackItem> transform(List<Track> tracks) {
        List<ArtistTopTrackItem> trackItems = new ArrayList<>();
        for (Track track : tracks) {
            ArtistTopTrackItem trackItem = new ArtistTopTrackItem();
            trackItem.setArtist(track.artists.get(0).name);
            trackItem.setTrack(track.name);
            trackItem.setAlbum(track.album.name);
            trackItem.setDuration(track.duration_ms);
            trackItem.setPreviewUrl(track.preview_url);
            trackItem.setImageUrl(getImageUrl(track));
            trackItems.add(trackItem);
        }
        return trackItems;
    }

    private String getImageUrl(Track track) {
        List<Image> images = track.album.images;
        if (images.size() > 0) {
            return images.get(0).url;
        }
        return null;
    }

}
