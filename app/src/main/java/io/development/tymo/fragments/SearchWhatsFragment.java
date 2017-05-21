package io.development.tymo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.adapters.SearchMultipleAdapter;
import io.development.tymo.R;
import io.development.tymo.utils.Utilities;


public class SearchWhatsFragment extends Fragment {

    private EasyRecyclerView mRecyclerView;
    private SearchMultipleAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Object> listWhats = new ArrayList<>();

    public SearchWhatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (EasyRecyclerView) view.findViewById(R.id.listSearch);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(true);

        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setProgressView(R.layout.progress_loading_list);

        adapter = new SearchMultipleAdapter(getActivity());

        mRecyclerView.setAdapterWithProgress(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter.clear();
        adapter.addAll(listWhats);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getClass().getSimpleName(), null /* class override */);
        mRecyclerView.showProgress();
    }

    public void setAdapterItens(List<Object> list){
        listWhats.clear();
        listWhats.addAll(list);
        if(adapter != null) {
            adapter.clear();
            adapter.addAll(listWhats);
        }
    }

    public void showProgress(){
        if(mRecyclerView!=null)
            mRecyclerView.showProgress();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            adapter.clear();
            adapter.addAll(listWhats);
        }
    }
}