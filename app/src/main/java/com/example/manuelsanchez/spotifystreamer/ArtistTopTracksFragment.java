package com.example.manuelsanchez.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class ArtistTopTracksFragment extends Fragment {

    public static final String TRACK = "selected_track";
    public static final String TRACK_ITEMS = "track_items";

    ArtistTopTracksListAdapter mArtistTopTracksListAdapter = null;


    public ArtistTopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_top_ten, container, true);

        mArtistTopTracksListAdapter = new ArtistTopTracksListAdapter(getActivity(), R.layout.artist_top_ten_item);

        ListView trackListView = (ListView) view.findViewById(R.id.artist_top_ten_list_view);
        trackListView.setAdapter(mArtistTopTracksListAdapter);
        trackListView.setOnItemClickListener(trackSelectionOnClickListener());

        if (savedInstanceState != null) {
            ArrayList<ArtistTopTrackItem> topTracks = savedInstanceState.getParcelableArrayList(TRACK_ITEMS);
            mArtistTopTracksListAdapter.addAll(topTracks);
        } else {
            String selectedArtist = getActivity().getIntent().getStringExtra(ArtistSearchFragment.SELECTED_ARTIST_ID);
            new ArtistTopTracksTask(mArtistTopTracksListAdapter).execute(selectedArtist);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACK_ITEMS, mArtistTopTracksListAdapter.getItems());
    }

    private AdapterView.OnItemClickListener trackSelectionOnClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistTopTrackItem selectedTrack = (ArtistTopTrackItem) ((ListView) parent).getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                intent.putExtra(TRACK, selectedTrack);
                startActivity(intent);
            }
        };
    }

}
