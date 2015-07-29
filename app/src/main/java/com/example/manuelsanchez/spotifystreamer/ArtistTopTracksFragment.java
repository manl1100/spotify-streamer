package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.SELECTED_ARTIST_ID;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;


public class ArtistTopTracksFragment extends Fragment {

    private ArtistTopTracksListAdapter mArtistTopTracksListAdapter = null;
    private OnTrackSelectedListener mOnTrackSelectedListener;


    public ArtistTopTracksFragment() {
    }

    public interface OnTrackSelectedListener {
        void onTrackSelected(ArrayList<ArtistTopTrackItem> artistTracks, int trackIndex);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnTrackSelectedListener = (OnTrackSelectedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_top_ten, container, false);

        mArtistTopTracksListAdapter = new ArtistTopTracksListAdapter(getActivity(), R.layout.artist_top_ten_item);

        ListView trackListView = (ListView) view.findViewById(R.id.artist_top_ten_list_view);
        trackListView.setAdapter(mArtistTopTracksListAdapter);
        trackListView.setOnItemClickListener(trackSelectionOnClickListener());

        if (savedInstanceState != null) {
            ArrayList<ArtistTopTrackItem> topTracks = savedInstanceState.getParcelableArrayList(TRACK_ITEMS);
            mArtistTopTracksListAdapter.addAll(topTracks);
        } else {
            String selectedArtist = getActivity().getIntent().getStringExtra(SELECTED_ARTIST_ID);
            new ArtistTopTracksTask(mArtistTopTracksListAdapter).execute(selectedArtist);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACK_ITEMS, mArtistTopTracksListAdapter.getItems());
    }

    public void displayArtistTracks(String artistId) {
        new ArtistTopTracksTask(mArtistTopTracksListAdapter).execute(artistId);
    }

    private AdapterView.OnItemClickListener trackSelectionOnClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<ArtistTopTrackItem> artistTracks = mArtistTopTracksListAdapter.getItems();
                if (mOnTrackSelectedListener != null) {
                    mOnTrackSelectedListener.onTrackSelected(artistTracks, position);
                }
            }
        };
    }

}
