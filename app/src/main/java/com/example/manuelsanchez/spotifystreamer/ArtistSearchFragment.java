package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ARTIST_ITEMS;


public class ArtistSearchFragment extends Fragment {

    private ArtistSearchListAdapter mArtistListAdapter = null;
    private OnArtistSelectedListener mOnArtistSelectedListener;


    public ArtistSearchFragment() {
    }

    public interface OnArtistSelectedListener {
        void onArtistSelected(String artistId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnArtistSelectedListener = (OnArtistSelectedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mArtistListAdapter = new ArtistSearchListAdapter(getActivity(), R.layout.artist_search_item);
        if (savedInstanceState != null) {
            ArrayList<ArtistSearchItem> artistSearchItems = savedInstanceState.getParcelableArrayList(ARTIST_ITEMS);
            mArtistListAdapter.addAll(artistSearchItems);
        }

        ListView searchResultListView = (ListView) view.findViewById(R.id.music_list_view);
        searchResultListView.setAdapter(mArtistListAdapter);
        searchResultListView.setOnItemClickListener(artistOnClickListener);

        EditText artistSearchEditText = (EditText) view.findViewById(R.id.music_search_input);
        artistSearchEditText.setOnEditorActionListener(editorActionListener);

        return view;
    }

    private AdapterView.OnItemClickListener artistOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ArtistSearchItem selectedArtist = (ArtistSearchItem) ((ListView) parent).getAdapter().getItem(position);
            mOnArtistSelectedListener.onArtistSelected(selectedArtist.getArtistId());
        }
    };

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView searchInput, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchInput.getText().length() > 0) {
                    new ArtistSearchTask(mArtistListAdapter).execute(searchInput.getText().toString());
                }
            }
            return false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARTIST_ITEMS, mArtistListAdapter.getItems());
    }
}
